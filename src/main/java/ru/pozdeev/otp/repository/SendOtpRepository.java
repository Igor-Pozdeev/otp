package ru.pozdeev.otp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pozdeev.otp.entity.SendOtp;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SendOtpRepository extends JpaRepository<SendOtp, UUID> {

    Optional<SendOtp> findFirstByProcessIdOrderByCreateTimeDesc(String processId);

    List<SendOtp> findAllByProcessIdOrderByCreateTimeAsc(String processId);
}


