package ru.pozdeev.otp.service;

import ru.pozdeev.otp.model.OtpCheckRequest;
import ru.pozdeev.otp.model.OtpGenerateRequest;

public interface OtpService {

    void generateAndSend(OtpGenerateRequest request);

    void check(OtpCheckRequest request);
}
