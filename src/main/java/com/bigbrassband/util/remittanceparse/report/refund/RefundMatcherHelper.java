package com.bigbrassband.util.remittanceparse.report.refund;

import com.bigbrassband.util.remittanceparse.Keyed;
import com.bigbrassband.util.remittanceparse.remittance.RemittanceLine;
import com.bigbrassband.util.remittanceparse.transaction.Transaction;

import java.util.ArrayList;

// Helper for RefundMatcher
// Handles some of the remittance PDF refunds that are missing from the transaction API
class RefundMatcherHelper implements Keyed.NeedleHandler<RemittanceLine, Transaction> {
    private final RefundMatcher refundMatcher;

    RefundMatcherHelper(RefundMatcher refundMatcher) {
        this.refundMatcher = refundMatcher;
    }

    @Override
    public boolean test(RemittanceLine remittanceLine) {
        return remittanceLine.getPaidPennies() < 0;
    }

    @Override
    public void accept(RemittanceLine remittanceLine, Transaction transaction) {
        final RefundKey refundKey = new RefundKey(remittanceLine);
        ArrayList<RemittanceLine> lines = refundMatcher.remittanceLines.computeIfAbsent(refundKey, k -> new ArrayList<>());
        lines.add(remittanceLine);
    }

    @Override
    public void done() {

    }
}
