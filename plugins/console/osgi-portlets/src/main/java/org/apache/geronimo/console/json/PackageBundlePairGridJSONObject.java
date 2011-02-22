package org.apache.geronimo.console.json;

import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;



public class PackageBundlePairGridJSONObject extends JSONObject{
    public PackageBundlePairGridJSONObject(Set<PackageBundlePairJSONObject> items) throws JSONException{
        this.put("identifier", "fid");
        this.put("label", "description");
        
        this.put("items", items);
    }
}
