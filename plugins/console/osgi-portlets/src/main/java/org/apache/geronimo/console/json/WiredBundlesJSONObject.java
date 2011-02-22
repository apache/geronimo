package org.apache.geronimo.console.json;

import org.json.JSONException;
import org.json.JSONObject;

public class WiredBundlesJSONObject extends JSONObject{
    public WiredBundlesJSONObject(PackageBundlePairGridJSONObject importingPairGrid, PackageBundlePairGridJSONObject exportingPairGrid) throws JSONException{
        this.put("importingBundles", importingPairGrid);
        this.put("exportingBundles", exportingPairGrid);
    }
    
}
