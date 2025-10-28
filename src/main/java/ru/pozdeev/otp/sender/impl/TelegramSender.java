package ru.pozdeev.otp.sender.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.pozdeev.otp.dto.common.SendingResult;
import ru.pozdeev.otp.dto.common.SendingResultStatus;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaRequest;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponse;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.kafka.OtpSendKafkaProducer;
import ru.pozdeev.otp.model.SendingChannel;
import ru.pozdeev.otp.sender.Sender;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramSender implements Sender<SendOtpKafkaResponse> {

    private final OtpSendKafkaProducer kafkaProducer;

    @Value("${otp.kafka.send-otp.telegram-channel-max-timeout-ms}")
    private Integer maxTimeout;

    private final Map<String, CompletableFuture<SendOtpKafkaResponse>> pendingResponses = new ConcurrentHashMap<>();

    @Override
    public SendingResult sendToTargetChannel(String otp, SendOtp sendOtp, String message) throws ExecutionException, InterruptedException {
        SendOtpKafkaRequest kafkaRequest = SendOtpKafkaRequest.builder()
                .id(sendOtp.getSendMessageKey())
                .telegramChatId(sendOtp.getTarget())
                .message(message)
                .build();

        CompletableFuture<SendOtpKafkaResponse> responseFuture = new CompletableFuture<>();
        pendingResponses.put(sendOtp.getSendMessageKey(), responseFuture);
        try {
            kafkaProducer.sendMessage(kafkaRequest);
            SendOtpKafkaResponse sendOtpKafkaResponse = responseFuture.get(maxTimeout, TimeUnit.MILLISECONDS);
            log.info("Получен ответ от сервиса отправки ОТР в телеграм. Ответ: {}", sendOtpKafkaResponse);

            return SendingResult.builder()
                    .status(SendingResultStatus.getStatus(sendOtpKafkaResponse.getStatus()))
                    .errorMessage(sendOtpKafkaResponse.getErrorMessage())
                    .build();
        } catch (TimeoutException e) {
            log.warn("Таймаут ожидания ответа от сервиса отправки OTP в телеграм: {}", sendOtp.getSendMessageKey());

            return SendingResult.builder()
                    .status(SendingResultStatus.ERROR)
                    .errorMessage("Таймаут ожидания ответа от сервиса отправки")
                    .build();
        }
    }

    public void completeResponse(SendOtpKafkaResponse response) {
        CompletableFuture<SendOtpKafkaResponse> future = pendingResponses.remove(response.getId());
        if (future != null) {
            future.complete(response);
        }
    }

    @Override
    public SendingChannel getChannel() {
        return SendingChannel.TELEGRAM;
    }
}
