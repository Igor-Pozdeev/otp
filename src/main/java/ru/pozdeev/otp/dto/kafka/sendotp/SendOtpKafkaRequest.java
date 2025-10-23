package ru.pozdeev.otp.dto.kafka.sendotp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendOtpKafkaRequest {

    private String id;

    private String telegramChatId;

    private String message;
}
