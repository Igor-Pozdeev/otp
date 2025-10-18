package ru.pozdeev.otp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pozdeev.otp.dto.kafka.KafkaResponse;
import ru.pozdeev.otp.dto.kafka.KafkaResponseStatus;
import ru.pozdeev.otp.entity.OtpSendStatus;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.exception.OtpException;
import ru.pozdeev.otp.repository.SendOtpRepository;
import ru.pozdeev.otp.service.KafkaResponseService;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaResponseServiceImpl implements KafkaResponseService {

    private final SendOtpRepository sendOtpRepository;

    @Override
    public void processKafkaResponse(KafkaResponse kafkaResponse) {
        log.info("Обработка ответа от Kafka. ID: {}, Status: {}, Error: {}",
                kafkaResponse.getId(),
                kafkaResponse.getStatus(),
                kafkaResponse.getErrorMessage());

        SendOtp sendOtp = sendOtpRepository.findBySendMessageKey(kafkaResponse.getId())
                .orElseThrow(() -> new OtpException(String.format("Не найден SendOtp с ID: %s, пропускаем обработку", kafkaResponse.getId())));

        if (kafkaResponse.getStatus() == KafkaResponseStatus.SUCCESS) {
            sendOtp.setStatus(OtpSendStatus.DELIVERED);
            log.info("OTP с ID {} успешно доставлен через Kafka", kafkaResponse.getId());
        } else {
            sendOtp.setStatus(OtpSendStatus.ERROR);
            log.warn("Ошибка доставки OTP с ID {} через Kafka: {}",
                    kafkaResponse.getId(), kafkaResponse.getErrorMessage());
        }

        sendOtpRepository.save(sendOtp);

        log.debug("Статус SendOtp с ID {} обновлен на: {}",
                kafkaResponse.getId(), sendOtp.getStatus());
    }
}
