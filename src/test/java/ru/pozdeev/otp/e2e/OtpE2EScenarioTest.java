package ru.pozdeev.otp.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("E2E сценарии работы OTP сервиса")
class OtpE2EScenarioTest extends AbstractE2ETest {

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
    @DisplayName("Сценарий 1: Генерация OTP через CONSOLE и проверка с неверным кодом")
    void scenario_generateConsole_and_checkWithWrongOtp() throws Exception {
        UUID processId = UUID.randomUUID();

        OtpGenerateRequest generateBody = createValidGenerateRequest(processId, SendingChannel.CONSOLE, "console_target");
        CommonRequest<OtpGenerateRequest> generateRequest = new CommonRequest<>(generateBody);

        mockMvc.perform(post("/api/v1/otp/generateAndSend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(generateRequest)))
                .andExpect(status().isOk());

        List<SendOtp> sendOtps = sendOtpRepository.findAllByProcessId(processId.toString());
        assertEquals(1, sendOtps.size(), "Должна быть создана одна запись SendOtp");
        SendOtp sendOtp = sendOtps.get(0);
        assertEquals(OtpSendStatus.DELIVERED, sendOtp.getStatus(), "Статус для CONSOLE должен быть DELIVERED");

        OtpCheckRequest checkBody = new OtpCheckRequest(processId, "WRONG_OTP");
        CommonRequest<OtpCheckRequest> checkRequest = new CommonRequest<>(checkBody);

        mockMvc.perform(post("/api/v1/otp/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkRequest)))
                .andExpect(status().isBadRequest());

        List<CheckOtp> checks = checkOtpRepository.findAllByProcessId(processId.toString());
        assertEquals(1, checks.size(), "Должна быть создана одна запись CheckOtp");
        assertFalse(checks.get(0).getCorrect(), "Результат проверки должен быть отрицательным");
    }

    @Test
    @DisplayName("Сценарий 2: Генерация OTP через TELEGRAM с имитацией ответа от Kafka")
    void scenario_generateTelegram_withKafkaResponse() throws Exception {
        UUID processId = UUID.randomUUID();
        String target = "telegram_chat_id";

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

                Thread.sleep(1000);

                System.out.println("E2E Test: Sending mock response to Kafka for ID: " + lastOtp.getSendMessageKey());
                kafkaTemplate.send("Send.Otp.OUT.V1", jsonUtil.toJson(kafkaResponse));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        kafkaMockThread.start();

        mockMvc.perform(post("/api/v1/otp/generateAndSend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(generateRequest)))
                .andExpect(status().isOk());

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            List<SendOtp> list = sendOtpRepository.findAllByProcessId(processId.toString());
            assertFalse(list.isEmpty());
            assertEquals(OtpSendStatus.DELIVERED, list.get(0).getStatus(), "Статус должен быть DELIVERED");
        });

        kafkaMockThread.join(5000);
    }

    private OtpGenerateRequest createValidGenerateRequest(UUID processId, SendingChannel channel, String target) {
        return OtpGenerateRequest.builder()
                .processId(processId)
                .sendingChannel(channel)
                .target(target)
                .message("Ваш код: %s")
                .length(6)
                .ttl(300)
                .sessionTtl(600)
                .resendAttempts(3)
                .resendTimeout(60)
                .build();
    }
}
