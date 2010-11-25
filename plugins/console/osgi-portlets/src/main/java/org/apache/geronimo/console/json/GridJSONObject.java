package org.apache.geronimo.console.json;

import java.util.LinkedList;
import java.util.List;

import org.apache.geronimo.console.javabean.OSGiBundle;
import org.json.JSONException;
import org.json.JSONObject;
public class GridJSONObject extends JSONObject{
    
	public GridJSONObject(List<OSGiBundle> bundles) throws JSONException{
		this.put("identifier", "id");
		this.put("label", "description");
		
		List<BundleJSONObject> items=new LinkedList<BundleJSONObject>();
		for(OSGiBundle bundle:bundles){			
			items.add(new BundleJSONObject(bundle));
		}
		this.put("items", items);
	}
	
}
