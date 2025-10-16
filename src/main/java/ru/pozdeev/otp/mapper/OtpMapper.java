package ru.pozdeev.otp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.pozdeev.otp.entity.CheckOtp;
import ru.pozdeev.otp.entity.SendOtp;
import ru.pozdeev.otp.model.OtpCheckRequest;
import ru.pozdeev.otp.model.OtpGenerateRequest;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface OtpMapper {

    @Mapping(target = "processId", expression = "java(request.getProcessId().toString())")
    @Mapping(target = "sendMessageKey", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "status", expression = "java(ru.pozdeev.otp.entity.OtpSendStatus.IN_PROCESS)")
    @Mapping(target = "encodedOtp", source = "encodedOtp")
    @Mapping(target = "sendTime", source = "sendTime")
    SendOtp toSendOtp(OtpGenerateRequest request, String encodedOtp, LocalDateTime sendTime);

    @Mapping(target = "processId", expression = "java(request.getProcessId().toString())")
    @Mapping(target = "checkTime", source = "checkTime")
    @Mapping(target = "correct", source = "correct")
    CheckOtp toCheckOtp(OtpCheckRequest request, LocalDateTime checkTime, Boolean correct);
}
