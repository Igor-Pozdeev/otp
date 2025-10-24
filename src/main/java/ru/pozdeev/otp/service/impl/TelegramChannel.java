package ru.pozdeev.otp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaRequest;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponse;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.kafka.OtpSendKafkaProducer;
import ru.pozdeev.otp.model.SendingChannel;
import ru.pozdeev.otp.properties.OtpProperty;
import ru.pozdeev.otp.service.Sender;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramChannel implements Sender {

    private final OtpSendKafkaProducer kafkaProducer;

    private final OtpProperty property;

    private final Map<String, CompletableFuture<SendOtpKafkaResponse>> pendingResponses = new ConcurrentHashMap<>();

    @Override
    public SendOtpKafkaResponse sendToTargetChannel(String otp, SendOtp sendOtp, String message) throws ExecutionException, InterruptedException, TimeoutException {
        SendOtpKafkaRequest kafkaRequest = SendOtpKafkaRequest.builder()
                .id(sendOtp.getSendMessageKey())
                .telegramChatId(sendOtp.getTarget())
                .message(message)
                .build();

        CompletableFuture<SendOtpKafkaResponse> responseFuture = new CompletableFuture<>();
        pendingResponses.put(sendOtp.getSendMessageKey(), responseFuture);

        Integer maxTimeout = property.getTelegramChannelMaxTimeoutMs();
        kafkaProducer.sendMessage(kafkaRequest);

        return responseFuture.get(maxTimeout, TimeUnit.MILLISECONDS);
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
