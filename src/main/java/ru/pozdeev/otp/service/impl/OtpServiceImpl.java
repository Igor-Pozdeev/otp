package ru.pozdeev.otp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pozdeev.otp.entity.CheckOtp;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.exception.OtpException;
import ru.pozdeev.otp.mapper.OtpMapper;
import ru.pozdeev.otp.model.OtpCheckRequest;
import ru.pozdeev.otp.model.OtpGenerateRequest;
import ru.pozdeev.otp.model.SendingChannel;
import ru.pozdeev.otp.repository.CheckOtpRepository;
import ru.pozdeev.otp.repository.SendOtpRepository;
import ru.pozdeev.otp.service.OtpService;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final SendOtpRepository sendOtpRepository;
    private final CheckOtpRepository checkOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpMapper mapper;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public void generateAndSend(OtpGenerateRequest request) {
        Optional<SendOtp> lastOpt = sendOtpRepository.findFirstByProcessIdOrderByCreateTimeDesc(request.getProcessId().toString());
        LocalDateTime now = LocalDateTime.now();

        lastOpt.ifPresent(x -> {
            if (x.getCreateTime().plusSeconds(request.getSessionTtl()).isBefore(now)) {
                throw new OtpException("Превышено время жизни сессии для отправки ОТП");
            }

            if (Duration.between(x.getCreateTime(), now).getSeconds() < request.getResendTimeout()) {
                throw new OtpException("Превышена частота попыток отправки OTP");
            }
        });

        List<SendOtp> allForProcess = sendOtpRepository.findAllByProcessIdOrderByCreateTimeAsc(request.getProcessId().toString());
        if (!allForProcess.isEmpty()) {
            var firstSendOtp = allForProcess.get(0);
            if (allForProcess.size() > firstSendOtp.getResendAttempts()) {
                throw new OtpException("Превышено количество отправок OTP");
            }
        }

        String otp = generateNumericOtp(request.getLength());
        String raw = request.getProcessId().toString() + otp;
        String encodedOtp = passwordEncoder.encode(raw);
        String renderedMessage = request.getMessage().replace("%s", otp);
        SendOtp sendOtp = mapper.toSendOtp(request, encodedOtp, now);

        SendOtp savedSendOtp = sendOtpRepository.save(sendOtp);

        Map<SendingChannel, Runnable> strategyMap = Map.of(
                SendingChannel.TELEGRAM, this::actForTelegram,
                SendingChannel.CONSOLE, () -> System.out.println(otp));
        strategyMap.get(request.getSendingChannel()).run();
    }

    @Override
    @Transactional
    public void check(OtpCheckRequest request) {
        String processId = request.getProcessId().toString();
        LocalDateTime now = LocalDateTime.now();

        SendOtp last = sendOtpRepository.findFirstByProcessIdOrderByCreateTimeDesc(processId)
                .orElseThrow(() -> new OtpException("Не удалось найти информацию об отправке данного OTP"));

        if (last.getCreateTime().plusSeconds(last.getTtl()).isBefore(now)) {
            throw new OtpException("Время жизни OTP истекло");
        }

        List<CheckOtp> confirmed = checkOtpRepository.findAllByProcessIdAndCorrectIsTrueAndOtp(
                processId, request.getOtp());

        if (!confirmed.isEmpty()) {
            saveCheck(request, now, false);
            throw new OtpException("Попытка подтверждения ранее подтвержденного пароля");
        }

        String raw = processId + request.getOtp();
        boolean matches = passwordEncoder.matches(raw, last.getEncodedOtp());

        if (!matches) {
            saveCheck(request, now, false);
            throw new OtpException("Введен неверный OTP");
        }
        saveCheck(request, now, true);
    }

    private void saveCheck(OtpCheckRequest request, LocalDateTime now, boolean correct) {
        CheckOtp checkOtp = mapper.toCheckOtp(request, now, correct);
        checkOtpRepository.save(checkOtp);
    }

    private String generateNumericOtp(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
    private void actForTelegram() {
        log.debug("Будет сделана логика для TELEGRAM");
    }
}


