package ru.pozdeev.otp.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.pozdeev.otp.dto.common.CommonRequest;
import ru.pozdeev.otp.dto.common.CommonResponse;
import ru.pozdeev.otp.model.OtpCheckRequest;
import ru.pozdeev.otp.model.OtpGenerateRequest;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/otp")
public class OtpController {

    @PostMapping(value = "/generateAndSend")
    @ResponseStatus(HttpStatus.OK)
    public CommonResponse<Void> generateAndSend(@Valid @RequestBody CommonRequest<OtpGenerateRequest> request) {
        log.debug("Согласно требованиям, пока что только пустой ответ");
        return CommonResponse.<Void>builder()
                .id(UUID.randomUUID())
                .build();
    }

    @PostMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    public CommonResponse<Void> check(@Valid @RequestBody CommonRequest<OtpCheckRequest> request) {
        log.debug("Согласно требованиям, пока что только пустой ответ");
        return CommonResponse.<Void>builder()
                .id(UUID.randomUUID())
                .build();
    }
}
