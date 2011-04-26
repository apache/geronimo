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

package org.apache.geronimo.system.main;

import java.io.IOException;
import java.io.InputStream;

import org.apache.geronimo.cli.CLParserException;
import org.apache.geronimo.cli.daemon.DaemonCLParser;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.osgi.framework.BundleContext;


/**
 * @version $Rev:385659 $ $Date$
 */
public class Daemon extends EmbeddedDaemon {

    private Daemon() {
        //TODO osgi bundleContext == null
        super(KernelFactory.newInstance(null).createKernel("geronimo"), null);
    }

    @Override
    protected int initializeKernel() throws Exception {
        ClassLoader classLoader = EmbeddedDaemon.class.getClassLoader();
        BundleContext bundleContext = null;

        // boot the kernel
        try {
            kernel.boot();
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

        // add our shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread("Geronimo shutdown thread") {
            public void run() {
                System.out.println("");
                System.out.println("Server shutdown started");
                kernel.shutdown();
                System.out.println("Server shutdown completed");
            }
        });

        // load this configuration
        InputStream in = classLoader.getResourceAsStream("META-INF/config.ser");
        try {
            ConfigurationUtil.loadBootstrapConfiguration(kernel, in, bundleContext);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
        return 0;
    }

    /**
     * Static entry point allowing a Kernel to be run from the command line.
     *
     * Once the Kernel is booted and the configuration is loaded, the process
     * will remain running until the shutdown() method on the kernel is
     * invoked or until the JVM exits.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DaemonCLParser parser = new DaemonCLParser(System.out);
        try {
            parser.parse(args);
        } catch (CLParserException e) {
            System.err.println(e.getMessage());
            parser.displayHelp();
            System.exit(1);
        }
        new Daemon().execute(parser);
    }
}
