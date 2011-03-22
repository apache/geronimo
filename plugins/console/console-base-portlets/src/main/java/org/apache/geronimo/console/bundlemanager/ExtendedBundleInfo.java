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

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;

public class ExtendedBundleInfo extends SimpleBundleInfo {

    private List<BundleType> types = new ArrayList<BundleType>();
    private List<String> contextPaths = new ArrayList<String>();
    private BlueprintState blueprintState = null;

    public ExtendedBundleInfo(Bundle bundle) {
        super(bundle);
    }
    
    public List<BundleType> getTypes() {
        return types;
    }
    
    public void addType(BundleType type) {
        types.add(type);
    }

    public BlueprintState getBlueprintState(){
        return blueprintState;
    }
    
    public void setBlueprintState(BlueprintState state){
        blueprintState = state;
    }
    
    public boolean isOperable(){
        return !types.contains(BundleType.SYSTEM) && !types.contains(BundleType.CONFIGURATION);
    }

    public List<String> getContextPaths() {
        return contextPaths;
    }
    
    public void addContextPath(String path) {
        contextPaths.add(path);
    }

}

