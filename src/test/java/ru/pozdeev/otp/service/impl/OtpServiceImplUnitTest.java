package ru.pozdeev.otp.service.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.pozdeev.otp.dto.common.SendingResult;
import ru.pozdeev.otp.dto.common.SendingResultStatus;
import ru.pozdeev.otp.entity.CheckOtp;
import ru.pozdeev.otp.entity.OtpSendStatus;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.exception.OtpException;
import ru.pozdeev.otp.mapper.OtpMapper;
import ru.pozdeev.otp.model.OtpCheckRequest;
import ru.pozdeev.otp.model.OtpGenerateRequest;
import ru.pozdeev.otp.model.SendingChannel;
import ru.pozdeev.otp.repository.CheckOtpRepository;
import ru.pozdeev.otp.repository.SendOtpRepository;
import ru.pozdeev.otp.sender.Sender;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplUnitTest {

    @Mock
    private SendOtpRepository sendOtpRepository;

    @Mock
    private CheckOtpRepository checkOtpRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private OtpMapper mapper = Mappers.getMapper(OtpMapper.class);

    @Mock
    private Map<SendingChannel, Sender> sendingChannelStrategy;

    @InjectMocks
    private OtpServiceImpl otpService;

    @Nested
    class GenerateAndSend {

        @Test
        void when_generateAndSend_validRequest_then_saveAndSendOtp() throws Exception {
            UUID processId = UUID.randomUUID();
            OtpGenerateRequest request = createOtpGenerateRequest(processId);
            Sender sender = mock(Sender.class);
            SendOtp sendOtp = createSendOtp(processId);

            when(sendOtpRepository.findAllByProcessId(processId.toString())).thenReturn(Collections.emptyList());
            when(passwordEncoder.encode(anyString())).thenReturn("encodedOtp");
            when(sendOtpRepository.save(any(SendOtp.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(sendingChannelStrategy.get(SendingChannel.CONSOLE)).thenReturn(sender);
            when(sender.sendToTargetChannel(anyString(), any(SendOtp.class), anyString()))
                    .thenReturn(new SendingResult(SendingResultStatus.SUCCESS, null));

            otpService.generateAndSend(request);

            ArgumentCaptor<SendOtp> sendOtpCaptor = ArgumentCaptor.forClass(SendOtp.class);
            verify(sendOtpRepository, times(2)).save(sendOtpCaptor.capture());
            SendOtp capturedSendOtp = sendOtpCaptor.getAllValues().get(0);

            verify(sender).sendToTargetChannel(anyString(), eq(capturedSendOtp), anyString());
            assertEquals(OtpSendStatus.DELIVERED, capturedSendOtp.getStatus());
        }

        @Test
        void when_generateAndSend_unsupportedChannel_then_throwException() {
            UUID processId = UUID.randomUUID();
            OtpGenerateRequest request = createOtpGenerateRequest(processId);

            when(sendOtpRepository.findAllByProcessId(processId.toString())).thenReturn(Collections.emptyList());
            when(passwordEncoder.encode(anyString())).thenReturn("encodedOtp");
            when(sendOtpRepository.save(any(SendOtp.class))).thenReturn(new SendOtp());
            when(sendingChannelStrategy.get(SendingChannel.CONSOLE)).thenReturn(null);

            OtpException exception = assertThrows(OtpException.class, () -> otpService.generateAndSend(request));
            assertTrue(exception.getMessage().contains("Неподдерживаемый канал отправки"));
        }

        @Test
        void when_generateAndSend_tooFrequent_then_throwException() {
            UUID processId = UUID.randomUUID();
            OtpGenerateRequest request = createOtpGenerateRequest(processId);
            request.setResendTimeout(60);

            SendOtp lastOtp = new SendOtp();
            lastOtp.setCreateTime(LocalDateTime.now().minusSeconds(30));

            when(sendOtpRepository.findAllByProcessId(processId.toString())).thenReturn(List.of(lastOtp));

            OtpException exception = assertThrows(OtpException.class, () -> otpService.generateAndSend(request));
            assertEquals("Превышена частота попыток отправки OTP", exception.getMessage());
        }

        private OtpGenerateRequest createOtpGenerateRequest(UUID processId) {
            OtpGenerateRequest request = new OtpGenerateRequest();
            request.setProcessId(processId);
            request.setLength(6);
            request.setMessage("Your code is %s");
            request.setSendingChannel(SendingChannel.CONSOLE);
            request.setResendTimeout(60);
            request.setSessionTtl(300);
            return request;
        }

        private SendOtp createSendOtp(UUID processId) {
            SendOtp sendOtp = new SendOtp();
            sendOtp.setProcessId(processId.toString());
            sendOtp.setStatus(OtpSendStatus.IN_PROCESS);
            return sendOtp;
        }
    }

    @Nested
    class Check {

        @Test
        void when_check_validOtp_then_saveCorrectCheck() {
            UUID processId = UUID.randomUUID();
            String otp = "123456";
            OtpCheckRequest request = new OtpCheckRequest();
            request.setProcessId(processId);
            request.setOtp(otp);

            SendOtp lastSendOtp = new SendOtp();
            lastSendOtp.setEncodedOtp("encodedOtp");
            lastSendOtp.setCreateTime(LocalDateTime.now());
            lastSendOtp.setTtl(300);

            when(sendOtpRepository.findFirstByProcessIdOrderByCreateTimeDesc(processId.toString()))
                    .thenReturn(Optional.of(lastSendOtp));
            when(checkOtpRepository.existsByProcessIdAndCorrectIsTrueAndOtp(processId.toString(), otp))
                    .thenReturn(false);
            when(passwordEncoder.matches(anyString(), eq("encodedOtp"))).thenReturn(true);

            otpService.check(request);

            ArgumentCaptor<CheckOtp> captor = ArgumentCaptor.forClass(CheckOtp.class);
            verify(checkOtpRepository).save(captor.capture());
            CheckOtp savedCheck = captor.getValue();
            assertAll(
                    () -> assertEquals(processId.toString(), savedCheck.getProcessId()),
                    () -> assertEquals(otp, savedCheck.getOtp()),
                    () -> assertTrue(savedCheck.getCorrect())
            );
        }

        @Test
        void when_check_wrongOtp_then_throwExceptionAndSaveIncorrectCheck() {
            UUID processId = UUID.randomUUID();
            String otp = "123456";
            OtpCheckRequest request = new OtpCheckRequest();
            request.setProcessId(processId);
            request.setOtp(otp);

            SendOtp lastSendOtp = new SendOtp();
            lastSendOtp.setEncodedOtp("encodedOtp");
            lastSendOtp.setCreateTime(LocalDateTime.now());
            lastSendOtp.setTtl(300);

            when(sendOtpRepository.findFirstByProcessIdOrderByCreateTimeDesc(processId.toString()))
                    .thenReturn(Optional.of(lastSendOtp));
            when(checkOtpRepository.existsByProcessIdAndCorrectIsTrueAndOtp(processId.toString(), otp))
                    .thenReturn(false);
            when(passwordEncoder.matches(anyString(), eq("encodedOtp"))).thenReturn(false);

            OtpException exception = assertThrows(OtpException.class, () -> otpService.check(request));
            assertEquals("Введен неверный OTP", exception.getMessage());

            ArgumentCaptor<CheckOtp> captor = ArgumentCaptor.forClass(CheckOtp.class);
            verify(checkOtpRepository).save(captor.capture());
            assertFalse(captor.getValue().getCorrect());
        }

        @Test
        void when_check_alreadyConfirmed_then_throwException() {
            UUID processId = UUID.randomUUID();
            String otp = "123456";
            OtpCheckRequest request = new OtpCheckRequest();
            request.setProcessId(processId);
            request.setOtp(otp);

            SendOtp lastSendOtp = new SendOtp();
            lastSendOtp.setCreateTime(LocalDateTime.now());
            lastSendOtp.setTtl(300);

            when(sendOtpRepository.findFirstByProcessIdOrderByCreateTimeDesc(processId.toString()))
                    .thenReturn(Optional.of(lastSendOtp));
            when(checkOtpRepository.existsByProcessIdAndCorrectIsTrueAndOtp(processId.toString(), otp))
                    .thenReturn(true);

            OtpException exception = assertThrows(OtpException.class, () -> otpService.check(request));
            assertEquals("Попытка подтверждения ранее подтвержденного пароля", exception.getMessage());
            verify(checkOtpRepository).save(any(CheckOtp.class));
        }

        @Test
        void when_check_otpExpired_then_throwException() {
            UUID processId = UUID.randomUUID();
            OtpCheckRequest request = new OtpCheckRequest();
            request.setProcessId(processId);

            SendOtp lastSendOtp = new SendOtp();
            lastSendOtp.setCreateTime(LocalDateTime.now().minusSeconds(301));
            lastSendOtp.setTtl(300);

            when(sendOtpRepository.findFirstByProcessIdOrderByCreateTimeDesc(processId.toString()))
                    .thenReturn(Optional.of(lastSendOtp));

            OtpException exception = assertThrows(OtpException.class, () -> otpService.check(request));
            assertEquals("Время жизни OTP истекло", exception.getMessage());
        }
    }
}
