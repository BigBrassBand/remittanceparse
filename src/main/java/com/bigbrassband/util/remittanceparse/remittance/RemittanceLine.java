package com.bigbrassband.util.remittanceparse.remittance;

import com.bigbrassband.util.remittanceparse.Format;
import com.bigbrassband.util.remittanceparse.Keyed;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Line item on remittance PDF
public class RemittanceLine extends Keyed {
    private static final Pattern referencePattern = Pattern.compile(".+ #(.+)");

    private final Date date;
    private final String reference;
    private final StringBuilder description = new StringBuilder();
    private final long amountPennies;
    private final long paidPennies;
    private final String id;

    RemittanceLine(ArrayList<String> fields) throws ParseException {
        final Matcher matcher = referencePattern.matcher(fields.get(1));
        if (!matcher.matches())
            throw new ParseException("Can not parse reference field: " + fields.get(1) + ".", 0);
        id = matcher.group(1);

        date = Format.dateFormatter.parse(fields.get(0));
        reference = fields.get(1);
        description.append(fields.get(2));
        amountPennies = parsePennies(fields.get(3));
        paidPennies = parsePennies(fields.get(fields.size() - 1));
    }

    private static long parsePennies(String amount) throws ParseException {
        if (StringUtils.isEmpty(amount))
            return 0L;

        final String[] split = StringUtils.split(amount, ".");
        long pennies = Format.numberFormatter.parse(split[1]).longValue();
        long dollars = Format.numberFormatter.parse(split[0]).longValue();
        return 100L * dollars + (pennies * (dollars < 0 ? -1 : 1));
    }

    void addAdditionalDetail(ArrayList<String> fields) {
        description.append(' ').append(fields.get(2));
    }

    @Override
    public String toString() {
        return "ReportLine{" +
                "date=" + date +
                ", reference='" + reference + '\'' +
                ", description=" + description +
                ", amountPennies=" + amountPennies +
                ", paidPennies=" + paidPennies +
                '}';
    }

    public Date getDate() {
        return date;
    }

    public String getReference() {
        return reference;
    }

    public long getPaidPennies() {
        return paidPennies;
    }

    public String getPaidString() {
        return Format.penniesString(paidPennies);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    protected long getPennies() {
        return paidPennies;
    }
}