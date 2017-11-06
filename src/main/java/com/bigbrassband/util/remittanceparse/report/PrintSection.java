package com.bigbrassband.util.remittanceparse.report;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Report printing class. Prints to a string.
abstract public class PrintSection implements AutoCloseable {
    protected final PrintStream print;
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    protected PrintSection() throws UnsupportedEncodingException {
        print = new PrintStream(baos, true, "UTF-8");
    }

    @Override
    public String toString() {
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
        print.close();
    }
}
