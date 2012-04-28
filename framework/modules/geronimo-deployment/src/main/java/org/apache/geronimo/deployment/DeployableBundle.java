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
package org.apache.geronimo.deployment;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:386276 $ $Date$
 */
public class DeployableBundle implements Deployable {
    
    private static final Logger logger = LoggerFactory.getLogger(DeployableBundle.class);
    
    private Bundle bundle;
    
    public DeployableBundle(Bundle bundle) {
        this.bundle = bundle;
    }
    
    public URL getResource(String name) {
        try {
            return BundleUtils.getEntry(bundle, name);
        } catch (MalformedURLException e) {
            logger.warn("MalformedURLException when getting entry:" + name + " from bundle " + bundle.getSymbolicName(), e);
            return null;
        }
    }
    
    public void close() {        
    }
    
    public Bundle getBundle() {
        return bundle;
    }
    
}

