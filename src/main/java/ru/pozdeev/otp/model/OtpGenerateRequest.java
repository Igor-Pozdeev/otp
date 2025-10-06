package ru.pozdeev.otp.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpGenerateRequest {

    @NotBlank
    private UUID processId;

    private SendingChannel sendingChannel;

    @NotBlank
    private String target;

    @NotBlank
    private String message;

    @Min(4)
    @Max(12)
    @NotNull
    private Integer length;

    @Min(30)
    @NotNull
    private Integer ttl;

    @Min(60)
    @NotNull
    private Integer sessionTtl;

    @Min(1)
    @Max(3)
    @NotNull
    private Integer resendAttempts;

    @Min(30)
    @NotNull
    private Integer resendTimeout;
}
