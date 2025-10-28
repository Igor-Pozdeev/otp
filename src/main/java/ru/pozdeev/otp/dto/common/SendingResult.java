package ru.pozdeev.otp.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendingResult {

    private SendingResultStatus status;
    private String errorMessage;
}
