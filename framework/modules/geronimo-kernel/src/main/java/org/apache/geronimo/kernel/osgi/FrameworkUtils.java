/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.kernel.osgi;

import java.io.IOException;
import java.net.URL;

import org.eclipse.osgi.service.urlconversion.URLConverter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * @version $Rev$ $Date$
 */
public class FrameworkUtils {

    private static final String USE_URL_CLASSLOADER = "org.apache.geronimo.equinox.useURLClassLoader";
    
    private static final boolean useURLClassLoader = initUseURLClassLoader();
    private static final boolean isEquinox = initIsEquinox();
    private static final Object urlConverter = initUrlConverter();
    
    private static boolean initUseURLClassLoader() {
        String property = System.getProperty(USE_URL_CLASSLOADER, "false");
        return Boolean.parseBoolean(property);        
    }
        
    private static boolean initIsEquinox() {
        Bundle bundle = FrameworkUtil.getBundle(FrameworkUtils.class);
        if (bundle != null) {
            bundle = bundle.getBundleContext().getBundle(0);
            if (bundle.getSymbolicName().startsWith("org.eclipse.osgi")) {
                return true;
            }
        }
        return false;
    }
    
    private static Object initUrlConverter() {
        if (isEquinox) {
            Bundle bundle = FrameworkUtil.getBundle(FrameworkUtils.class);
            BundleContext context = bundle.getBundleContext();           
            ServiceReference reference = context.getServiceReference(URLConverter.class.getName());
            if (reference != null) {
                return context.getService(reference);
            }
        }
        return null;
    }
    
    public static boolean isEquinox() {
        return isEquinox;
    }
    
    public static boolean useURLClassLoader() {
        return useURLClassLoader;
    }
    
    public static URL convertURL(URL bundleURL) {
        if (urlConverter != null && bundleURL != null) {
            try {
                return ((URLConverter) urlConverter).resolve(bundleURL);
            } catch (IOException e) {
                // ignore
            }
        }
        return bundleURL;        
    }
}
