package ru.pozdeev.otp.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    public static final String DEFAULT_DB_USER = "otp-default-user";

    public static final String MDC_KAFKA_TOPIC = "kafka_topic";

    public static final String MDC_KAFKA_PARTITION = "kafka_partition";

    public static final String MDC_KAFKA_OFFSET = "kafka_offset";

    public static final String MDC_KAFKA_KEY = "kafka_key";

    public static final String MDC_KAFKA_GROUP_ID = "kafka_group_id";

    public static final String MDC_KAFKA_MESSAGE_ID = "kafka_message_id";
}
