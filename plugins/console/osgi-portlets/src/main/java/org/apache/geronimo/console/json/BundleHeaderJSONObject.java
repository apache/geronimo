package org.apache.geronimo.console.json;

import org.json.JSONException;
import org.json.JSONObject;

public class BundleHeaderJSONObject extends JSONObject{
    public BundleHeaderJSONObject(String key, String value) throws JSONException{
        this.put("hkey", key);
        this.put("hvalue", value);
    }
}
