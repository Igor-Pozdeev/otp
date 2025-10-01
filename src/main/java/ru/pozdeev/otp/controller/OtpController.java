package ru.pozdeev.otp.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pozdeev.otp.dto.common.CommonRequest;
import ru.pozdeev.otp.model.OtpCheckRequest;
import ru.pozdeev.otp.model.OtpGenerateRequest;

@Slf4j
@RestController
@RequestMapping("/otp/api/v1/otp")
public class OtpController {


    @PostMapping(value = "/generateAndSend")
    public ResponseEntity<Void> generateAndSend(@Valid @RequestBody CommonRequest<OtpGenerateRequest> request) {
        log.debug("Согласно требованиям, пока что только пустой ответ");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check")
    public ResponseEntity<Void> check(@Valid @RequestBody CommonRequest<OtpCheckRequest> request) {
        log.debug("Согласно требованиям, пока что только пустой ответ");
        return ResponseEntity.ok().build();
    }
}
