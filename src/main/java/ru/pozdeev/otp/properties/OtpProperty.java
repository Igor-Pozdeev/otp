package ru.pozdeev.otp.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("otp-application")
public class OtpProperty {

    private Integer telegramChannelMaxTimeoutMs;
}
