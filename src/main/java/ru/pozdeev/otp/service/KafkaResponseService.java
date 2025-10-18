package ru.pozdeev.otp.service;

import ru.pozdeev.otp.dto.kafka.KafkaResponse;

public interface KafkaResponseService {

    void processKafkaResponse(KafkaResponse kafkaResponse);
}
