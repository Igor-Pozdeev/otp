package ru.pozdeev.otp.sender.impl;

import org.springframework.stereotype.Service;
import ru.pozdeev.otp.dto.common.SendingResult;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.model.SendingChannel;
import ru.pozdeev.otp.sender.Sender;

@Service
public class ConsoleSender implements Sender<Void> {

    @Override
    public SendingResult sendToTargetChannel(String otp, SendOtp sendOtp, String message) {
        System.out.println("Одноразовый пароль: " + otp);
        // Для вывода в консоль ответ из kafka не нужен
        return null;
    }

    @Override
    public SendingChannel getChannel() {
        return SendingChannel.CONSOLE;
    }

    @Override
    public void completeResponse(Void response) {
        //Имплементация не требуется
    }
}
