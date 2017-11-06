package com.bigbrassband.util.remittanceparse.remittance;

import technology.tabula.CommandLineApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

// Parses a remittance PDF into in memory structure
public class RemittancePdf {
    private final ArrayList<RemittanceLine> lines = new ArrayList<>();
    private Date minimumDate, maximumDate;

    public RemittancePdf(File inputPdfFile) throws IOException, ParserException {
        File tempTsvFile = File.createTempFile("com.bigbrassband.util.remittanceparse.", ".tsv.tmp");
        try (ExitCodeCaptor exitCodeCaptor = new ExitCodeCaptor()) {
            exitCodeCaptor.run(() ->
                    CommandLineApp.main(new String[]{"--pages", "all", "-f", "TSV", "-t", "-g", "-o",
                            tempTsvFile.getAbsolutePath(), inputPdfFile.getAbsolutePath()}));

            if (exitCodeCaptor.getStatus() != 0)
                throw new IOException("Failed PDF convert with error code " + exitCodeCaptor.getStatus());
        }

        RemittanceParser parser = new RemittanceParser(lines);
        try (BufferedReader reader = new BufferedReader(new FileReader(tempTsvFile))) {
            for (String line = reader.readLine(); line != null;
                 line = reader.readLine()) {
                parser.newLine(line);
            }
        }

        for (RemittanceLine line : lines) {
            final Date date = line.getDate();
            if (minimumDate == null || date.before(minimumDate))
                minimumDate = date;
            if (maximumDate == null || date.after(maximumDate))
                maximumDate = date;
        }
    }

    public Date getMinimumDate() {
        return minimumDate;
    }

    public Date getMaximumDate() {
        return maximumDate;
    }

    public ArrayList<RemittanceLine> getLines() {
        return lines;
    }
}
