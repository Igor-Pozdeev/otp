package ru.pozdeev.otp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.pozdeev.otp.AbstractTest;
import ru.pozdeev.otp.dto.common.CommonRequest;
import ru.pozdeev.otp.model.OtpCheckRequest;
import ru.pozdeev.otp.model.OtpGenerateRequest;
import ru.pozdeev.otp.model.SendingChannel;
import ru.pozdeev.otp.service.OtpService;
import ru.pozdeev.otp.testutil.TestRequestsUtil;

import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OtpControllerTest extends AbstractTest {

    @MockitoBean
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        doNothing().when(otpService).generateAndSend(any());
        doNothing().when(otpService).check(any());
    }

    @Nested
    class GenerateAndSend {

        @Test
        void when_generateAndSend_withValidRequest_then_ok() throws Exception {
            OtpGenerateRequest body = TestRequestsUtil.defaultOtpGenerateRequest();
            CommonRequest<OtpGenerateRequest> request = new CommonRequest<>(body);

            mockMvc.perform(post("/api/v1/otp/generateAndSend")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.body").doesNotExist())
                    .andExpect(jsonPath("$.errorMessage").doesNotExist())
                    .andExpect(jsonPath("$.validationErrors").doesNotExist());
        }

        @Test
        void when_generateAndSend_withNullBody_then_badRequest() throws Exception {
            CommonRequest<OtpGenerateRequest> request = CommonRequest.<OtpGenerateRequest>builder()
                    .body(null)
                    .build();

            mockMvc.perform(post("/api/v1/otp/generateAndSend")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorMessage").value("Ошибка валидации"))
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'body')]").exists());
        }

        @ParameterizedTest
        @MethodSource("invalidGenerateRequests")
        void when_generateAndSend_withInvalidRequest_then_returnBadRequest(OtpGenerateRequest body, String expectedField) throws Exception {
            CommonRequest<OtpGenerateRequest> request = new CommonRequest<>(body);

            mockMvc.perform(post("/api/v1/otp/generateAndSend")
                            .contentType(MediaType.APPLICATION_JSON)
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

        @Test
        void when_generateAndSend_withInvalidEnumValue_then_returnBadRequest() throws Exception {
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
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonWithInvalidEnum))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorMessage").value(containsString("Ошибка валидации, указан некорректный формат поля 'body.sendingChannel'")));
        }
    }

    @Nested
    class Check {

        @Test
        void when_check_withValidRequest_then_ok() throws Exception {
            OtpCheckRequest body = new OtpCheckRequest(UUID.randomUUID(), "123456");
            CommonRequest<OtpCheckRequest> request = new CommonRequest<>(body);

            mockMvc.perform(post("/api/v1/otp/check")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.body").doesNotExist())
                    .andExpect(jsonPath("$.errorMessage").doesNotExist())
                    .andExpect(jsonPath("$.validationErrors").doesNotExist());
        }

        @ParameterizedTest
        @MethodSource("invalidCheckRequests")
        void when_check_withInvalidRequest_then_returnBadRequest(OtpCheckRequest body, String expectedField) throws Exception {
            CommonRequest<OtpCheckRequest> request = new CommonRequest<>(body);

            mockMvc.perform(post("/api/v1/otp/check")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorMessage").value("Ошибка валидации"))
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'body.%s')]".formatted(expectedField)).exists());
        }

        @Test
        void when_check_withNullBody_then_returnBadRequest() throws Exception {
            CommonRequest<OtpCheckRequest> request = new CommonRequest<>(null);

            mockMvc.perform(post("/api/v1/otp/check")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorMessage").value("Ошибка валидации"))
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'body')]").exists());
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
}
