package com.bigbrassband.util.remittanceparse;

import com.bigbrassband.util.remittanceparse.remittance.ParserException;
import com.bigbrassband.util.remittanceparse.remittance.RemittanceLine;
import com.bigbrassband.util.remittanceparse.remittance.RemittancePdf;
import com.bigbrassband.util.remittanceparse.report.RemittancePdfSalesMissingFromTransactionApiSales;
import com.bigbrassband.util.remittanceparse.report.TransactionApiSalesMissingFromRemittancePdf;
import com.bigbrassband.util.remittanceparse.report.refund.RefundMatcher;
import com.bigbrassband.util.remittanceparse.transaction.Transactions;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.tools.ant.DirectoryScanner;

public class Main {

    public static void main(String args[]) throws IOException, ParserException {
        if(args.length!=2)
        {
            printUsage();
            System.exit(1);
        }

        ArrayList<RemittanceLine> lines=new ArrayList<>();

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{args[0]});
        scanner.setCaseSensitive(false);
        scanner.scan();
        String[] files = scanner.getIncludedFiles();
        Arrays.sort(files);
        if(files.length==0)
        {
            System.err.println("No PDF files found.");
            System.exit(1);
        }

        Date minimumDate=null, maximumDate=null;
        for (String file : files) {
            System.err.println("Processing PDF " + file);
            RemittancePdf remittancePdf=new RemittancePdf(new File(file));
            remittancePdf.parseRemittancePdf(lines);

            //Remittance PDF date covers the previous month
            final YearMonth yearMonth = YearMonth.from(
                    Format.asLocalDate(remittancePdf.getRemittanceDate())).minusMonths(1);

            Date firstDay=Format.asDate(yearMonth.atDay(1));
            Date lastDay=Format.asDate(yearMonth.atEndOfMonth());

            if(minimumDate==null || firstDay.before(minimumDate))
                minimumDate=firstDay;

            if(maximumDate==null || lastDay.after(maximumDate))
                maximumDate=lastDay;
        }

        System.err.println("Processing transactions JSON " + args[1]);
        Transactions transactions = new Transactions(new File(args[1]),
                minimumDate, maximumDate);


        report(lines, transactions, minimumDate, maximumDate);
    }

    private static void report(ArrayList<RemittanceLine> lines, Transactions transactions, Date minimumDate, Date maximumDate) throws UnsupportedEncodingException {
        //Pass through the transactions API haystack
        RemittancePdfSalesMissingFromTransactionApiSales pdfSalesMissing =
                new RemittancePdfSalesMissingFromTransactionApiSales();
        RefundMatcher refundMatcher = new RefundMatcher();
        Keyed.haystackSearch(lines, transactions.getTransactions(),
                pdfSalesMissing, refundMatcher.getRefundMatcherHelper());

        //Pass through the remittance PDF haystack
        TransactionApiSalesMissingFromRemittancePdf transactionApiSalesMissing =
                new TransactionApiSalesMissingFromRemittancePdf();
        Keyed.haystackSearch(transactions.getTransactions(), lines,
                transactionApiSalesMissing, refundMatcher);

        //Print the report
        System.out.println("Reconciliation for " +
                Format.usDateFormatter.format(minimumDate) + " - " +
                Format.usDateFormatter.format(maximumDate));
        System.out.println();

        System.out.println(transactionApiSalesMissing.toString());
        System.out.println(pdfSalesMissing.toString());
        System.out.println(refundMatcher.toString());

        long totalPennies = transactionApiSalesMissing.getTotalPennies()
                - pdfSalesMissing.getTotalPennies()
                + refundMatcher.getRemittancePdfRefundsMissedPennies()
                - refundMatcher.getTransactionsApiRefundsMissedPennies();

        System.out.println("Total variance (1. Transaction API Sales Missed - 2. Remittance PDF Sales Missed + 3. Remittance PDF Refunds Missed - 4. Transaction API Refunds Missed ): "
                + Format.penniesString(totalPennies));

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("* Matched based on amount and date. No transaction ID matches between remittance PDF and the transactions API.");
    }

    private static void printUsage() {
        final String jarName = getJarName("remittanceparse.jar");
        System.out.println(jarName+" - Copyright 2017 BigBrassBand LLC");
        System.out.println();
        System.out.println("This utility compares an Atlassian remittance report PDF with a\ntransactions JSON file.");
        System.out.println();
        System.out.println("PURPOSE");
        System.out.println("This utility compares an Atlassian remittance report PDF with a\ntransactions JSON file.");
        System.out.println();
        System.out.println("You can download your current transactions JSON file here\n(change <VENDORID> to your vendor ID): ");
        System.out.println("https://marketplace.atlassian.com/rest/2/vendors/<VENDORID>/reporting/sales/transactions/export?accept=json");
        System.out.println();
        System.out.println("The remittance report PDF comes in e-mail monthly directly\nfrom Atlassian.");
        System.out.println();
        System.out.println("COMMAND LINE USAGE");
        System.out.println("java -jar "+ jarName +" REMITTANCEPDF JSONFILE");
        System.out.println();
        System.out.println("WHERE");
        System.out.println("REMITTANCEPDF is the remittance PDF received through e-mail.");
        System.out.println("JSONFILE is the downloaded transactions JSON file.");
    }

    private static String getJarName(String defaultJarName)
    {
        try {
            return new File(Main.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath())
                    .getName();
        }
        catch (Throwable ignore)
        {
            return defaultJarName;
        }
    }
}