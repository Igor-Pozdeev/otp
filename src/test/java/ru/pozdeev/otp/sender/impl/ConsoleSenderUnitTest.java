package ru.pozdeev.otp.sender.impl;

import org.junit.jupiter.api.Test;
import ru.pozdeev.otp.dto.common.SendingResult;
import ru.pozdeev.otp.dto.common.SendingResultStatus;
import ru.pozdeev.otp.entity.SendOtp;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsoleSenderUnitTest {

    private final ConsoleSender consoleSender = new ConsoleSender();

    @Test
    void when_sendToTargetChannel_always_then_success() {
        String otp = "123456";
        SendOtp sendOtp = new SendOtp();
        String message = "Your code is 123456";

        SendingResult result = consoleSender.sendToTargetChannel(otp, sendOtp, message);

        assertEquals(SendingResultStatus.SUCCESS, result.getStatus());
    }
}
