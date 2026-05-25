package ru.pozdeev.otp.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaRequest;
import ru.pozdeev.otp.exception.OtpException;
import ru.pozdeev.otp.util.JsonUtil;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static ru.pozdeev.otp.util.Constants.MDC_KAFKA_MESSAGE_ID;
import static ru.pozdeev.otp.util.Constants.MDC_KAFKA_TOPIC;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "otp.kafka.send-otp", name = "enabled", havingValue = "true")
public class OtpSendKafkaProducer {

    private final JsonUtil jsonUtil;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${otp.kafka.send-otp.send-topic}")
    private String topicIn;

    public void sendMessage(SendOtpKafkaRequest kafkaRequest) throws TimeoutException {
        try {
            MDC.put(MDC_KAFKA_MESSAGE_ID, kafkaRequest.getId());

            SendResult<String, String> result = kafkaTemplate.send(topicIn, jsonUtil.toJson(kafkaRequest)).get(5, TimeUnit.SECONDS);

            log.info("Запрос отправлен в кафку. Партиция: {}, Offset: {}",
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        } catch (InterruptedException | ExecutionException e) {
            throw new OtpException("Ошибка отправки сообщения в кафку", e);
        } finally {
            MDC.clear();
        }
    }
}
