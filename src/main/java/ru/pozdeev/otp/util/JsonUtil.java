package ru.pozdeev.otp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.pozdeev.otp.exception.OtpException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtil {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static <T> T fromJson(String rawValue, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(rawValue, clazz);
        } catch (JsonProcessingException e) {
            throw new OtpException("Ошибка парсинга JSON", e);
        }
    }

    public static <T> String toJson(T object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new OtpException("Ошибка конвертации объекта в JSON", e);
        }
    }
}
