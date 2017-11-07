package com.bigbrassband.util.remittanceparse;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

// Formatters
public class Format {
    public static final SimpleDateFormat dateFormatterAfterJune2017 = new SimpleDateFormat("M/d/yyyy");
    public static final SimpleDateFormat dateFormatterBeforeAndEqualMay2017 = new SimpleDateFormat("d/M/yyyy");
    public static final NumberFormat numberFormatter = NumberFormat.getIntegerInstance(Locale.US);
    public static final SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");


    static {
        Format.numberFormatter.setParseIntegerOnly(true);
    }

    public static String penniesString(long pennies) {
        long dollars = pennies / 100;
        long cents = Math.abs(pennies % 100);

        if (cents >= 10)
            return "$" + numberFormatter.format(dollars) + "." + cents;
        else
            return "$" + numberFormatter.format(dollars) + ".0" + cents;
    }
}
