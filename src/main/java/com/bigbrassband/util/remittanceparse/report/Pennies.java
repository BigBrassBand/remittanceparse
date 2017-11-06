package com.bigbrassband.util.remittanceparse.report;

import com.bigbrassband.util.remittanceparse.Format;

// Represents a pile of pennies
public class Pennies {
    private long pennies = 0L;
    private int transactionCount = 0;

    public void add(long pennies) {
        this.pennies += pennies;
        transactionCount++;
    }

    @Override
    public String toString() {
        return Format.penniesString(pennies);
    }

    public long getPennies() {
        return pennies;
    }

    public void print(PrintSection printSection) {
        if (transactionCount > 0) {
            printSection.print.println("Total: " + toString());
        } else {
            printSection.print.println("\tNO DATA MISSING");
        }
    }
}
