package ru.pozdeev.otp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SendingChannel {
    @JsonProperty("telegram")
    TELEGRAM,

    @JsonProperty("console")
    CONSOLE
}
