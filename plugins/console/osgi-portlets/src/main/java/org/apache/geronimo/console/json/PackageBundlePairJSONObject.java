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
