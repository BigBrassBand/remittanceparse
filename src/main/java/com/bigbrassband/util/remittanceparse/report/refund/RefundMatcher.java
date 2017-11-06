package com.bigbrassband.util.remittanceparse.report.refund;

import com.bigbrassband.util.remittanceparse.Format;
import com.bigbrassband.util.remittanceparse.Keyed;
import com.bigbrassband.util.remittanceparse.remittance.RemittanceLine;
import com.bigbrassband.util.remittanceparse.report.Pennies;
import com.bigbrassband.util.remittanceparse.report.PrintSection;
import com.bigbrassband.util.remittanceparse.transaction.Transaction;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.TreeMap;

// Calculates refunds that are missing
// This main class handles all of the transaction API refunds that are missing from the remittance PDF
// There's a helper class that handles some of the remittance PDF refunds that are missing from the transaction API
public class RefundMatcher
        extends PrintSection implements Keyed.NeedleHandler<Transaction, RemittanceLine> {

    final TreeMap<RefundKey, ArrayList<RemittanceLine>> remittanceLines = new TreeMap<>();
    private final RefundMatcherHelper refundMatcherHelper;
    private final Pennies totalPennies = new Pennies();
    private final Pennies refundMatcherHelperTotalPennies = new Pennies();

    public RefundMatcher() throws UnsupportedEncodingException {
        refundMatcherHelper = new RefundMatcherHelper(this);
        print.println("3. Transaction API Refunds Missing from Remittance Report PDF (heuristic*):");
    }

    public RefundMatcherHelper getRefundMatcherHelper() {
        return refundMatcherHelper;
    }

    @Override
    public boolean test(Transaction transaction) {
        return transaction.getSaleType().equals("Refund");
    }

    @Override
    public void accept(Transaction transaction, RemittanceLine remittanceLine) {

        final RefundKey refundKey = new RefundKey(transaction);
        final ArrayList<RemittanceLine> matchedRemittanceLines = this.remittanceLines.get(refundKey);
        if (matchedRemittanceLines != null) {
            matchedRemittanceLines.remove(matchedRemittanceLines.size() - 1);
            if (matchedRemittanceLines.isEmpty())
                remittanceLines.remove(refundKey);
        } else {
            print.println("\t" + transaction.getTransactionId() + " - " +
                    transaction.getSaleDateString() + " - " + transaction.getVendorAmountString());
            totalPennies.add(transaction.getVendorAmountPennies());
        }
    }

    @Override
    public void done() {

        totalPennies.print(this);

        print.println();

        print.println("4. Remittance Report PDF Refunds Missing from Transactions API (heuristic*):");
        for (ArrayList<RemittanceLine> lines : remittanceLines.values()) {
            for (RemittanceLine remittanceLine : lines) {
                print.println("\t" + Format.dateFormatter.format(remittanceLine.getDate()) + "\t" + remittanceLine.getReference() + "\t" + remittanceLine.getPaidString());
                refundMatcherHelperTotalPennies.add(remittanceLine.getPaidPennies());
            }
        }

        refundMatcherHelperTotalPennies.print(this);
    }

    public long getRemittancePdfRefundsMissedPennies() {
        return totalPennies.getPennies();
    }

    public long getTransactionsApiRefundsMissedPennies() {
        return refundMatcherHelperTotalPennies.getPennies();
    }
}
