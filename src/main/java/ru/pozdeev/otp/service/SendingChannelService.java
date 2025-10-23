package ru.pozdeev.otp.service;

import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponse;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.model.SendingChannel;

public interface SendingChannelService {

    boolean sendToTargetChannel(String otp, SendOtp sendOtp, String message);

    SendingChannel getChannel();

    void completeResponse(String messageKey, SendOtpKafkaResponse response);
}
