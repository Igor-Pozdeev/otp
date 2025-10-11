package ru.pozdeev.otp.mapper;

import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.pozdeev.otp.entity.CheckOtp;
import ru.pozdeev.otp.entity.OtpSendStatus;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.model.OtpCheckRequest;
import ru.pozdeev.otp.model.OtpGenerateRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OtpMapper {

    @BeforeMapping
    default void mapSendOtpBefore(@MappingTarget SendOtp sendOtp) {
        sendOtp.setSendMessageKey(UUID.randomUUID().toString());
        sendOtp.setStatus(OtpSendStatus.IN_PROCESS);
    }

    @Mapping(target = "processId", expression = "java(request.getProcessId().toString())")
    @Mapping(target = "sendMessageKey", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "encodedOtp", source = "encodedOtp")
    @Mapping(target = "sendTime", source = "sendTime")
    SendOtp toSendOtp(OtpGenerateRequest request, String encodedOtp, LocalDateTime sendTime);

    @Mapping(target = "processId", expression = "java(request.getProcessId().toString())")
    @Mapping(target = "checkTime", source = "checkTime")
    @Mapping(target = "correct", source = "correct")
    CheckOtp toCheckOtp(OtpCheckRequest request, LocalDateTime checkTime, Boolean correct);
}
