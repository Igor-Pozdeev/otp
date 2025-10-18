package ru.pozdeev.otp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pozdeev.otp.dto.kafka.KafkaRequest;
import ru.pozdeev.otp.entity.OtpSendStatus;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.exception.OtpException;
import ru.pozdeev.otp.kafka.KafkaProducer;
import ru.pozdeev.otp.model.SendingChannel;
import ru.pozdeev.otp.repository.SendOtpRepository;
import ru.pozdeev.otp.service.SendingChannelService;

import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramChannel implements SendingChannelService {

    private final KafkaProducer kafkaProducer;

    private final SendOtpRepository sendOtpRepository;

    @Override
    public void sendToTargetChannel(String otp, SendOtp sendOtp, String message) {
        KafkaRequest kafkaRequest = KafkaRequest.builder()
                .id(sendOtp.getSendMessageKey())
                .telegramChatId(sendOtp.getTarget())
                .message(message)
                .build();

        try {
            kafkaProducer.sendMessage(kafkaRequest);
        } catch (TimeoutException e) {
            sendOtp.setStatus(OtpSendStatus.ERROR);
            sendOtpRepository.saveAndFlush(sendOtp);
            throw new OtpException("Таймаут ожидания ответа от сервиса отправки сообщения", e);
        }
    }

    @Override
    public SendingChannel getChannel() {
        return SendingChannel.TELEGRAM;
    }
}
