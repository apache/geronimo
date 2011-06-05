/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.osgi.web;

import java.util.Hashtable;

import javax.servlet.ServletContext;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

public class WebApplicationUtils {

    public static boolean isWebApplicationBundle(Bundle bundle) {
        return (bundle != null && bundle.getHeaders().get(WABApplicationConstants.CONTEXT_PATH_HEADER) != null);
    }
    
    public static ServiceRegistration registerServletContext(BundleContext bundleContext, ServletContext context) {   
        return registerServletContext(bundleContext.getBundle(), context);
    }
    
    public static ServiceRegistration registerServletContext(Bundle bundle, ServletContext context) {        
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(WABApplicationConstants.WEB_SYMBOLIC_NAME, bundle.getSymbolicName());
        String version = (String) bundle.getHeaders().get(Constants.BUNDLE_VERSION);
        if (version != null) {
            properties.put(WABApplicationConstants.WEB_VERSION, version);
        }
        properties.put(WABApplicationConstants.WEB_CONTEXT_PATH, context.getContextPath());
        return bundle.getBundleContext().registerService(ServletContext.class.getName(), context, properties);
    }
    
}
