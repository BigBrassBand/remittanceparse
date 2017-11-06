package com.bigbrassband.util.remittanceparse.report;

import com.bigbrassband.util.remittanceparse.Keyed;
import com.bigbrassband.util.remittanceparse.remittance.RemittanceLine;
import com.bigbrassband.util.remittanceparse.transaction.Transaction;

import java.io.UnsupportedEncodingException;

// Report that calculates remittance PDF sale lines that are not present in Transaction API
public class RemittancePdfSalesMissingFromTransactionApiSales
        extends PrintSection implements Keyed.NeedleHandler<RemittanceLine, Transaction> {
    private final Pennies totalPennies = new Pennies();

    public RemittancePdfSalesMissingFromTransactionApiSales() throws UnsupportedEncodingException {
        print.println("2. Remittance Report PDF Sales Missing from Transactions API:");
    }

    @Override
    public boolean test(RemittanceLine remittanceLine) {
        return remittanceLine.getPaidPennies() > 0L;
    }

    @Override
    public void accept(RemittanceLine remittanceLine, Transaction transaction) {
        if (transaction == null) {
            print.println("\t" + remittanceLine.getDate() + "\t" + remittanceLine.getReference() + "\t" + remittanceLine.getPaidString());
            totalPennies.add(remittanceLine.getPaidPennies());
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
