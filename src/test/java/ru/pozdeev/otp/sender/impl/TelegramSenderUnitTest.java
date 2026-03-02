package ru.pozdeev.otp.sender.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.pozdeev.otp.dto.common.SendingResult;
import ru.pozdeev.otp.dto.common.SendingResultStatus;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponse;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponseStatus;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.kafka.OtpSendKafkaProducer;
import ru.pozdeev.otp.model.SendingChannel;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TelegramSenderUnitTest {

    @Mock
    private OtpSendKafkaProducer kafkaProducer;

    @InjectMocks
    private TelegramSender telegramSender;

    @Nested
    class SendToTargetChannel {

        @Test
        void when_sendToTargetChannel_successResponse_then_returnSuccess() throws Exception {
            ReflectionTestUtils.setField(telegramSender, "maxTimeout", 5000);
            String messageKey = UUID.randomUUID().toString();
            SendOtp sendOtp = new SendOtp();
            sendOtp.setSendMessageKey(messageKey);
            sendOtp.setTarget("chatId");

            SendOtpKafkaResponse kafkaResponse = new SendOtpKafkaResponse();
            kafkaResponse.setId(messageKey);
            kafkaResponse.setStatus(SendOtpKafkaResponseStatus.SUCCESS);

            CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS).execute(() -> {
                telegramSender.completeResponse(kafkaResponse);
            });

            SendingResult result = telegramSender.sendToTargetChannel("123456", sendOtp, "Your code is 123456");

            verify(kafkaProducer).sendMessage(any());
            assertEquals(SendingResultStatus.SUCCESS, result.getStatus());
        }

        @Test
        void when_sendToTargetChannel_timeout_then_returnError() throws Exception {
            ReflectionTestUtils.setField(telegramSender, "maxTimeout", 100);
            String messageKey = UUID.randomUUID().toString();
            SendOtp sendOtp = new SendOtp();
            sendOtp.setSendMessageKey(messageKey);
            sendOtp.setTarget("chatId");

            SendingResult result = telegramSender.sendToTargetChannel("123456", sendOtp, "Your code is 123456");

            assertAll(
                    () -> assertEquals(SendingResultStatus.ERROR, result.getStatus()),
                    () -> assertEquals("Таймаут ожидания ответа от сервиса отправки", result.getErrorMessage())
            );
        }
    }

    @Nested
    class GetChannel {

        @Test
        void when_getChannel_then_returnTelegram() {
            SendingChannel channel = telegramSender.getChannel();

            assertEquals(SendingChannel.TELEGRAM, channel);
        }
    }
}
