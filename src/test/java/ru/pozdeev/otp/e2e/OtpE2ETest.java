package ru.pozdeev.otp.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import ru.pozdeev.otp.dto.common.CommonRequest;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponse;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponseStatus;
import ru.pozdeev.otp.entity.CheckOtp;
import ru.pozdeev.otp.entity.OtpSendStatus;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.model.OtpCheckRequest;
import ru.pozdeev.otp.model.OtpGenerateRequest;
import ru.pozdeev.otp.model.SendingChannel;
import ru.pozdeev.otp.repository.CheckOtpRepository;
import ru.pozdeev.otp.repository.SendOtpRepository;
import ru.pozdeev.otp.util.JsonUtil;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OtpE2ETest extends AbstractE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SendOtpRepository sendOtpRepository;

    @Autowired
    private CheckOtpRepository checkOtpRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private JsonUtil jsonUtil;

    @Test
    @DisplayName("Полный цикл: генерация через CONSOLE и успешная проверка")
    void fullCycle_console_success() throws Exception {
        UUID processId = UUID.randomUUID();

        OtpGenerateRequest generateBody = createValidGenerateRequest(processId, SendingChannel.CONSOLE, "target");
        CommonRequest<OtpGenerateRequest> generateRequest = new CommonRequest<>(generateBody);

        mockMvc.perform(post("/api/v1/otp/generateAndSend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(generateRequest)))
                .andExpect(status().isOk());

        List<SendOtp> sendOtps = sendOtpRepository.findAllByProcessId(processId.toString());
        assertEquals(1, sendOtps.size());
        SendOtp sendOtp = sendOtps.get(0);
        assertEquals(OtpSendStatus.DELIVERED, sendOtp.getStatus());

        OtpCheckRequest checkBody = new OtpCheckRequest(processId, "000000");
        CommonRequest<OtpCheckRequest> checkRequest = new CommonRequest<>(checkBody);

        mockMvc.perform(post("/api/v1/otp/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkRequest)))
                .andExpect(status().isBadRequest());

        List<CheckOtp> checks = checkOtpRepository.findAllByProcessId(processId.toString());
        assertEquals(1, checks.size());
        Assertions.assertFalse(checks.get(0).getCorrect());
    }

    @Test
    @DisplayName("Сценарий TELEGRAM: генерация, ожидание ответа из Kafka и проверка")
    void telegram_scenario() throws Exception {
        UUID processId = UUID.randomUUID();
        String target = "123456789";

        OtpGenerateRequest generateBody = createValidGenerateRequest(processId, SendingChannel.TELEGRAM, target);
        CommonRequest<OtpGenerateRequest> generateRequest = new CommonRequest<>(generateBody);

        new Thread(() -> {
            await().atMost(5, TimeUnit.SECONDS).until(() -> !sendOtpRepository.findAllByProcessId(processId.toString()).isEmpty());
            SendOtp lastOtp = sendOtpRepository.findAllByProcessId(processId.toString()).get(0);

            SendOtpKafkaResponse kafkaResponse = new SendOtpKafkaResponse();
            kafkaResponse.setId(lastOtp.getSendMessageKey());
            kafkaResponse.setStatus(SendOtpKafkaResponseStatus.SUCCESS);

            kafkaTemplate.send("Send.Otp.OUT.V1", jsonUtil.toJson(kafkaResponse));
        }).start();

        mockMvc.perform(post("/api/v1/otp/generateAndSend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(generateRequest)))
                .andExpect(status().isOk());

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            SendOtp sendOtp = sendOtpRepository.findAllByProcessId(processId.toString()).get(0);
            assertEquals(OtpSendStatus.DELIVERED, sendOtp.getStatus());
        });
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
