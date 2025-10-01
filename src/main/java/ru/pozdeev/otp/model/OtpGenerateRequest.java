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

    private UUID processId;

    @NotBlank
    @NotNull(message = "Это обязательное поле. Необходимо его заполнить")
    private String sendingChannel;

    @NotBlank
    @NotNull(message = "Это обязательное поле. Необходимо его заполнить")
    private String target;

    @NotNull(message = "Это обязательное поле. Необходимо его заполнить")
    private String message;

    @Min(4)
    @Max(12)
    @NotNull(message = "Это обязательное поле. Необходимо его заполнить")
    private Integer length;

    @Min(30)
    @NotNull(message = "Это обязательное поле. Необходимо его заполнить")
    private Integer ttl;

    @Min(60)
    @NotNull(message = "Это обязательное поле. Необходимо его заполнить")
    private Integer sessionTtl;

    @Min(1)
    @Max(3)
    @NotNull(message = "Это обязательное поле. Необходимо его заполнить")
    private Integer resendAttempts;

    @Min(30)
    @NotNull(message = "Это обязательное поле. Необходимо его заполнить")
    private Integer resendTimeout;
}
