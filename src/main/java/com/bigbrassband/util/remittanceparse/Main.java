package com.bigbrassband.util.remittanceparse;

import com.bigbrassband.util.remittanceparse.remittance.ParserException;
import com.bigbrassband.util.remittanceparse.remittance.RemittancePdf;
import com.bigbrassband.util.remittanceparse.report.RemittancePdfSalesMissingFromTransactionApiSales;
import com.bigbrassband.util.remittanceparse.report.TransactionApiSalesMissingFromRemittancePdf;
import com.bigbrassband.util.remittanceparse.report.refund.RefundMatcher;
import com.bigbrassband.util.remittanceparse.transaction.Transactions;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String args[]) throws IOException, ParserException {
        if(args.length!=2)
        {
            printUsage();
            System.exit(1);
        }

        //parse the remittance PDF and transactions JSON
        RemittancePdf remittancePdf = new RemittancePdf(new File(args[0]));
        Transactions transactions = new Transactions(new File(args[1]),
                remittancePdf.getMinimumDate(), remittancePdf.getMaximumDate());

        //Pass through the transactions API haystack
        RemittancePdfSalesMissingFromTransactionApiSales pdfSalesMissing =
                new RemittancePdfSalesMissingFromTransactionApiSales();
        RefundMatcher refundMatcher = new RefundMatcher();
        Keyed.haystackSearch(remittancePdf.getLines(), transactions.getTransactions(),
                pdfSalesMissing, refundMatcher.getRefundMatcherHelper());

        //Pass through the remittance PDF haystack
        TransactionApiSalesMissingFromRemittancePdf transactionApiSalesMissing =
                new TransactionApiSalesMissingFromRemittancePdf();
        Keyed.haystackSearch(transactions.getTransactions(), remittancePdf.getLines(),
                transactionApiSalesMissing, refundMatcher);

        //Print the report
        System.out.println("Reconciliation for " +
                Format.dateFormatter.format(remittancePdf.getMinimumDate()) + " - " +
                Format.dateFormatter.format(remittancePdf.getMaximumDate()));
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