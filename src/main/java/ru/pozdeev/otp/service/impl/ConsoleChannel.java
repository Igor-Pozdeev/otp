package ru.pozdeev.otp.service.impl;

import org.springframework.stereotype.Service;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponse;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.model.SendingChannel;
import ru.pozdeev.otp.service.Sender;

@Service
public class ConsoleChannel implements Sender {

    @Override
    public SendOtpKafkaResponse sendToTargetChannel(String otp, SendOtp sendOtp, String message) {
        System.out.println("Одноразовый пароль: " + otp);
        // Для вывода в консоль ответ из kafka не нужен
        return null;
    }

    @Override
    public SendingChannel getChannel() {
        return SendingChannel.CONSOLE;
    }

    @Override
    public void completeResponse(SendOtpKafkaResponse response) {
        //Имплементация не требуется
    }
}
