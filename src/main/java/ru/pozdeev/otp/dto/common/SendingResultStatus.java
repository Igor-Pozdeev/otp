package ru.pozdeev.otp.dto.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.pozdeev.otp.dto.kafka.sendotp.SendOtpKafkaResponseStatus;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum SendingResultStatus {
    SUCCESS(SendOtpKafkaResponseStatus.SUCCESS),
    ERROR(SendOtpKafkaResponseStatus.ERROR);

    private final SendOtpKafkaResponseStatus sendOtpKafkaResponseStatus;

    public static SendingResultStatus getStatus(SendOtpKafkaResponseStatus status) {
        return Arrays.stream(values())
                .filter(resultStatus -> resultStatus.getSendOtpKafkaResponseStatus() == status)
                .findFirst()
                .orElse(null);
    }
}
