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
package org.apache.geronimo.client;

import java.util.Properties;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.omg.CORBA.ORB;

/**
 * @version $Rev$ $Date$
 */
public class AppClientCORBABean implements GBeanLifecycle {
    private final ClassLoader classLoader;
    private ORB orb;

    public AppClientCORBABean(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void doStart() throws Exception {
        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            orb = ORB.init(new String[0], new Properties());
            new Thread(new ORBRunable(orb), "ORBInitialization").start();
        } finally {
            Thread.currentThread().setContextClassLoader(savedLoader);
        }
    }

    public void doStop() throws Exception {
        orb.shutdown(true);
    }

    public void doFail() {
        orb.shutdown(false);
    }

    public ORB getORB() {
        return orb;
    }

    private static final class ORBRunable implements Runnable {
        private final ORB orb;

        public ORBRunable(ORB orb) {
            this.orb = orb;
        }

        public void run() {
            orb.run();
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(AppClientCORBABean.class);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("ORB", ORB.class, false);

        infoFactory.setConstructor(new String[]{"classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
