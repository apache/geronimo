/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.kernel.config;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.log.GeronimoLogging;

/**
 * @version $Rev$ $Date$
 */
public class Run {
    static {
        // This MUST be done before the first log is acquired
        GeronimoLogging.initialize(GeronimoLogging.INFO);
    }

    private final static String[] MAIN_ARGS = {String[].class.getName()};

    public static void main(String[] args) {
        ClassLoader cl = Run.class.getClassLoader();
        ObjectName gbeanName;

        try {
            InputStream is = cl.getResourceAsStream("META-INF/MANIFEST.MF");
            try {
                Manifest mf = new Manifest(is);
                Attributes attrs = mf.getMainAttributes();
                gbeanName = new ObjectName(attrs.getValue("Geronimo-GBean"));
            } finally {
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            throw new AssertionError();
        }

        final Kernel kernel = new Kernel("geronimo.kernel", "geronimo");
        try {
            kernel.boot();

            GBeanMBean config = new GBeanMBean(Configuration.GBEAN_INFO, cl);
            ObjectInputStream ois = new ObjectInputStream(cl.getResourceAsStream("META-INF/config.ser"));
            try {
                Configuration.loadGMBeanState(config, ois);
            } finally {
                ois.close();
            }
            final ObjectName configName = kernel.getConfigurationManager().load(config, cl.getResource("/"));

            Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Thread") {
                public void run() {
                    try {
                        kernel.stopGBean(configName);
                    } catch (Exception e) {
                        // ignore
                    }
                    kernel.shutdown();
                }
            });

            kernel.startGBean(configName);
            kernel.getMBeanServer().invoke(gbeanName, "main", new Object[]{args}, MAIN_ARGS);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
            throw new AssertionError();
        }
    }
}
