package ru.pozdeev.otp.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ru.pozdeev.otp.dto.kafka.KafkaResponse;
import ru.pozdeev.otp.service.KafkaResponseService;
import ru.pozdeev.otp.util.JsonUtil;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "otp.kafka.send-otp", name = "enabled", havingValue = "true")
public class KafkaConsumer {

    private final KafkaResponseService kafkaResponseService;

    @KafkaListener(topics = "${otp.kafka.send-otp.get-topic}")
    public void consume(ConsumerRecord<String, String> consumerRecord,
                        @Payload String payload,
                        @Header(KafkaHeaders.GROUP_ID) String groupId,
                        @Header(value = KafkaHeaders.KEY, required = false) String key) {

        log.info("Ответ от кафки получен. Топик: {}, Партиция: {}, Offset: {}, Key: {}, GroupId: {}",
                consumerRecord.topic(),
                consumerRecord.partition(),
                consumerRecord.offset(),
                key, groupId);

        try {
            KafkaResponse kafkaResponse = JsonUtil.fromJson(payload, KafkaResponse.class);

            kafkaResponseService.processKafkaResponse(kafkaResponse);
        } catch (Exception e) {
            log.warn("Ошибка десериализации сообщения от Kafka", e);
        }
    }
}
