package ru.pozdeev.otp.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpGenerateRequest {

    @NotNull
    private UUID processId;

    @NotNull
    private SendingChannel sendingChannel;

    @NotBlank
    private String target;

    @NotBlank
    private String message;

    @Range(min = 4, max = 12)
    @NotNull
    private Integer length;

    @Min(30)
    @NotNull
    private Integer ttl;

    @Min(60)
    @NotNull
    private Integer sessionTtl;

    @Range(min = 1, max = 3)
    @NotNull
    private Integer resendAttempts;

    @Min(30)
    @NotNull
    private Integer resendTimeout;
}
