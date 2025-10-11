package ru.pozdeev.otp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.pozdeev.otp.model.SendingChannel;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "send_otp")
public class SendOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String processId;
    @Enumerated(EnumType.STRING)
    private SendingChannel sendingChannel;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendOtp sendOtp = (SendOtp) o;
        return id.equals(sendOtp.id);
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}


