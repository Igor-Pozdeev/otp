package ru.pozdeev.otp.util;

import ru.pozdeev.otp.model.OtpGenerateRequest;

import java.util.UUID;

public class TestRequests {

    private TestRequests() {
    }

    public static OtpGenerateRequest defaultOtpGenerateRequest() {
        return otpGenerateRequestWithLength(6);
    }

    public static OtpGenerateRequest otpGenerateRequestWithLength(int length) {
        return new OtpGenerateRequest(
                UUID.randomUUID(),
                "console",
                "ignored",
                "Your code: %s",
                length,
                60,
                120,
                2,
                45
        );
    }
}
