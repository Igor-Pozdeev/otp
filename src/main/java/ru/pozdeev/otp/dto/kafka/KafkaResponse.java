package ru.pozdeev.otp.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaResponse {

    private String id;

    private KafkaResponseStatus status;

    private String errorMessage;
}
