package com.bigbrassband.util.remittanceparse.transaction;

import com.bigbrassband.util.remittanceparse.Format;
import com.bigbrassband.util.remittanceparse.Keyed;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Represnts transaction from Transactions API
public class Transaction extends Keyed {
    private static final Pattern transactionIdPattern = Pattern.compile("(AT-)?(.+)");
    private final JSONObject jsonObject;
    private final String id;
    private final long pennies;

    Transaction(JSONObject jsonObject) throws IOException {
        final Matcher matcher = transactionIdPattern.matcher(JsonUtil.getString(jsonObject, "transactionId"));
        if (!matcher.matches())
            throw new IOException("Can not parse transaction ID " + JsonUtil.getString(jsonObject, "transactionId"));
        id = matcher.group(2);
        pennies = Math.round(JsonUtil.getDouble(jsonObject, "purchaseDetails", "vendorAmount") * 100.0);
        this.jsonObject = jsonObject;
    }

    public Date getSaleDate() {
        return JsonUtil.getIsoDate(jsonObject, "purchaseDetails", "saleDate");
    }

    public String getTransactionId() {
        return JsonUtil.getString(jsonObject, "transactionId");
    }

    public long getVendorAmountPennies() {
        return getPennies();
    }

    public String getVendorAmountString() {
        return Format.penniesString(getVendorAmountPennies());
    }

    public String getTier() {
        return JsonUtil.getString(jsonObject, "purchaseDetails", "tier");
    }

    public String getHosting() {
        return JsonUtil.getString(jsonObject, "purchaseDetails", "hosting");
    }

    public String getSaleType() {
        return JsonUtil.getString(jsonObject, "purchaseDetails", "saleType");
    }

    public String getSaleDateString() {
        return JsonUtil.getString(jsonObject, "purchaseDetails", "saleDate");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    protected long getPennies() {
        return pennies;
    }
}
