package ru.pozdeev.otp.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponse;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponseStatus;
import ru.pozdeev.otp.entity.OtpSendStatus;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.exception.OtpException;
import ru.pozdeev.otp.repository.SendOtpRepository;
import ru.pozdeev.otp.sender.Sender;
import ru.pozdeev.otp.util.JsonUtil;

import static ru.pozdeev.otp.util.Constants.MDC_KAFKA_GROUP_ID;
import static ru.pozdeev.otp.util.Constants.MDC_KAFKA_KEY;
import static ru.pozdeev.otp.util.Constants.MDC_KAFKA_MESSAGE_ID;
import static ru.pozdeev.otp.util.Constants.MDC_KAFKA_OFFSET;
import static ru.pozdeev.otp.util.Constants.MDC_KAFKA_PARTITION;
import static ru.pozdeev.otp.util.Constants.MDC_KAFKA_TOPIC;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "otp.kafka.send-otp", name = "enabled", havingValue = "true")
public class OtpSendKafkaConsumer {

    private final JsonUtil jsonUtil;
    private final SendOtpRepository sendOtpRepository;
    private final Sender<SendOtpKafkaResponse> telegramSender;

    public OtpSendKafkaConsumer(JsonUtil jsonUtil, SendOtpRepository sendOtpRepository, @Qualifier("telegramSender") Sender telegramSender) {
        this.jsonUtil = jsonUtil;
        this.sendOtpRepository = sendOtpRepository;
        this.telegramSender = telegramSender;
    }

    @KafkaListener(topics = "${otp.kafka.send-otp.get-topic}")
    public void consume(ConsumerRecord<String, String> consumerRecord,
                        @Header(KafkaHeaders.GROUP_ID) String groupId) {
        try {
            MDC.put(MDC_KAFKA_TOPIC, consumerRecord.topic());
            MDC.put(MDC_KAFKA_PARTITION, String.valueOf(consumerRecord.partition()));
            MDC.put(MDC_KAFKA_OFFSET, String.valueOf(consumerRecord.offset()));
            MDC.put(MDC_KAFKA_KEY, consumerRecord.key());
            MDC.put(MDC_KAFKA_GROUP_ID, groupId);

            log.info("Ответ от кафки получен");

            SendOtpKafkaResponse kafkaResponse = jsonUtil.fromJson(consumerRecord.value(), SendOtpKafkaResponse.class);
            MDC.put(MDC_KAFKA_MESSAGE_ID, kafkaResponse.getId());

            SendOtp sendOtp = sendOtpRepository.findBySendMessageKey(kafkaResponse.getId())
                    .orElseThrow(() -> new OtpException(String.format("Не найден SendOtp с ID: %s, пропускаем обработку", kafkaResponse.getId())));

            telegramSender.completeResponse(kafkaResponse);

            if (kafkaResponse.getStatus() == SendOtpKafkaResponseStatus.SUCCESS) {
                sendOtp.setStatus(OtpSendStatus.DELIVERED);
                log.info("OTP с ID {} успешно доставлен через Kafka. Статус в БД обновлен.", kafkaResponse.getId());
            } else {
                sendOtp.setStatus(OtpSendStatus.ERROR);
                log.warn("Ошибка доставки OTP с ID {} через Kafka: {}. Статус в БД обновлен.", kafkaResponse.getId(), kafkaResponse.getErrorMessage());
            }

            sendOtpRepository.save(sendOtp);
            log.debug("Статус SendOtp с ID {} обновлен на: {}", kafkaResponse.getId(), sendOtp.getStatus());

        } catch (Exception e) {
            log.warn("Ошибка при обработке сообщения от Kafka", e);
        } finally {
            MDC.clear();
        }
    }
}
