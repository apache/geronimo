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
package org.apache.geronimo.kernel.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.osgi.framework.BundleContext;

/**
 *
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class MainConfigurationBootstrapper {

//    public static void main(String[] args) {
//        int status = main(new MainConfigurationBootstrapper(), args, bundle);
//        System.exit(status);
//    }

    public static int main(MainConfigurationBootstrapper bootstrapper, Object opaque, BundleContext bundleContext) {
        Main main = bootstrapper.getMain(bundleContext);

        int exitCode;
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader newTCCL = main.getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(newTCCL);
            exitCode = main.execute(opaque);
        } finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
        return exitCode;
    }

    protected Kernel kernel;

    public Main getMain(BundleContext bundleContext) {
        try {
            bootKernel(bundleContext);
            loadBootConfiguration(bundleContext);
            loadPersistentConfigurations();
            return getMain();
        } catch (Exception e) {
            if (null != kernel) {
                kernel.shutdown();
            }
            e.printStackTrace();
            System.exit(1);
            throw new AssertionError();
        }
    }
    
    public void bootKernel(BundleContext bundleContext) throws Exception {
        kernel = KernelFactory.newInstance(bundleContext).createKernel("MainBootstrapper");
        kernel.boot(bundleContext);

        Runtime.getRuntime().addShutdownHook(new Thread("MainBootstrapper shutdown thread") {
            public void run() {
                kernel.shutdown();
            }
        });
    }
    
    public void loadBootConfiguration(BundleContext bundleContext) throws Exception {
//        InputStream in = bundleContext.getBundle().getResource("META-INF/config.ser").openStream();
//        try {
//            ConfigurationUtil.loadBootstrapConfiguration(kernel, in, bundleContext, true, configurationManager);
//        } finally {
//            if (in != null) {
//                try {
//                    in.close();
//                } catch (IOException ignored) {
//                    // ignored
//                }
//            }
//        }
    }
    
    public void loadPersistentConfigurations() throws Exception {
    }

    public Main getMain() throws Exception {
        return (Main) kernel.getGBean(Main.class);
    }

    public Kernel getKernel() {
        return kernel;
    }
    
}
