package com.example.usermanager.utils.convert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DateConvert {

    private DateConvert() {
    }

    public static Date formatDate(String date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.parse(date);
    }

    public static LocalDate convertDateToLocalDate(String dob) throws ParseException {
        return formatDate(dob).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }


}
