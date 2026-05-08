package com.tzkit.converter;

import com.tzkit.utils.DateUtils;
import com.tzkit.context.TimeZoneContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.TimeZone;

/**
 * QueryParam日期参数转换器
 * 支持@RequestParam LocalDateTime参数的多格式解析
 */
@Component
public class UserTimeZoneLocalDateTimeConverter implements Converter<String, java.time.LocalDateTime> {

    @Override
    public java.time.LocalDateTime convert(String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }
        TimeZone tz = TimeZoneContext.get();
        if (tz == null) {
            tz = DateUtils.getTimeZone();
        }
        return DateUtils.parseLdt(source);
    }
}
