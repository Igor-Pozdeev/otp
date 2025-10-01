package ru.pozdeev.otp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.pozdeev.otp.dto.common.CommonRequest;
import ru.pozdeev.otp.model.OtpCheckRequest;
import ru.pozdeev.otp.model.OtpGenerateRequest;
import ru.pozdeev.otp.util.TestRequests;

import java.util.UUID;

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

        mockMvc.perform(post("/otp/api/v1/otp/generateAndSend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Метод generateAndSend получает невалидный запрос и возвращает статус 400")
    void when_generateAndSend_validationError_then_badRequest() throws Exception {
        OtpGenerateRequest body = TestRequests.otpGenerateRequestWithLength(3);

        CommonRequest<OtpGenerateRequest> request = CommonRequest.<OtpGenerateRequest>builder()
                .body(body)
                .build();

        mockMvc.perform(post("/otp/api/v1/otp/generateAndSend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Метод check получает валидный запрос и возвращает статус OK")
    void when_check_takesvalidRequest_then_ok() throws Exception {
        OtpCheckRequest body = new OtpCheckRequest(UUID.randomUUID(), "123456");
        CommonRequest<OtpCheckRequest> request = CommonRequest.<OtpCheckRequest>builder()
                .body(body)
                .build();

        mockMvc.perform(post("/otp/api/v1/otp/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}


