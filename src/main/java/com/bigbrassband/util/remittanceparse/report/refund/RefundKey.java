package com.bigbrassband.util.remittanceparse.report.refund;

import com.bigbrassband.util.remittanceparse.remittance.RemittanceLine;
import com.bigbrassband.util.remittanceparse.transaction.Transaction;

import java.util.Date;

class RefundKey implements Comparable<RefundKey> {
    private final long pennies;
    private final Date date;

    RefundKey(RemittanceLine remittanceLine) {
        pennies = remittanceLine.getPaidPennies();
        date = remittanceLine.getDate();
    }

    RefundKey(Transaction transaction) {
        pennies = transaction.getVendorAmountPennies();
        date = transaction.getSaleDate();
    }

    @Override
    public int compareTo(RefundKey o) {
        int i = date.compareTo(o.date);
        return i != 0 ? i : Long.compare(pennies, o.pennies);
    }
}
