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

import org.osgi.framework.Bundle;

public class SimpleBundleInfo implements BundleInfo{
    private final long bundleId;
    private final String symbolicName;
    private final String bundleVersion;
    private final String bundleName;
    private final BundleState state;
    
    public SimpleBundleInfo(Bundle bundle){
        this.bundleId = bundle.getBundleId();
        this.symbolicName = BundleUtil.getSymbolicName(bundle);
        this.bundleName = BundleUtil.getBundleName(bundle);
        this.bundleVersion = bundle.getVersion().toString();
        this.state = BundleState.getState(bundle);
    }
    
    public long getBundleId() {
        return bundleId;
    }
    
    public String getSymbolicName() {
        return symbolicName;
    }
    
    public String getBundleVersion() {
        return bundleVersion;
    }
    
    public String getBundleName() {
        return bundleName;
    }
    
    
    public BundleState getState() {
        return state;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        final SimpleBundleInfo other = (SimpleBundleInfo) o;
        if (this.bundleId != other.bundleId){
            return false;
        }
        if (this.symbolicName != other.symbolicName && (this.symbolicName == null || !this.symbolicName.equals(other.symbolicName))) {
            return false;
        }
        if (this.bundleVersion != other.bundleVersion && (this.bundleVersion == null || !this.bundleVersion.equals(other.bundleVersion))) {
            return false;
        }

        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 11;
        hash = hash + (int)bundleId;
        hash = 17* hash + (symbolicName != null ? symbolicName.hashCode():0);
        hash = 17 * hash + (bundleVersion != null ? bundleVersion.hashCode():0);

        return hash;
    }
    

}

