/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.concurrent.spi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

class FactoryFinder {

    private static Object newInstance(String factoryClassName) throws Exception {
        ClassLoader classloader = null;
        try {
            classloader = Thread.currentThread().getContextClassLoader();
        } catch (Exception exception) {
            throw new Exception(exception.toString(), exception);
        }

        try {
            Class factory = null;
            if (classloader == null) {
                factory = Class.forName(factoryClassName);
            } else {
                try {
                    factory = classloader.loadClass(factoryClassName);
                } catch (ClassNotFoundException cnfe) {
                }
            }
            if (factory == null) {
                classloader = FactoryFinder.class.getClassLoader();
                factory = classloader.loadClass(factoryClassName);
            }
            return factory.newInstance();
        } catch (ClassNotFoundException classnotfoundexception) {
            throw new Exception(
                    "Provider " + factoryClassName + " not found",
                    classnotfoundexception);
        } catch (Exception exception) {
            throw new Exception(
                    "Provider " + factoryClassName + " could not be instantiated: " + exception,
                    exception);
        }
    }

    static Object find(String factoryPropertyName,
                       String defaultFactoryClassName) throws Exception {
        try {
            String factoryClassName = System.getProperty(factoryPropertyName);
            if (factoryClassName != null) {
                return newInstance(factoryClassName);
            }
        } catch (SecurityException securityexception) {
        }

        try {
            String propertiesFileName = System.getProperty("java.home")
                    + File.separator + "lib"
                    + File.separator + "concurrent.properties";
            File file = new File(propertiesFileName);
            if (file.exists()) {
                FileInputStream fileInput = new FileInputStream(file);
                Properties properties = new Properties();
                properties.load(fileInput);
                fileInput.close();
                String factoryClassName = properties.getProperty(factoryPropertyName);
                return newInstance(factoryClassName);
            }
        } catch (Exception exception1) {
        }

        String factoryResource = "META-INF/services/" + factoryPropertyName;

        try {
            InputStream inputstream = getResource(factoryResource);
            if (inputstream != null) {
                BufferedReader bufferedreader = 
                    new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
                String factoryClassName = bufferedreader.readLine();
                bufferedreader.close();
                if ((factoryClassName != null) && !"".equals(factoryClassName)) {
                    try {
                        return newInstance(factoryClassName);
                    } catch (Exception e) {
                        throw new Exception(
                                "Provider for " + factoryPropertyName + " cannot be found");
                    }
                }
            }
        } catch (Exception exception2) {
        }

        if (defaultFactoryClassName == null) {
            throw new Exception(
                    "Provider for " + factoryPropertyName + " cannot be found");
        } else {
            return newInstance(defaultFactoryClassName);
        }
    }

    private static InputStream getResource(String factoryResource) {
        ClassLoader classloader = null;
        try {
            classloader = Thread.currentThread().getContextClassLoader();
        } catch (SecurityException securityexception) {
        }

        InputStream inputstream;
        if (classloader == null) {
            inputstream =
                    ClassLoader.getSystemResourceAsStream(factoryResource);
        } else {
            inputstream = classloader.getResourceAsStream(factoryResource);
        }

        if (inputstream == null) {
            inputstream =
                    FactoryFinder.class.getClassLoader().getResourceAsStream(factoryResource);
        }
        return inputstream;
    }
}
