package ru.pozdeev.otp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pozdeev.otp.entity.CheckOtp;

import java.util.List;
import java.util.UUID;

@Repository
public interface CheckOtpRepository extends JpaRepository<CheckOtp, UUID> {

    List<CheckOtp> findAllByProcessIdAndCorrectIsTrueAndOtp(String string, String otp);
}


