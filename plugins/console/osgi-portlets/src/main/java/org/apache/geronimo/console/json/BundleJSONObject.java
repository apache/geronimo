package org.apache.geronimo.console.json;

import org.apache.geronimo.console.javabean.OSGiBundle;
import org.json.JSONException;
import org.json.JSONObject;
public class BundleJSONObject extends JSONObject{
	public BundleJSONObject(OSGiBundle bundle) throws JSONException{
		this.put("id", bundle.getIdentifier());
		this.put("symbolicName", bundle.getSymbolicName());
		this.put("version",bundle.getVersion());
		this.put("state", bundle.getState());
	}

}
