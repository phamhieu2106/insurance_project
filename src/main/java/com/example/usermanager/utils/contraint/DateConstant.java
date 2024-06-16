package com.example.usermanager.utils.contraint;

import java.util.Date;

public class DateConstant {

    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private DateConstant() {
        super();
    }

    public static long convertDateToLong(Date date) {
        return date.getTime();
    }
}
