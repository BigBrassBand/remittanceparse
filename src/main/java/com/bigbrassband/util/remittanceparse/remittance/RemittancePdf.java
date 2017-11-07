package com.bigbrassband.util.remittanceparse.remittance;

import com.bigbrassband.util.remittanceparse.Format;
import org.apache.commons.lang3.StringUtils;
import technology.tabula.CommandLineApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// Parses a remittance PDF into in memory structure
public class RemittancePdf {
    private Date remittanceDate;
    private SimpleDateFormat dateParser;
    private final File inputPdfFile;


    public RemittancePdf(File inputPdfFile) throws IOException, ParserException {
        this.inputPdfFile=inputPdfFile;

        File tempTsvFileForDate = File.createTempFile(RemittancePdf.class.getPackage().getName()+".", ".tsv.tmp");
        try (ExitCodeCaptor exitCodeCaptor = new ExitCodeCaptor()) {
            exitCodeCaptor.run(() -> {
                CommandLineApp.main(new String[]{"-a", "-11.093,283.05,234.473,610.47","-p", "1",
                        "-f", "TSV", "-o",tempTsvFileForDate.getAbsolutePath(),
                        inputPdfFile.getAbsolutePath()});
            });

            if (exitCodeCaptor.getStatus() != 0)
                throw new IOException("Failed PDF convert with error code " + exitCodeCaptor.getStatus());
        }

        handleDate(tempTsvFileForDate);
    }

    public void parseRemittancePdf(ArrayList<RemittanceLine> lines) throws IOException, ParserException {
        File tempTsvFileForLines = File.createTempFile(RemittancePdf.class.getPackage().toString()+".", ".tsv.tmp");

        try (ExitCodeCaptor exitCodeCaptor = new ExitCodeCaptor()) {
            exitCodeCaptor.run(() -> {
                CommandLineApp.main(new String[]{"--pages", "all", "-f", "TSV", "-t", "-g", "-o",
                        tempTsvFileForLines.getAbsolutePath(), inputPdfFile.getAbsolutePath()});
            });

            if (exitCodeCaptor.getStatus() != 0)
                throw new IOException("Failed PDF convert with error code " + exitCodeCaptor.getStatus());
        }


        RemittanceParser parser = new RemittanceParser(lines, dateParser);
        try (BufferedReader reader = new BufferedReader(new FileReader(tempTsvFileForLines))) {
            for (String line = reader.readLine(); line != null;
                 line = reader.readLine()) {
                parser.newLine(line);
            }
        }
    }

    //determines the date parser to use and the date of the remittance PDF
    private void handleDate(File tempTsvFileForDate) throws IOException, ParserException {

        String date=null;
        try(BufferedReader reader=new BufferedReader(new FileReader(tempTsvFileForDate)))
        {
            for(String line=reader.readLine();line!=null;line=reader.readLine())
            {
                line=line.trim();
                if(!StringUtils.startsWithIgnoreCase(line,"Date"))
                    continue;

                final String[] split = StringUtils.splitByWholeSeparator(line, " ");
                if(split.length!=2)
                    throw new ParserException("Could not parse date field: "+line);

                date=split[1];
                break;
            }
        }

        if(date==null)
            throw new ParserException("Never found date field");

        //start guessing at the date format
        boolean isEuroDate=isEuroDate(date);
        boolean isUSDate=isUSDate(date);

        if(!isEuroDate && !isUSDate)
            throw new ParserException("Could not parse date field: "+date);

        if(isEuroDate && !isUSDate) {
            dateParser = Format.euroDateFormatter;
            return;
        }

        if(!isEuroDate) {
            dateParser = Format.usDateFormatter;
            return;
        }

        //hmmm.... date is valid in both formats. Use the filename instead

        try {
            final String[] array = StringUtils.splitByWholeSeparator(inputPdfFile.getName(), "-");
            int year = Integer.parseInt(array[0]);
            int month = Integer.parseInt(array[1]);
            if (year < 2017 || month <= 6)
                dateParser = Format.euroDateFormatter;
            else
                dateParser = Format.usDateFormatter;

            remittanceDate=dateParser.parse(date);

        }
        catch (Throwable t)
        {
            throw new ParserException("Can not determine date format to use.",t);
        }

    }

    private boolean isUSDate(String date) {
        try {
            remittanceDate=Format.usDateFormatter.parse(date);
        } catch (ParseException ignore) {
            return false;
        }

        return true;
    }

    private boolean isEuroDate(String date) {
        try {
            remittanceDate=Format.euroDateFormatter.parse(date);
        } catch (ParseException ignore) {
            return false;
        }

        return true;
    }

    public Date getRemittanceDate() {
        return remittanceDate;
    }
}
