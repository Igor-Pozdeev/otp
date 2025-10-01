package ru.pozdeev.otp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pozdeev.otp.entity.CheckOtpEntity;

import java.util.UUID;

@Repository
public interface CheckOtpRepository extends JpaRepository<CheckOtpEntity, UUID> {

}


