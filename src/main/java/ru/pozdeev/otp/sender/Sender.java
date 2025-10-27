package ru.pozdeev.otp.sender;

import ru.pozdeev.otp.dto.common.CommonResponse;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.model.SendingChannel;

import java.util.concurrent.ExecutionException;

public interface Sender<T> {

    CommonResponse<T> sendToTargetChannel(String otp, SendOtp sendOtp, String message) throws ExecutionException, InterruptedException;

    SendingChannel getChannel();

    void completeResponse(T response);
}
