package com.bigbrassband.util.remittanceparse.remittance;

import org.apache.commons.lang3.StringUtils;

// Parse exception class for remittance PDF parsing
public class ParserException extends Exception {

    ParserException(RemittanceParser remittanceParser, Exception e) {
        super(buildMessage(remittanceParser, ""), e);
    }

    ParserException(RemittanceParser remittanceParser, String message) {
        super(buildMessage(remittanceParser, message));
    }

    ParserException(String message) {
        super(message);
    }

    ParserException(String message, Throwable t) {
        super(message,t);
    }

    private static String buildMessage(RemittanceParser remittanceParser, String message) {
        StringBuilder builder = new StringBuilder();
        if (!StringUtils.isEmpty(message))
            builder.append(message).append(" ");

        builder.append("Page: ").append(remittanceParser.getPageCount())
                .append(". Line: ").append(remittanceParser.getCurrentLine());
        return builder.toString();
    }
}
