package com.bigbrassband.util.remittanceparse.transaction;


import com.bigbrassband.util.remittanceparse.Format;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;


// JSON parsing
class JsonUtil {

    static String getString(JSONObject json, String... path) {
        return getHelper(json, path).getString(path[path.length - 1]);
    }

    static Date getIsoDate(JSONObject json, String... path) {
        try {
            return Format.isoDateFormat.parse(getString(json, path));
        } catch (ParseException e) {
            throw new JSONException(e);
        }
    }

    static double getDouble(JSONObject json, String... path) {
        return getHelper(json, path).getDouble(path[path.length - 1]);
    }

    private static JSONObject getHelper(JSONObject json, String[] path) {

        JSONObject headJson = json;

        for (int i = 0; i < path.length - 1; i++) {
            JSONObject jsonObject = headJson.optJSONObject(path[i]);
            if (jsonObject == null)
                throw new JSONException("Failed to find path " + StringUtils.join(path, "/", 0, i));
            headJson = jsonObject;
        }

        return headJson;
    }
}
