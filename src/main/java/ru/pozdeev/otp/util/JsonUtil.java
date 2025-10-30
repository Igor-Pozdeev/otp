package ru.pozdeev.otp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.pozdeev.otp.exception.OtpException;

@Component
@RequiredArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    public <T> T fromJson(String rawValue, Class<T> clazz) {
        try {
            return objectMapper.readValue(rawValue, clazz);
        } catch (JsonProcessingException e) {
            throw new OtpException("Ошибка парсинга JSON", e);
        }
    }

    public <T> String toJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new OtpException("Ошибка конвертации объекта в JSON", e);
        }
    }
}
