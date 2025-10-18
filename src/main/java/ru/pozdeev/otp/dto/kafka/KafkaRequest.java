package ru.pozdeev.otp.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaRequest {

    private String id;

    private String telegramChatId;

    private String message;
}
