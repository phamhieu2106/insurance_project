package com.example.usermanager.utils.convert;


import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Component
@ReadingConverter
public class BytesToLocalDateConvert implements Converter<byte[], LocalDate> {


    @Override
    public LocalDate convert(byte[] source) {
        return LocalDate.parse(new String(source), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    }
}
