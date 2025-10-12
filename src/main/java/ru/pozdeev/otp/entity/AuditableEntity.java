package ru.pozdeev.otp.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static ru.pozdeev.otp.util.Constants.DEFAULT_DB_USER;

@Getter
@Setter
@MappedSuperclass
public class AuditableEntity {

    /**
     * Время создания записи
     */
    private LocalDateTime createTime;
    /**
     * Пользователь, создавший запись
     */
    private String createUser;
    /**
     * Время обновления
     */
    private LocalDateTime lastUpdateTime;
    /**
     * Пользователь, изменивший запись
     */
    private String lastUpdateUser;

    @PrePersist
    public void prePersist() {
        var now = LocalDateTime.now();
        this.createTime = now;
        this.lastUpdateTime = now;
        this.createUser = DEFAULT_DB_USER;
        this.lastUpdateUser = DEFAULT_DB_USER;
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdateTime = LocalDateTime.now();
        this.lastUpdateUser = DEFAULT_DB_USER;
    }
}
