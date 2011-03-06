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


public class PackageInfo implements Comparable<PackageInfo>{
    private String packageName;
    private String packageVersion;
    
    public PackageInfo(String packageName, String packageVersion) {
        this.packageName = packageName;
        this.packageVersion = packageVersion;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public String getPackageVersion() {
        return packageVersion;
    }
    
    @Override
    public int compareTo(PackageInfo another) {
        if (another != null) {
            int result = packageName.compareTo(another.packageName);
            if (result != 0){
                return result;
            }else{
                return packageVersion.compareTo(another.packageVersion);
            }
        } else {
            return -1;
        } 
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        final PackageInfo other = (PackageInfo) o;
        if (this.packageName != other.packageName && (this.packageName == null || !this.packageName.equals(other.packageName))) {
            return false;
        }
        if (this.packageVersion != other.packageVersion && (this.packageVersion == null || !this.packageVersion.equals(other.packageVersion))) {
            return false;
        }

        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 11;
        hash = 17* hash + (packageName != null ? packageName.hashCode():0);
        hash = 17 * hash + (packageVersion != null ? packageVersion.hashCode():0);

        return hash;
    }
}