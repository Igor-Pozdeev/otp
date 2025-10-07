package ru.pozdeev.otp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.pozdeev.otp.dto.common.CommonRequest;
import ru.pozdeev.otp.model.OtpCheckRequest;
import ru.pozdeev.otp.model.OtpGenerateRequest;
import ru.pozdeev.otp.model.SendingChannel;
import ru.pozdeev.otp.testutil.TestRequestsUtil;

import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OtpController.class)
class OtpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Метод generateAndSend получает валидный запрос и возвращает статус OK")
    void when_generateAndSend_takesValidRequest_then_ok() throws Exception {
        OtpGenerateRequest body = TestRequestsUtil.defaultOtpGenerateRequest();

        CommonRequest<OtpGenerateRequest> request = CommonRequest.<OtpGenerateRequest>builder()
                .body(body)
                .build();

        mockMvc.perform(post("/api/v1/otp/generateAndSend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Метод generateAndSend получает body == null -> возвращается статус 400")
    void when_generateAndSend_bodyIsNull_then_badRequest() throws Exception {
        CommonRequest<OtpGenerateRequest> request = CommonRequest.<OtpGenerateRequest>builder()
                .body(null)
                .build();

        mockMvc.perform(post("/api/v1/otp/generateAndSend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource(value = "generateAndSendProvider")
    @DisplayName("Параметризованный тест для generateAndSend: валидация всех полей")
    void when_generateAndSend_parametrized_then_badRequest(OtpGenerateRequest otpGenerateRequest, String pointer, String message) throws Exception {
        CommonRequest<OtpGenerateRequest> request = CommonRequest.<OtpGenerateRequest>builder()
                .body(otpGenerateRequest)
                .build();

        mockMvc.perform(post("/api/v1/otp/generateAndSend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Ошибка валидации"))
                .andExpect(jsonPath("$.validationErrors[0].field").value(pointer))
                .andExpect(jsonPath("$.validationErrors[0].message").value(message));
    }

    @Test
    @DisplayName("Метод check получает body == null -> возвращается статус 400")
    void when_check_bodyIsNull_then_badRequest() throws Exception {
        CommonRequest<OtpCheckRequest> request = CommonRequest.<OtpCheckRequest>builder()
                .body(null)
                .build();

        mockMvc.perform(post("/api/v1/otp/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("checkProvider")
    @DisplayName("Параметризованный тест для check: валидация всех полей")
    void when_check_parametrized_then_badRequest(OtpCheckRequest checkRequest) throws Exception {
        CommonRequest<OtpCheckRequest> request = CommonRequest.<OtpCheckRequest>builder()
                .body(checkRequest)
                .build();

        mockMvc.perform(post("/api/v1/otp/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> generateAndSendProvider() {
        return Stream.of(Arguments.of(OtpGenerateRequest.builder()
                        .processId(UUID.randomUUID())
                        .length(4)
                        .sendingChannel(SendingChannel.CONSOLE)
                        .message("message")
                        .resendTimeout(30)
                        .resendAttempts(2)
                        .target("target")
                        .sessionTtl(120).build(), "body.ttl", "must not be null"),
                Arguments.of(OtpGenerateRequest.builder()
                        .processId(UUID.randomUUID())
                        .sendingChannel(SendingChannel.CONSOLE)
                        .message("message")
                        .resendTimeout(30)
                        .resendAttempts(2)
                        .target("target")
                        .sessionTtl(120)
                        .ttl(31).build(), "body.length", "must not be null"),
                Arguments.of(OtpGenerateRequest.builder()
                        .processId(UUID.randomUUID())
                        .length(13)
                        .sendingChannel(SendingChannel.CONSOLE)
                        .message("message")
                        .resendTimeout(30)
                        .resendAttempts(2)
                        .target("target")
                        .sessionTtl(120)
                        .ttl(31).build(), "body.length", "must be between 4 and 12")
        );
    }

    private static Stream<Arguments> checkProvider() {
        return Stream.of(Arguments.of(new OtpCheckRequest(null, null)),
                Arguments.of(new OtpCheckRequest(null, " ")),
                Arguments.of(new OtpCheckRequest(null, "")));
    }
}


