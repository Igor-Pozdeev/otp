package ru.pozdeev.otp.dto.kafka.sendotp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendOtpKafkaResponse {

    private String id;

    private SendOtpKafkaResponseStatus status;

    private String errorMessage;
}
