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
import org.apache.geronimo.kernel.log.GeronimoLogging;

/**
 *
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class MainConfigurationBootstrapper {
    public final static String ARGUMENT_VERBOSE_SHORTFORM = "-v";
    public final static String ARGUMENT_VERBOSE = "--verbose";
    public final static String ARGUMENT_MORE_VERBOSE_SHORTFORM = "-vv";
    public final static String ARGUMENT_MORE_VERBOSE = "--veryverbose";

    public static String getVerboseLevel(String[] args) {
        String verboseArg = null;
        for (int i = 0; i < args.length; i++) {
            verboseArg = filterVerboseArgument(args[i]);
            if (null != verboseArg) {
                break;
            }
        }
        return verboseArg;
    }

    public static String filterVerboseArgument(String arg) {
        if (arg.equals(ARGUMENT_VERBOSE_SHORTFORM) || arg.equals(ARGUMENT_VERBOSE)) {
            return ARGUMENT_VERBOSE;
        } else if (arg.equals(ARGUMENT_MORE_VERBOSE_SHORTFORM) || arg.equals(ARGUMENT_MORE_VERBOSE)) {
            return ARGUMENT_MORE_VERBOSE;
        }
        return null;
    }

    public static boolean isVerboseLevel(String verboseLevel) {
        return verboseLevel.equals(ARGUMENT_VERBOSE);
    }

    public static boolean isMoreVerboseLevel(String verboseLevel) {
        return verboseLevel.equals(ARGUMENT_MORE_VERBOSE);
    }

    public static void main(String[] args) {
        main(new MainConfigurationBootstrapper(), args);
    }

    public static void main(MainConfigurationBootstrapper bootstrapper, String[] args) {
        bootstrapper.initializeLogging(args);
        
        Main main = bootstrapper.getMain(MainConfigurationBootstrapper.class.getClassLoader());

        int exitCode;
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader newTCCL = main.getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(newTCCL);
            exitCode = main.execute(args);
        } finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
        System.exit(exitCode);
    }

    protected Kernel kernel;

    public Main getMain(ClassLoader classLoader) {
        try {
            bootKernel();
            loadBootConfiguration(classLoader);
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
    
    public void bootKernel() throws Exception {
        kernel = KernelFactory.newInstance().createKernel("MainBootstrapper");
        kernel.boot();

        Runtime.getRuntime().addShutdownHook(new Thread("MainBootstrapper shutdown thread") {
            public void run() {
                kernel.shutdown();
            }
        });
    }
    
    public void loadBootConfiguration(ClassLoader classLoader) throws Exception {
        InputStream in = classLoader.getResourceAsStream("META-INF/config.ser");
        try {
            ConfigurationUtil.loadBootstrapConfiguration(kernel, in, classLoader, true);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
    }
    
    public void loadPersistentConfigurations() throws Exception {
    }

    public Main getMain() throws Exception {
        return (Main) kernel.getGBean(Main.class);
    }

    public Kernel getKernel() {
        return kernel;
    }
    
    protected void initializeLogging(String[] args) {
        String verboseArg = getVerboseLevel(args);
        
        //
        // FIXME: Allow -v -> INFO, -vv -> DEBUG, -vvv -> TRACE
        //
        
        // This MUST be done before the first log is acquired (which the startup monitor below does)
        // Generally we want to suppress anything but WARN until the log GBean starts up
        GeronimoLogging level = GeronimoLogging.WARN;
        if (verboseArg != null) {
            if (isVerboseLevel(verboseArg)) {
                level = GeronimoLogging.DEBUG;
            } else if (isMoreVerboseLevel(verboseArg)) {
                level = GeronimoLogging.TRACE;
            }
        }
        GeronimoLogging.initialize(level);
    }
    
}
