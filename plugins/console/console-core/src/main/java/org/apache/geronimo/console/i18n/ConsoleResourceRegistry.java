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

package org.apache.geronimo.console.i18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * Each console extension will register its classloader to this GBean, then
 * console-portal-driver can load the resource bundle for the above classloader.
 */
public class ConsoleResourceRegistry {
    
    private static final Locale defaultLocale = new Locale("");
    private static Map<String, Locale> localeMap;
    
    static {
        localeMap = new HashMap<String, Locale>();
    }

    private List<ClassLoader> classloaders = new ArrayList<ClassLoader>();

    /**
     * Register the classloader of the console extension
     *
     * @param classloader console plugin classloader to register
     */
    public void registerConsoleResource(ClassLoader classloader) {
        classloaders.add(classloader);
    }

    /**
     * Remove the classloader of the console extension
     * @param classloader console plugin classloader to unregister
     */
    public void removeConsoleResource(ClassLoader classloader) {
        classloaders.remove(classloader);
    }

    /**
     * Iterate the classloaders of the console extensions to find the value
     * related to the key.
     * @param basename name of the bundle
     * @param locale locale desired
     * @param key key for desired string
     */
    public String handleGetObject(String basename, Locale locale, String key) {
        if (localeMap.containsKey(locale.getLanguage())) {
            locale = localeMap.get(locale.getLanguage());
        }
        for (ClassLoader classloader : classloaders) {
            try {
                ResourceBundle rb = ResourceBundle.getBundle(basename, locale, classloader);            
                if (null != rb) {
                    if (!locale.getLanguage().equals(rb.getLocale().getLanguage())) {
                        localeMap.put(locale.getLanguage(), defaultLocale);
                        rb = ResourceBundle.getBundle(basename, defaultLocale, classloader);
                    }
                    String value = rb.getString(key);
                    if (null != value) {
                        return value;
                    }
                }
            } catch (MissingResourceException e) {
                continue;
            }
        }

        return null;
    }

    /*
    * Standard GBean information
    */
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("ConsoleResourceRegistry", ConsoleResourceRegistry.class);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
