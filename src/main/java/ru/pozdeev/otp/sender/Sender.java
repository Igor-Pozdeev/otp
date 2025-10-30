package ru.pozdeev.otp.sender;

import ru.pozdeev.otp.dto.common.SendingResult;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.model.SendingChannel;

import java.util.concurrent.ExecutionException;

public interface Sender<T> {

    SendingResult sendToTargetChannel(String otp, SendOtp sendOtp, String message) throws ExecutionException, InterruptedException;

    SendingChannel getChannel();

    void completeResponse(T response);
}
