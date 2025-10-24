package ru.pozdeev.otp.service;

import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponse;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.model.SendingChannel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface Sender {

    SendOtpKafkaResponse sendToTargetChannel(String otp, SendOtp sendOtp, String message) throws ExecutionException, InterruptedException, TimeoutException;

    SendingChannel getChannel();

    void completeResponse(SendOtpKafkaResponse response);
}
