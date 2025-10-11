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
@Table(name = "check_otp")
public class CheckOtp extends AuditableEntity {

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
     * Введенный клиентом код
     */
    private String otp;
    /**
     * Время проверки
     */
    private LocalDateTime checkTime;
    /**
     * Признак корректности введенного пароля
     */
    private Boolean correct;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckOtp checkOtp = (CheckOtp) o;
        return id.equals(checkOtp.id);
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}


