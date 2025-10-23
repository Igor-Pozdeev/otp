package ru.pozdeev.otp.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.pozdeev.otp.model.SendingChannel;
import ru.pozdeev.otp.service.SendingChannelService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class OtpGeneralConfiguration {

    @Bean
    public Map<SendingChannel, SendingChannelService> sendingChannelStrategy(List<SendingChannelService> sendingChannelServices) {
        return sendingChannelServices.stream()
                .collect(Collectors.toMap(SendingChannelService::getChannel, Function.identity()));
    }
}
