package ru.pozdeev.otp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.pozdeev.otp.dto.common.CommonRequest;
import ru.pozdeev.otp.model.OtpCheckRequest;
import ru.pozdeev.otp.model.OtpGenerateRequest;
import ru.pozdeev.otp.model.SendingChannel;
import ru.pozdeev.otp.service.OtpService;

import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OtpController.class)
class OtpValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OtpService otpService;

    @Nested
    @DisplayName("Валидация запроса генерации OTP")
    class GenerateAndSendValidation {

        @Test
        @DisplayName("Успешная валидация корректного запроса")
        void when_validRequest_then_returnOk() throws Exception {
            OtpGenerateRequest body = createValidGenerateRequest();
            CommonRequest<OtpGenerateRequest> request = new CommonRequest<>(body);

            mockMvc.perform(post("/api/v1/otp/generateAndSend")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.errorMessage").doesNotExist());
        }

        @ParameterizedTest
        @MethodSource("invalidGenerateRequests")
        @DisplayName("Ошибка валидации некорректного запроса")
        void when_invalidRequest_then_returnBadRequest(OtpGenerateRequest body, String expectedField) throws Exception {
            CommonRequest<OtpGenerateRequest> request = new CommonRequest<>(body);

            mockMvc.perform(post("/api/v1/otp/generateAndSend")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorMessage").value("Ошибка валидации"))
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'body.%s')]".formatted(expectedField)).exists());
        }

        static Stream<Arguments> invalidGenerateRequests() {
            return Stream.of(
                    Arguments.of(createValidGenerateRequestBuilder().processId(null).build(), "processId"),
                    Arguments.of(createValidGenerateRequestBuilder().sendingChannel(null).build(), "sendingChannel"),
                    Arguments.of(createValidGenerateRequestBuilder().target(null).build(), "target"),
                    Arguments.of(createValidGenerateRequestBuilder().target("").build(), "target"),
                    Arguments.of(createValidGenerateRequestBuilder().message(null).build(), "message"),
                    Arguments.of(createValidGenerateRequestBuilder().message(" ").build(), "message"),
                    Arguments.of(createValidGenerateRequestBuilder().length(3).build(), "length"),
                    Arguments.of(createValidGenerateRequestBuilder().length(13).build(), "length"),
                    Arguments.of(createValidGenerateRequestBuilder().length(null).build(), "length"),
                    Arguments.of(createValidGenerateRequestBuilder().ttl(29).build(), "ttl"),
                    Arguments.of(createValidGenerateRequestBuilder().ttl(null).build(), "ttl"),
                    Arguments.of(createValidGenerateRequestBuilder().sessionTtl(59).build(), "sessionTtl"),
                    Arguments.of(createValidGenerateRequestBuilder().sessionTtl(null).build(), "sessionTtl"),
                    Arguments.of(createValidGenerateRequestBuilder().resendAttempts(0).build(), "resendAttempts"),
                    Arguments.of(createValidGenerateRequestBuilder().resendAttempts(4).build(), "resendAttempts"),
                    Arguments.of(createValidGenerateRequestBuilder().resendAttempts(null).build(), "resendAttempts"),
                    Arguments.of(createValidGenerateRequestBuilder().resendTimeout(29).build(), "resendTimeout"),
                    Arguments.of(createValidGenerateRequestBuilder().resendTimeout(null).build(), "resendTimeout")
            );
        }

        private static OtpGenerateRequest createValidGenerateRequest() {
            return createValidGenerateRequestBuilder().build();
        }

        private static OtpGenerateRequest.OtpGenerateRequestBuilder createValidGenerateRequestBuilder() {
            return OtpGenerateRequest.builder()
                    .processId(UUID.randomUUID())
                    .sendingChannel(SendingChannel.CONSOLE)
                    .target("test@example.com")
                    .message("Code: %s")
                    .length(6)
                    .ttl(300)
                    .sessionTtl(600)
                    .resendAttempts(3)
                    .resendTimeout(60);
        }
    }

    @Nested
    @DisplayName("Валидация запроса проверки OTP")
    class CheckValidation {

        @Test
        @DisplayName("Успешная валидация корректного запроса")
        void when_validRequest_then_returnOk() throws Exception {
            OtpCheckRequest body = new OtpCheckRequest(UUID.randomUUID(), "123456");
            CommonRequest<OtpCheckRequest> request = new CommonRequest<>(body);

            mockMvc.perform(post("/api/v1/otp/check")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists());
        }

        @ParameterizedTest
        @MethodSource("invalidCheckRequests")
        @DisplayName("Ошибка валидации некорректного запроса")
        void when_invalidRequest_then_returnBadRequest(OtpCheckRequest body, String expectedField) throws Exception {
            CommonRequest<OtpCheckRequest> request = new CommonRequest<>(body);

            mockMvc.perform(post("/api/v1/otp/check")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorMessage").value("Ошибка валидации"))
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'body.%s')]".formatted(expectedField)).exists());
        }

        static Stream<Arguments> invalidCheckRequests() {
            return Stream.of(
                    Arguments.of(new OtpCheckRequest(null, "123456"), "processId"),
                    Arguments.of(new OtpCheckRequest(UUID.randomUUID(), null), "otp"),
                    Arguments.of(new OtpCheckRequest(UUID.randomUUID(), ""), "otp"),
                    Arguments.of(new OtpCheckRequest(UUID.randomUUID(), " "), "otp")
            );
        }
    }

    @Test
    @DisplayName("Ошибка если тело запроса null")
    void when_bodyIsNull_then_returnBadRequest() throws Exception {
        CommonRequest<OtpCheckRequest> request = new CommonRequest<>(null);

        mockMvc.perform(post("/api/v1/otp/check")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Ошибка валидации"))
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'body')]").exists());
    }

    @Test
    @DisplayName("Ошибка при передаче невалидного значения в Enum")
    void when_invalidEnumValue_then_returnBadRequest() throws Exception {
        String jsonWithInvalidEnum = """
                {
                    "body": {
                        "processId": "%s",
                        "sendingChannel": "INVALID_CHANNEL",
                        "target": "target",
                        "message": "message",
                        "length": 6,
                        "ttl": 300,
                        "sessionTtl": 600,
                        "resendAttempts": 3,
                        "resendTimeout": 60
                    }
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/otp/generateAndSend")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonWithInvalidEnum))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.containsString("Ошибка валидации, указан некорректный формат поля 'body.sendingChannel'")));
    }
}
