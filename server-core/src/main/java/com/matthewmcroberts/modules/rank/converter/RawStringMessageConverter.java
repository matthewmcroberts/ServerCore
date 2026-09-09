package com.matthewmcroberts.modules.rank.converter;

import org.springframework.messaging.Message;
import org.springframework.messaging.converter.StringMessageConverter;

public class RawStringMessageConverter extends StringMessageConverter {
    @Override
    protected boolean canConvertFrom(Message<?> message, Class<?> targetClass) {
        return String.class == targetClass;
    }
}