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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

/**
 * This GBean provides a way for console extension to register its classloader to
 * ConsoleResourceRegistry. ConsoleResourceRegistry will use the classloaders to
 * load the resource bundle.
 */
public class ConsoleResourceGBean implements GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(ConsoleResourceGBean.class);
    public static final GBeanInfo GBEAN_INFO;
    private ClassLoader classloader;
    private ConsoleResourceRegistry consoleResourceRegistry;

    public ConsoleResourceGBean(ClassLoader classloader, ConsoleResourceRegistry consoleResourceRegistry) {
        this.classloader = classloader;
        this.consoleResourceRegistry = consoleResourceRegistry;
    }

    /*
    * Called when the GBean is started
    * @see org.apache.geronimo.gbean.GBeanLifecycle#doStart()
    */
    public synchronized void doStart() throws Exception {
        consoleResourceRegistry.registerConsoleResource(classloader);
    }

    /*
     * Called when the GBean is stopped
     * @see org.apache.geronimo.gbean.GBeanLifecycle#doStop()
     */
    public synchronized void doStop() throws Exception {
        consoleResourceRegistry.removeConsoleResource(classloader);
    }

    /*
    * Called when the GBean fails
    * @see org.apache.geronimo.gbean.GBeanLifecycle#doFail()
    */
    public synchronized void doFail() {
        log.warn("AdminConsoleExtensionGBean for failed.");
    }

    /*
    * Standard GBean information
    */
    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("ConsoleResourceGBean", ConsoleResourceGBean.class);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addReference("ConsoleResourceRegistry", ConsoleResourceRegistry.class, null);
        infoFactory.setConstructor(new String[]{
                "classLoader",
                "ConsoleResourceRegistry"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
