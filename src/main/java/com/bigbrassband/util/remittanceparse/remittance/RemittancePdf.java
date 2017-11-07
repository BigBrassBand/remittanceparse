package com.bigbrassband.util.remittanceparse.remittance;

import com.bigbrassband.util.remittanceparse.Format;
import org.apache.commons.lang3.StringUtils;
import technology.tabula.CommandLineApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// Parses a remittance PDF into in memory structure
public class RemittancePdf {
    private final ArrayList<RemittanceLine> lines;
    private SimpleDateFormat dateParser;
    private Date minimumDate, maximumDate;

    public RemittancePdf(File inputPdfFile, ArrayList<RemittanceLine> lines) throws IOException, ParserException {
        this.lines=lines;
        File tempTsvFile = File.createTempFile("com.bigbrassband.util.remittanceparse.", ".tsv.tmp");
        try (ExitCodeCaptor exitCodeCaptor = new ExitCodeCaptor()) {
            exitCodeCaptor.run(() ->
                    CommandLineApp.main(new String[]{"--pages", "all", "-f", "TSV", "-t", "-g", "-o",
                            tempTsvFile.getAbsolutePath(), inputPdfFile.getAbsolutePath()}));

            if (exitCodeCaptor.getStatus() != 0)
                throw new IOException("Failed PDF convert with error code " + exitCodeCaptor.getStatus());
        }

        try {
            final String[] array = StringUtils.splitByWholeSeparator(inputPdfFile.getName(), "-");
            int year = Integer.parseInt(array[0]);
            int month = Integer.parseInt(array[1]);
            if (year < 2017 || month <= 6)
                dateParser = Format.dateFormatterBeforeAndEqualMay2017;
            else
                dateParser = Format.dateFormatterAfterJune2017;
        }
        catch (Throwable ignore)
        {
            dateParser = Format.dateFormatterAfterJune2017;
        }

        RemittanceParser parser = new RemittanceParser(lines,dateParser);
        try (BufferedReader reader = new BufferedReader(new FileReader(tempTsvFile))) {
            for (String line = reader.readLine(); line != null;
                 line = reader.readLine()) {
                parser.newLine(line);
            }
        }


    }

    public ArrayList<RemittanceLine> getLines() {
        return lines;
    }
}
