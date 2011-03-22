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
import org.osgi.framework.Constants;

public class BundleUtil {
    
    public static final String WEB_CONTEXT_PATH_HEADER = "Web-ContextPath";
    
    public static final String BLUEPRINT_HEADER = "Bundle-Blueprint";
    
    public static String getBundleName(Bundle bundle) {
        String name = (String) bundle.getHeaders().get(Constants.BUNDLE_NAME);
        name = (name == null) ? bundle.getSymbolicName() : name;
        name = (name == null) ? bundle.getLocation() : name;
        return name;
    }
    
    public static String getSymbolicName(Bundle bundle) {
        String name = bundle.getSymbolicName();
        if (name == null) {
            name = bundle.getLocation();
        }
        return name;
    }
}
