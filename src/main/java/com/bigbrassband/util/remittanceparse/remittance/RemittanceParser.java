package com.bigbrassband.util.remittanceparse.remittance;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

class RemittanceParser {
    private final ArrayList<RemittanceLine> remittanceLines;
    private final SimpleDateFormat dateParser;
    private int pageCount = 0;
    private String currentLine;

    RemittanceParser(ArrayList<RemittanceLine> remittanceLines, SimpleDateFormat dateParser) {
        this.remittanceLines = remittanceLines;
        this.dateParser=dateParser;
    }

    void newLine(String line) throws ParserException {
        ArrayList<String> fields = new ArrayList<>();
        currentLine = line;
        parseTsvLine(fields, line);
        if (fields.isEmpty())
            throw new ParserException(this, "Empty line.");

        switch (LineType.parse(fields)) {
            case unknown:
                throw new ParserException(this, "Could not determine line type.");

            case header:
                pageCount++;
                break;

            case detail:
                try {
                    remittanceLines.add(new RemittanceLine(fields,dateParser));
                } catch (ParseException e) {
                    throw new ParserException(this, e);
                }
                break;

            case additional:
                remittanceLines.get(remittanceLines.size() - 1).addAdditionalDetail(fields);
                break;
        }
    }

    private void parseTsvLine(ArrayList<String> destination, String source) {
        final String[] split = StringUtils.splitPreserveAllTokens(source, "\t");
        for (String s : split) {
            if (StringUtils.startsWith(s, "\"") && StringUtils.endsWith(s, "\"")) {
                destination.add(s.substring(1, s.length() - 1));
            } else
                destination.add(s);
        }

    }

    int getPageCount() {
        return pageCount;
    }

    String getCurrentLine() {
        return currentLine;
    }

    enum LineType {
        header,
        detail,
        additional,
        unknown;

        static LineType parse(ArrayList<String> fields) {
            if (fields.get(0).equalsIgnoreCase("Date"))
                return header;

            if (StringUtils.isEmpty(fields.get(0)))
                return additional;

            if (fields.size() >= 5 || fields.size() <= 6)
                return detail;

            return unknown;
        }
    }
}