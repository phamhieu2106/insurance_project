package com.example.usermanager.utils.convert;


import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@ReadingConverter
public class BytesToDateConverter implements Converter<byte[], Date> {


    @Override
    public Date convert(byte[] source) {
        String dateString = new String(source);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format", e);
        }
    }
}
