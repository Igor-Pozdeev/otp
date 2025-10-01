package ru.pozdeev.otp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "send_otp")
public class SendOtpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String processId;
    private String sendingChannel;
    private String target;
    private String message;
    private Integer length;
    private Integer ttl;
    private Integer sessionTtl;
    private Integer resendAttempts;
    private Integer resendTimeout;
    private String encodedOtp;
    private String sendMessageKey;
    @Enumerated(EnumType.STRING)
    private OtpSendStatus status;
    private LocalDateTime sendTime;
    private LocalDateTime createTime;
    private String createUser;
    private LocalDateTime lastUpdateTime;
    private String lastUpdateUser;
}


