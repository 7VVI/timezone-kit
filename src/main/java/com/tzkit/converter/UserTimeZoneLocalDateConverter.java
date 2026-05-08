package com.tzkit.converter;

import com.tzkit.utils.DateUtils;
import com.tzkit.context.TimeZoneContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

/**
 * QueryParam日期参数转换器
 * 支持@RequestParam LocalDate参数的多格式解析
 */
@Component
public class UserTimeZoneLocalDateConverter implements Converter<String, LocalDate> {

    @Override
    public LocalDate convert(String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }
        TimeZone tz = TimeZoneContext.get();
        if (tz == null) {
            tz = DateUtils.getTimeZone();
        }
        Date date = DateUtils.parse(source, tz);
        if (date == null) {
            return null;
        }
        ZoneId zoneId = tz.toZoneId();
        return date.toInstant().atZone(zoneId).toLocalDate();
    }
}
