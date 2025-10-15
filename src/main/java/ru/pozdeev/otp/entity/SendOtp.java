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
public class SendOtp extends AuditableEntity {

    /**
     * Идентификатор записи
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    /**
     * Идентификатор процесса
     */
    private String processId;
    /**
     * Канал отправки
     */
    @Enumerated(EnumType.STRING)
    private SendingChannel sendingChannel;
    /**
     * Адрес, куда будет выполнена отправка в канале
     */
    private String target;
    /**
     * Текст сообщения
     */
    private String message;
    /**
     * Длина одноразового пароля
     */
    private Integer length;
    /**
     * Время жизни одноразового пароля в секундах
     */
    private Integer ttl;
    /**
     * Время жизни сессии одноразового пароля в секундах
     */
    private Integer sessionTtl;
    /**
     * Количество возможных повторных отправок кода
     */
    private Integer resendAttempts;
    /**
     * Таймаут перед повторным запросом кода в секундах
     */
    private Integer resendTimeout;
    /**
     * Зашифрованный одноразовый пароль
     */
    private String encodedOtp;
    /**
     * Идентификатор сообщения, отправляемого во внешнюю систему
     */
    private String sendMessageKey;
    /**
     * Статус отправки сообщения
     */
    @Enumerated(EnumType.STRING)
    private OtpSendStatus status;
    /**
     * Время отправки одноразового пароля
     */
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


