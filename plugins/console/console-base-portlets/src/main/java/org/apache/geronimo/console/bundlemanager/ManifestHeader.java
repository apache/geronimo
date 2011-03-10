/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.console.bundlemanager;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.aries.application.utils.manifest.ManifestHeaderProcessor;

public class ManifestHeader implements Comparable<ManifestHeader>{

    private String key;
    private String value;
    
    public ManifestHeader(String key, String value){
        this.key = key;
        this.value = value;
    }
    
    public int compareTo(ManifestHeader another) {
        if (another != null) {
            return key.compareTo(another.key);
        } else {
            return -1;
        }
    }
    
    public String getKey() {
        return key;
    }
    
    public String getValue() {
        return value;
    }
    
    public static String formatPackageHeader(String header){
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

    private static String formatPackageValue(String pkg) {
        String result = "";
        
        String[] parts = pkg.split(";");
        
        if (parts!=null && parts.length>0) { //it should at least have a package name;
            result = parts[0]; 
        }
        
        String uses = "";
        for (int i=1; i<parts.length;i++){
            String aPart = parts[i];
            if (aPart.indexOf("uses:=")!=-1){  //deal with the "uses:=.." specially, because it always very long..
                uses+=";<BR/>&nbsp;&nbsp;&nbsp;&nbsp;"+"uses:="+"\"";
                
                String usesValue = "";
                if (aPart.indexOf("\"")!=-1){
                    usesValue = aPart.substring(aPart.indexOf("\"")+1, aPart.lastIndexOf("\""));
                } else {  // for such using uses:=javassist;
                    usesValue = aPart.substring(aPart.indexOf(":=")+2);
                }
                
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
                result += ";"+aPart;
            }
        }
        
        if (uses!="") result += uses;
        
        return result+",";
    }
    
}


