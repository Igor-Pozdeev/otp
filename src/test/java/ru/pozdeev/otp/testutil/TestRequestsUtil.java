package ru.pozdeev.otp.testutil;

import ru.pozdeev.otp.model.OtpGenerateRequest;
import ru.pozdeev.otp.model.SendingChannel;

import java.util.UUID;

public class TestRequestsUtil {

    public static OtpGenerateRequest defaultOtpGenerateRequest() {
        return otpGenerateRequestWithLength(6);
    }

    public static OtpGenerateRequest otpGenerateRequestWithLength(int length) {
        return new OtpGenerateRequest(
                UUID.randomUUID(),
                SendingChannel.TELEGRAM,
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
