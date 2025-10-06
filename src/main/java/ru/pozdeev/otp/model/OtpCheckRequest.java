package ru.pozdeev.otp.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpCheckRequest {

    @NotBlank
    private UUID processId;
    @NotBlank
    private String otp;
}
