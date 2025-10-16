package ru.pozdeev.otp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pozdeev.otp.dto.common.CommonRequest;
import ru.pozdeev.otp.dto.common.CommonResponse;
import ru.pozdeev.otp.model.OtpCheckRequest;
import ru.pozdeev.otp.model.OtpGenerateRequest;
import ru.pozdeev.otp.service.OtpService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/otp")
public class OtpController {

    private final OtpService otpService;

    @PostMapping(value = "/generateAndSend")
    public CommonResponse<Void> generateAndSend(@Valid @RequestBody CommonRequest<OtpGenerateRequest> request) {
        otpService.generateAndSend(request.getBody());

        return CommonResponse.<Void>builder()
                .id(UUID.randomUUID())
                .build();
    }

    @PostMapping("/check")
    public CommonResponse<Void> check(@Valid @RequestBody CommonRequest<OtpCheckRequest> request) {
        otpService.check(request.getBody());

        return CommonResponse.<Void>builder()
                .id(UUID.randomUUID())
                .build();
    }
}
