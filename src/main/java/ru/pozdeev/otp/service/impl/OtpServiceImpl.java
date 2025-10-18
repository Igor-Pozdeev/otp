package ru.pozdeev.otp.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.pozdeev.otp.entity.AuditableEntity;
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
import ru.pozdeev.otp.service.SendingChannelService;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OtpServiceImpl implements OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SendOtpRepository sendOtpRepository;

    private final CheckOtpRepository checkOtpRepository;

    private final PasswordEncoder passwordEncoder;

    private final OtpMapper mapper;

    private final Map<SendingChannel, SendingChannelService> strategyMap;

    public OtpServiceImpl(SendOtpRepository sendOtpRepository,
                          CheckOtpRepository checkOtpRepository,
                          PasswordEncoder passwordEncoder,
                          OtpMapper mapper,
                          List<SendingChannelService> sendingChannelServices) {
        this.sendOtpRepository = sendOtpRepository;
        this.checkOtpRepository = checkOtpRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.strategyMap = sendingChannelServices.stream()
                .collect(Collectors.toMap(SendingChannelService::getChannel, Function.identity()));
    }
    @Override
    public void generateAndSend(OtpGenerateRequest request) {
        String processIdAsString = request.getProcessId().toString();
        LocalDateTime currentTime = LocalDateTime.now();

        List<SendOtp> sendOtpList = sendOtpRepository.findAllByProcessId(processIdAsString);

        otpValidation(request, sendOtpList, currentTime);

        String otp = generateNumericOtp(request.getLength());
        String decodedOtp = processIdAsString + otp;
        String encodedOtp = passwordEncoder.encode(decodedOtp);
        String renderedMessage = request.getMessage().replace("%s", otp);
        SendOtp sendOtp = mapper.toSendOtp(request, encodedOtp, currentTime);

        SendOtp savedSendOtp = sendOtpRepository.save(sendOtp);

        strategyMap.get(request.getSendingChannel()).sendToTargetChannel(otp, savedSendOtp, renderedMessage);
    }

    private void otpValidation(OtpGenerateRequest request, List<SendOtp> sendOtpList, LocalDateTime currentTime) {
        sendOtpList.stream()
                .max(Comparator.comparing(AuditableEntity::getCreateTime))
                .ifPresent(lastOtp -> otpValidation(request, lastOtp, currentTime));

        sendOtpList.stream()
                .min(Comparator.comparing(AuditableEntity::getCreateTime))
                .ifPresent(firstOtp -> sendingAmountOtpValidation(sendOtpList.size(), firstOtp));
    }

    @Override
    public void check(OtpCheckRequest request) {
        String processId = request.getProcessId().toString();
        LocalDateTime currentTime = LocalDateTime.now();

        SendOtp lastSendOtp = sendOtpRepository.findFirstByProcessIdOrderByCreateTimeDesc(processId)
                .map(lastOtp -> {
                    lifetimeValidationOtp(lastOtp, currentTime);
                    return lastOtp;
                })
                .orElseThrow(() -> new OtpException("Не удалось найти информацию об отправке данного OTP"));

        checkForExistingOtp(request, processId, currentTime);

        String decodedOtp = processId + request.getOtp();
        boolean otpMatches = passwordEncoder.matches(decodedOtp, lastSendOtp.getEncodedOtp());

        if (!otpMatches) {
            saveCheck(request, currentTime, false);
            throw new OtpException("Введен неверный OTP");
        }
        saveCheck(request, currentTime, true);
    }

    private void checkForExistingOtp(OtpCheckRequest request, String processId, LocalDateTime currentTime) {
        if (checkOtpRepository.existsByProcessIdAndCorrectIsTrueAndOtp(processId, request.getOtp())) {
            saveCheck(request, currentTime, false);
            throw new OtpException("Попытка подтверждения ранее подтвержденного пароля");
        }
    }

    private void lifetimeValidationOtp(SendOtp lastOtp, LocalDateTime currentTime) {
        if (lastOtp.getCreateTime().plusSeconds(lastOtp.getTtl()).isBefore(currentTime)) {
            throw new OtpException("Время жизни OTP истекло");
        }
    }

    private void otpValidation(OtpGenerateRequest request, SendOtp lastSendOtp, LocalDateTime currentTime) {
        if (lastSendOtp.getCreateTime().plusSeconds(request.getSessionTtl()).isBefore(currentTime)) {
            throw new OtpException("Превышено время жизни сессии для отправки ОТП");
        }

        if (Duration.between(lastSendOtp.getCreateTime(), currentTime).getSeconds() < request.getResendTimeout()) {
            throw new OtpException("Превышена частота попыток отправки OTP");
        }
    }

    private void sendingAmountOtpValidation(int otpListSize, SendOtp firstSendOtp) {
        if (otpListSize > firstSendOtp.getResendAttempts()) {
            throw new OtpException("Превышено количество отправок OTP");
        }
    }

    private void saveCheck(OtpCheckRequest request, LocalDateTime currentTime, boolean correct) {
        CheckOtp checkOtp = mapper.toCheckOtp(request, currentTime, correct);
        checkOtpRepository.save(checkOtp);
    }

    private String generateNumericOtp(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}


