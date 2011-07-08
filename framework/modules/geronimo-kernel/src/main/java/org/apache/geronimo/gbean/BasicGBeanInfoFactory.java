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

package org.apache.geronimo.gbean;

import java.lang.reflect.Method;

import org.osgi.framework.Bundle;


/**
 *
 * @version $Rev:$ $Date:$
 */
public class BasicGBeanInfoFactory implements GBeanInfoFactory {

    /**
     * try not to use this one, it requires the *GBean class to be in the same classloader as the * class.
     * @param clazz
     * @return
     * @throws GBeanInfoFactoryException
     */
    public GBeanInfo getGBeanInfo(Class clazz) throws GBeanInfoFactoryException {
        Method method;
        try {
            method = clazz.getDeclaredMethod("getGBeanInfo", new Class[]{});
        } catch (NoSuchMethodException e) {
            try {
                // try to get the info from ${className}GBean
                clazz = clazz.getClassLoader().loadClass(clazz.getName() + "GBean");
                method = clazz.getDeclaredMethod("getGBeanInfo", new Class[]{});
            } catch (Exception ignored) {
                throw new GBeanInfoFactoryException("Class does not have a getGBeanInfo() method: " + clazz.getName());
            }
        } catch (NoClassDefFoundError e) {
            String message = e.getMessage();
            StringBuilder buf = new StringBuilder("Could not load gbean class ").append(clazz.getName()).append(" due to NoClassDefFoundError\n");
            if (message != null) {
                message = message.replace('/', '.');
                buf.append("    problematic class ").append(message);
                try {
                    Class hardToLoad = clazz.getClassLoader().loadClass(message);
                    buf.append(" can be loaded in supplied classloader ").append(clazz.getClassLoader()).append("\n");
                    buf.append("    and is found in ").append(hardToLoad.getClassLoader());
                } catch (ClassNotFoundException e1) {
                    buf.append(" cannot be loaded in supplied classloader ").append(clazz.getClassLoader()).append("\n");
                }
            }
            throw new GBeanInfoFactoryException(buf.toString(), e);
        }
        try {
            return (GBeanInfo) method.invoke(null, new Object[]{});
        } catch (Exception e) {
            throw new GBeanInfoFactoryException("Could not get GBeanInfo from class: " + clazz.getName(), e);
        } catch (NoClassDefFoundError e) {
            throw new GBeanInfoFactoryException("Could not get GBeanInfo from class: " + clazz.getName(), e);
        }
    }

    public GBeanInfo getGBeanInfo(String className, Bundle bundle) throws GBeanInfoFactoryException {
        Class clazz;
        try {
            clazz = bundle.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new GBeanInfoFactoryException("Could not load class " + className, e);
        } catch (NoClassDefFoundError e) {
            throw new GBeanInfoFactoryException("Could not load class " + className, e);
        }
        Method method;
        try {
            method = clazz.getDeclaredMethod("getGBeanInfo", new Class[]{});
        } catch (NoSuchMethodException e) {
            try {
                // try to get the info from ${className}GBean
                clazz = bundle.loadClass(className + "GBean");
                method = clazz.getDeclaredMethod("getGBeanInfo", new Class[]{});
            } catch (Exception ignored) {
                throw new GBeanInfoFactoryException("Class does not have a getGBeanInfo() method: " + className);
            }
        } catch (NoClassDefFoundError e) {
            String message = e.getMessage();
            StringBuilder buf = new StringBuilder("Could not load gbean class ").append(className).append(" due to NoClassDefFoundError\n");
            if (message != null) {
                message = message.replace('/', '.');
                buf.append("    problematic class ").append(message);
                try {
                    Class hardToLoad = bundle.loadClass(message);
                    buf.append(" can be loaded in supplied classloader ").append(bundle).append("\n");
                    buf.append("    and is found in ").append(hardToLoad.getClassLoader());
                } catch (ClassNotFoundException e1) {
                    buf.append(" cannot be loaded in supplied classloader ").append(bundle).append("\n");
                }
            }
            throw new GBeanInfoFactoryException(buf.toString(), e);
        }
        try {
            return (GBeanInfo) method.invoke(null, new Object[]{});
        } catch (Exception e) {
            throw new GBeanInfoFactoryException("Could not get GBeanInfo from class: " + className, e);
        } catch (NoClassDefFoundError e) {
            throw new GBeanInfoFactoryException("Could not get GBeanInfo from class: " + className, e);
        }
    }
    
}