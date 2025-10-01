package ru.pozdeev.otp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpCheckRequest {

    private UUID processId;

    private String otp;
}
