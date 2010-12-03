/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.console.json;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import org.json.JSONException;
import org.json.JSONObject;

// FIXME should replaced by import org.apache.aries.util.ManifestHeaderUtils; in next release
import org.apache.aries.application.utils.manifest.ManifestHeaderProcessor;


public class HeadersJSONObject extends JSONObject{
    
    public HeadersJSONObject(Dictionary<String,String> headers) throws JSONException{
        this.put("identifier", "hkey");
        this.put("label", "description");
        
        List<BundleHeaderJSONObject> items=new LinkedList<BundleHeaderJSONObject>();
        
        Enumeration<String> keys = headers.keys();
        while(keys.hasMoreElements()){
            String key = (String)keys.nextElement();
            
            if (key.equals("Import-Package")||key.equals("Export-Package")||key.equals("Ignore-Package")||key.equals("Private-Package")){
                items.add(new BundleHeaderJSONObject(key, formatPackageHeader((String)headers.get(key))));
            } else {
                items.add(new BundleHeaderJSONObject(key, (String)headers.get(key)));
            }
        }
        
        this.put("items", items);
    }

    
    private String formatPackageHeader(String header){
        List<String> packagesList = ManifestHeaderProcessor.split(header, ",");
        Collections.sort(packagesList);

        String result = "";
        for (Iterator<String> it = packagesList.iterator(); it.hasNext(); result+=formatPackageValue(it.next())+"<BR/>");
        
        return result;
    }
    
    /*
     * Format a package value from
     * org.apache.aries.util.tracker;uses:="org.osgi.util.tracker,org.osgi.framework,org.osgi.framework.launch";version="0.2.0.incubating",
     * to
     * org.apache.aries.util.tracker;version="0.2.0.incubating",
     *     uses:="org.osgi.util.tracker,
     *         org.osgi.framework,
     *         org.osgi.framework.launch",
     */
    
    private String formatPackageValue(String pkg) {
        String result = "";
        
        String[] parts = pkg.split(";");
        
        if (parts!=null && parts.length>0) { //it should at least have a package name;
            result = parts[0]; 
        }
        
        String uses = "";
        for (int i=1; i<parts.length; i++){
            String aPart = parts[i];
            if (aPart.indexOf("uses")!=-1){  //deal with the "uses:=.." specially, because it always very long..
                uses+=";<BR/>&nbsp;&nbsp;&nbsp;&nbsp;"+"uses:="+"\"";
                
                String usesValue=aPart.substring(aPart.indexOf("\"")+1, aPart.lastIndexOf("\""));
                
                List<String> usesValueList = ManifestHeaderProcessor.split(usesValue,",");
                Collections.sort(usesValueList);
                Iterator<String> usesIT = usesValueList.iterator();
                if (usesIT.hasNext()){ //it should at least have a value;
                    uses += usesIT.next();
                }
                while (usesIT.hasNext()){
                    uses += ",<BR/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+usesIT.next();
                }
                
                uses+="\"";
                
            } else {  //maybe "version=..", maybe "resolution=.."
                result+=";"+aPart;
            }
        }
        
        if (uses!="") result+=uses;
        
        return result+",";
    }
    
    
}
