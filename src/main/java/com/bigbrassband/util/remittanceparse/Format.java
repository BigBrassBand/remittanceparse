package com.bigbrassband.util.remittanceparse;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

// Formatters
public class Format {
    public static final SimpleDateFormat usDateFormatter = new SimpleDateFormat("M/d/yyyy");
    public static final SimpleDateFormat euroDateFormatter = new SimpleDateFormat("d/M/yyyy");
    public static final NumberFormat numberFormatter = NumberFormat.getIntegerInstance(Locale.US);
    public static final SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");


    static {
        Format.numberFormatter.setParseIntegerOnly(true);
        usDateFormatter.setLenient(false);
        euroDateFormatter.setLenient(false);
    }

    public static String penniesString(long pennies) {
        long dollars = pennies / 100;
        long cents = Math.abs(pennies % 100);

        if (cents >= 10)
            return "$" + numberFormatter.format(dollars) + "." + cents;
        else
            return "$" + numberFormatter.format(dollars) + ".0" + cents;
    }

    static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    static LocalDate asLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

}
