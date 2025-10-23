package ru.pozdeev.otp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaRequest;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponse;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponseStatus;
import ru.pozdeev.otp.entity.OtpSendStatus;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.exception.OtpException;
import ru.pozdeev.otp.kafka.OtpSendKafkaProducer;
import ru.pozdeev.otp.model.SendingChannel;
import ru.pozdeev.otp.repository.SendOtpRepository;
import ru.pozdeev.otp.service.SendingChannelService;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramChannel implements SendingChannelService {

    private final OtpSendKafkaProducer kafkaProducer;

    private final SendOtpRepository sendOtpRepository;

    private final Map<String, CompletableFuture<SendOtpKafkaResponse>> pendingResponses = new ConcurrentHashMap<>();

    @Override
    public boolean sendToTargetChannel(String otp, SendOtp sendOtp, String message) throws OtpException {
        SendOtpKafkaRequest kafkaRequest = SendOtpKafkaRequest.builder()
                .id(sendOtp.getSendMessageKey())
                .telegramChatId(sendOtp.getTarget())
                .message(message)
                .build();

        CompletableFuture<SendOtpKafkaResponse> responseFuture = new CompletableFuture<>();
        pendingResponses.put(sendOtp.getSendMessageKey(), responseFuture);

        try {
            kafkaProducer.sendMessage(kafkaRequest);
            SendOtpKafkaResponse response = responseFuture.get(5, TimeUnit.SECONDS);

            if (response.getStatus() == SendOtpKafkaResponseStatus.SUCCESS) {
                sendOtp.setStatus(OtpSendStatus.DELIVERED);
                sendOtpRepository.save(sendOtp);
                return true;
            } else {
                sendOtp.setStatus(OtpSendStatus.ERROR);
                sendOtpRepository.save(sendOtp);
                return false;
            }
        } catch (TimeoutException e) {
            pendingResponses.remove(sendOtp.getSendMessageKey());
            sendOtp.setStatus(OtpSendStatus.ERROR);
            sendOtpRepository.save(sendOtp);
            throw new OtpException("Таймаут ожидания ответа от сервиса отправки сообщения", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new OtpException("Ошибка отправки сообщения в кафку", e);
        }
    }

    public void completeResponse(String messageKey, SendOtpKafkaResponse response) {
        CompletableFuture<SendOtpKafkaResponse> future = pendingResponses.remove(messageKey);
        if (future != null) {
            future.complete(response);
        }
    }

    @Override
    public SendingChannel getChannel() {
        return SendingChannel.TELEGRAM;
    }
}
