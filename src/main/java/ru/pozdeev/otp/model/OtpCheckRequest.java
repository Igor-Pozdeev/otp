package ru.pozdeev.otp.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpCheckRequest {

    @NotNull
    private UUID processId;
    @NotBlank
    private String otp;
}
