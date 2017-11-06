package com.bigbrassband.util.remittanceparse.transaction;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

// Parses transactions  JSON file into in memory structure.
// This is date limited to the range of the remittance PDF.
public class Transactions {
    private final ArrayList<Transaction> transactions = new ArrayList<>();

    public Transactions(
            File inputJsonFile, Date minimumDate, Date maximumDate) throws IOException {
        JSONArray jsonArray = new JSONArray(
                FileUtils.readFileToString(inputJsonFile, StandardCharsets.UTF_8));

        for (int i = 0; i < jsonArray.length(); i++) {
            Transaction transaction = new Transaction(jsonArray.getJSONObject(i));
            final Date saleDate = transaction.getSaleDate();
            if (saleDate.compareTo(minimumDate) >= 0 && saleDate.compareTo(maximumDate) <= 0)
                transactions.add(transaction);
        }
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }
}
