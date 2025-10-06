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
import ru.pozdeev.otp.util.TestRequests;

import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        OtpGenerateRequest body = TestRequests.defaultOtpGenerateRequest();

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
    void when_generateAndSend_parametrized_then_badRequest(OtpGenerateRequest otpGenerateRequest) throws Exception {
        CommonRequest<OtpGenerateRequest> request = CommonRequest.<OtpGenerateRequest>builder()
                .body(otpGenerateRequest)
                .build();

        mockMvc.perform(post("/api/v1/otp/generateAndSend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> generateAndSendProvider() {
        return Stream.of(
                Arguments.of(
                        new OtpGenerateRequest(
                                null,
                                SendingChannel.CONSOLE,
                                " ",
                                "Your code: %s",
                                6,
                                60,
                                120,
                                2,
                                45)
                ),
                Arguments.of(
                        new OtpGenerateRequest(
                                null,
                                SendingChannel.CONSOLE,
                                null,
                                "Your code: %s",
                                6,
                                60,
                                120,
                                2,
                                45)
                ),
                Arguments.of(
                        new OtpGenerateRequest(
                                null,
                                SendingChannel.CONSOLE,
                                "null",
                                " ",
                                6,
                                60,
                                120,
                                2,
                                45)
                ),
                Arguments.of(
                        new OtpGenerateRequest(
                                null,
                                SendingChannel.CONSOLE,
                                "null",
                                null,
                                6,
                                60,
                                120,
                                2,
                                45)
                ),
                Arguments.of(
                        new OtpGenerateRequest(
                                null,
                                SendingChannel.CONSOLE,
                                "null",
                                "null",
                                13,
                                60,
                                120,
                                2,
                                45)
                ),
                Arguments.of(
                        new OtpGenerateRequest(
                                null,
                                SendingChannel.CONSOLE,
                                "null",
                                "null",
                                null,
                                60,
                                120,
                                2,
                                45)
                ),
                Arguments.of(
                        new OtpGenerateRequest(
                                null,
                                SendingChannel.CONSOLE,
                                "null",
                                "null",
                                10,
                                29,
                                120,
                                2,
                                45)
                ),
                Arguments.of(
                        new OtpGenerateRequest(
                                null,
                                SendingChannel.CONSOLE,
                                "null",
                                "null",
                                3,
                                60,
                                null,
                                2,
                                45)
                )
        );
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

    private static Stream<Arguments> checkProvider() {
        return Stream.of(
                Arguments.of(
                        new OtpCheckRequest(
                                null,
                                null
                        )
                ),
                Arguments.of(
                        new OtpCheckRequest(
                                null,
                                " "
                        )
                ),
                Arguments.of(
                        new OtpCheckRequest(
                                null,
                                ""
                        )
                )
        );
    }

    @Test
    @DisplayName("Метод check получает валидный запрос и возвращает статус OK")
    void when_check_takesvalidRequest_then_ok() throws Exception {
        OtpCheckRequest body = new OtpCheckRequest(UUID.randomUUID(), "123456");
        CommonRequest<OtpCheckRequest> request = CommonRequest.<OtpCheckRequest>builder()
                .body(body)
                .build();

        mockMvc.perform(post("/api/v1/otp/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}


