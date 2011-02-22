package org.apache.geronimo.console.json;

import org.json.JSONException;
import org.json.JSONObject;


public class PackageBundlePairJSONObject extends JSONObject {
    
    String pname;
    String bname;
    
    public PackageBundlePairJSONObject(int fakeId, String packageName, String bundleName) throws JSONException{
        this.put("fid", fakeId); //use as the identifier in datagrid's store
        
        this.put("pname", packageName);
        this.put("bname", bundleName);
        
        pname=packageName;
        bname=bundleName;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final PackageBundlePairJSONObject other = (PackageBundlePairJSONObject) obj;
        if (this.pname != other.pname && (this.pname == null || !this.pname.equals(other.pname))) {
            return false;
        }
        if (this.bname != other.bname && (this.bname == null || !this.bname.equals(other.bname))) {
            return false;
        }

        return true;
        
    }
    
    @Override
    public int hashCode() {
        int hash = 11;
        hash = 17 * hash + (pname != null ? pname.hashCode():0);
        hash = 17 * hash + (bname != null ? bname.hashCode():0);

        return hash;
    }
    
}
