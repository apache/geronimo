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
package org.apache.geronimo.kernel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.geronimo.kernel.basic.BasicKernelFactory;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
public abstract class KernelFactory {
    public static final String KERNEL_FACTORY_KEY = KernelFactory.class.getName();

    private BundleContext bundleContext;

    public static KernelFactory newInstance(BundleContext bundleContext) {
        // System property
        try {
            String kernelFactoryName = System.getProperty(KERNEL_FACTORY_KEY);
            if (kernelFactoryName != null) {
                return createKernelFactory(kernelFactoryName, bundleContext);
            }
        } catch (SecurityException se) {
        }

        // Jar Service Specification - http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html
        String serviceId = "META-INF/services/" + KERNEL_FACTORY_KEY;
        InputStream inputStream = null;
        try {
            inputStream = bundleContext.getBundle().getResource(serviceId).openStream();
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String kernelFactoryName = reader.readLine();
                reader.close();

                if (kernelFactoryName != null && kernelFactoryName.length() > 0) {
                    return createKernelFactory(kernelFactoryName, bundleContext);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
                inputStream = null;
            }
        }

        // Default is the basic kernel
        KernelFactory kernelFactory = new BasicKernelFactory();
        kernelFactory.bundleContext = bundleContext;
        return kernelFactory;
    }

    private static KernelFactory createKernelFactory(String className, BundleContext bundleContext) {
        try {
            KernelFactory kernelFactory = (KernelFactory) bundleContext.getBundle().loadClass(className).newInstance();
            kernelFactory.bundleContext = bundleContext;
            return kernelFactory;
        } catch (ClassCastException e) {
            throw new KernelFactoryError("Kernel factory class does not implement KernelFactory: " + className, e);
        } catch (ClassNotFoundException e) {
            throw new KernelFactoryError("Kernel factory class not found: " + className, e);
        } catch (Exception e) {
            throw new KernelFactoryError("Unable to instantiate kernel factory class: " + className, e);
        }
    }

    public abstract Kernel createKernel(String kernelName);

    public BundleContext getBundleContext() {
        return bundleContext;
    }
}
