package com.tzkit.converter;

import com.tzkit.utils.DateUtils;
import com.tzkit.context.TimeZoneContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.TimeZone;

/**
 * QueryParam日期参数转换器
 * 支持@RequestParam Date参数的多格式解析
 */
@Component
public class UserTimeZoneDateConverter implements Converter<String, Date> {

    @Override
    public Date convert(String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }
        TimeZone tz = TimeZoneContext.get();
        if (tz == null) {
            tz = DateUtils.getTimeZone();
        }
        return DateUtils.parse(source, tz);
    }
}
