package ru.pozdeev.otp.service.impl;

import org.springframework.stereotype.Service;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponse;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.model.SendingChannel;
import ru.pozdeev.otp.service.SendingChannelService;

@Service
public class ConsoleChannel implements SendingChannelService {

    @Override
    public boolean sendToTargetChannel(String otp, SendOtp sendOtp, String message) {
        System.out.println("Одноразовый пароль: " + otp);
        return true;
    }

    @Override
    public SendingChannel getChannel() {
        return SendingChannel.CONSOLE;
    }
    @Override
    public void completeResponse(String messageKey, SendOtpKafkaResponse response) {
        //Имплементация не требуется
    }
}
