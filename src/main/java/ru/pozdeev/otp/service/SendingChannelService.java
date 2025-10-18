package ru.pozdeev.otp.service;

import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.model.SendingChannel;

public interface SendingChannelService {

    void sendToTargetChannel(String otp, SendOtp sendOtp, String message);

    SendingChannel getChannel();
}
