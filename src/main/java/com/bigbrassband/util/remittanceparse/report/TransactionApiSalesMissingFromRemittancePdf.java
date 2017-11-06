package com.bigbrassband.util.remittanceparse.report;

import com.bigbrassband.util.remittanceparse.Keyed;
import com.bigbrassband.util.remittanceparse.remittance.RemittanceLine;
import com.bigbrassband.util.remittanceparse.transaction.Transaction;

import java.io.UnsupportedEncodingException;

// Report that calculates transaction API sales that are not present on the remittance PDF
public class TransactionApiSalesMissingFromRemittancePdf
        extends PrintSection implements Keyed.NeedleHandler<Transaction, RemittanceLine>

{
    private final Pennies totalPennies = new Pennies();

    public TransactionApiSalesMissingFromRemittancePdf() throws UnsupportedEncodingException {
        print.println("1. Transaction API Sales Missing from Remittance Report PDF:");
    }

    @Override
    public boolean test(Transaction transaction) {
        return !transaction.getSaleType().equals("Refund");
    }

    @Override
    public void accept(Transaction transaction, RemittanceLine remittanceLine) {
        if (remittanceLine == null) {
            print.println("\t" + transaction.getTransactionId() + " - " +
                    transaction.getSaleDateString() + " - " + transaction.getVendorAmountString());
            print.println("\t\t" + transaction.getSaleType() + " - " + transaction.getTier() + " - " + transaction.getHosting());
            totalPennies.add(transaction.getVendorAmountPennies());
        }
    }

    @Override
    public void done() {
        totalPennies.print(this);
    }

    public long getTotalPennies() {
        return totalPennies.getPennies();
    }
}
