package ru.pozdeev.otp.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import ru.pozdeev.otp.AbstractTest;
import ru.pozdeev.otp.dto.common.CommonRequest;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponse;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponseStatus;
import ru.pozdeev.otp.entity.CheckOtp;
import ru.pozdeev.otp.entity.OtpSendStatus;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.model.OtpCheckRequest;
import ru.pozdeev.otp.model.OtpGenerateRequest;
import ru.pozdeev.otp.model.SendingChannel;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OtpE2ETest extends AbstractTest {

    @Nested
    class Scenario {
        private static final Logger log = LoggerFactory.getLogger(Scenario.class);

        @Test
        void when_fullCycle_withConsoleAndWrongOtp_then_error() throws Exception {
            UUID processId = UUID.randomUUID();

            OtpGenerateRequest generateBody = createValidGenerateRequest(processId, SendingChannel.CONSOLE, "target");
            CommonRequest<OtpGenerateRequest> generateRequest = new CommonRequest<>(generateBody);

            mockMvc.perform(post("/api/v1/otp/generateAndSend")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(generateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.body").doesNotExist())
                    .andExpect(jsonPath("$.errorMessage").doesNotExist())
                    .andExpect(jsonPath("$.validationErrors").doesNotExist());

            List<SendOtp> sendOtps = sendOtpRepository.findAllByProcessId(processId.toString());
            assertEquals(1, sendOtps.size());
            SendOtp sendOtp = sendOtps.get(0);
            assertEquals(OtpSendStatus.DELIVERED, sendOtp.getStatus());

            OtpCheckRequest checkBody = new OtpCheckRequest(processId, "000000");
            CommonRequest<OtpCheckRequest> checkRequest = new CommonRequest<>(checkBody);

            mockMvc.perform(post("/api/v1/otp/check")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(checkRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.errorMessage", is("Бизнес исключение: Введен неверный OTP")))
                    .andExpect(jsonPath("$.validationErrors").doesNotExist());

            List<CheckOtp> checks = checkOtpRepository.findAllByProcessId(processId.toString());
            assertEquals(1, checks.size());
            Assertions.assertFalse(checks.get(0).getCorrect());
        }

        @Test
        void when_generateTelegram_withKafkaResponse_then_delivered() throws Exception {
            UUID processId = UUID.randomUUID();
            String target = "123456789";

            OtpGenerateRequest generateBody = createValidGenerateRequest(processId, SendingChannel.TELEGRAM, target);
            CommonRequest<OtpGenerateRequest> generateRequest = new CommonRequest<>(generateBody);

            Thread kafkaMockThread = new Thread(() -> {
                try {
                    await().atMost(15, TimeUnit.SECONDS).until(() -> {
                        List<SendOtp> list = sendOtpRepository.findAllByProcessId(processId.toString());
                        return !list.isEmpty() && list.get(0).getSendMessageKey() != null;
                    });

                    SendOtp lastOtp = sendOtpRepository.findAllByProcessId(processId.toString()).get(0);

                    SendOtpKafkaResponse kafkaResponse = new SendOtpKafkaResponse();
                    kafkaResponse.setId(lastOtp.getSendMessageKey());
                    kafkaResponse.setStatus(SendOtpKafkaResponseStatus.SUCCESS);


                    kafkaTemplate.send("Send.Otp.OUT.V1", jsonUtil.toJson(kafkaResponse));
                } catch (Exception e) {
                    log.atError()
                            .setCause(e)
                            .log("Не удалось отправить OTP ответ для процесса " + processId);
                }
            });
            kafkaMockThread.start();

            mockMvc.perform(post("/api/v1/otp/generateAndSend")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(generateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.body").doesNotExist())
                    .andExpect(jsonPath("$.errorMessage").doesNotExist())
                    .andExpect(jsonPath("$.validationErrors").doesNotExist());

            List<SendOtp> listByProcessId = sendOtpRepository.findAllByProcessId(processId.toString());
            Assertions.assertFalse(listByProcessId.isEmpty());
            assertEquals(OtpSendStatus.DELIVERED, listByProcessId.get(0).getStatus());
        }
    }

    private OtpGenerateRequest createValidGenerateRequest(UUID processId, SendingChannel channel, String target) {
        return OtpGenerateRequest.builder()
                .processId(processId)
                .sendingChannel(channel)
                .target(target)
                .message("Code: %s")
                .length(6)
                .ttl(300)
                .sessionTtl(600)
                .resendAttempts(3)
                .resendTimeout(60)
                .build();
    }
}
