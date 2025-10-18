package ru.pozdeev.otp.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.pozdeev.otp.dto.kafka.KafkaRequest;
import ru.pozdeev.otp.exception.OtpException;
import ru.pozdeev.otp.util.JsonUtil;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "otp.kafka.send-otp", name = "enabled", havingValue = "true")
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${otp.kafka.send-otp.send-topic}")
    private String topicIn;

    public void sendMessage(KafkaRequest kafkaRequest) throws TimeoutException {

        try {
            SendResult<String, String> result = kafkaTemplate.send(topicIn, JsonUtil.toJson(kafkaRequest)).get(5, TimeUnit.SECONDS);

            log.info("Запрос отправлен в кафку. Топик: {}, Партиция: {}, Offset: {}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        } catch (InterruptedException | ExecutionException e) {
            throw new OtpException("Ошибка отправки сообщения в кафку", e);
        }
    }
}
