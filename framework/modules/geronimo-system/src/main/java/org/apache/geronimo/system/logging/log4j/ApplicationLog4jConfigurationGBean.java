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


package org.apache.geronimo.system.logging.log4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev$ $Date$
 */
public class ApplicationLog4jConfigurationGBean {

    private static final String    ADDITIVITY_PREFIX = "log4j.additivity.";
    private static final String      CATEGORY_PREFIX = "log4j.category.";
    private static final String      LOGGER_PREFIX   = "log4j.logger.";
    private static final String      APPENDER_PREFIX = "log4j.appender.";
    private static final String      RENDERER_PREFIX = "log4j.renderer.";

    public ApplicationLog4jConfigurationGBean(String log4jResource, String log4jFile, ServerInfo serverInfo, ClassLoader classloader) throws IOException {
        InputStream in;
        if (log4jFile != null) {
            File file = serverInfo.resolveServer(log4jFile);
            in = new FileInputStream(file);
        } else if (log4jResource != null) {
            in = classloader.getResourceAsStream(log4jResource);
            if (in == null) {
                throw new NullPointerException("No log4j properties resource found at " + log4jResource);
            }
        } else {
            return;
        }
        Properties props = new Properties();
        try {
            props.load(in);
        } finally {
            in.close();
        }
        try {
            Class log4jConfigClass = classloader.loadClass("org.apache.log4j.PropertyConfigurator");
            if (log4jConfigClass.getClassLoader() == ClassLoader.getSystemClassLoader()) {
                //remove any global log4j configuration
                for (Iterator it = props.keySet().iterator(); it.hasNext();) {
                    String key = (String) it.next();
                    if (key.startsWith(CATEGORY_PREFIX)
                            || key.startsWith(LOGGER_PREFIX)
                            || key.startsWith(ADDITIVITY_PREFIX)
                            || key.startsWith(APPENDER_PREFIX)
                            || key.startsWith(RENDERER_PREFIX)) {
                        continue;
                    }
                    it.remove();
                }
            }
            log4jConfigClass.getMethod("configure", Properties.class).invoke(null, props);
        } catch (Exception e) {        
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ApplicationLog4jConfigurationGBean.class, "SystemLog");
        infoBuilder.setPriority(2);
        infoBuilder.addAttribute("log4jResource", String.class, true);
        infoBuilder.addAttribute("log4jFile", String.class, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);

        infoBuilder.addReference("ServerInfo", ServerInfo.class, "GBean");

        infoBuilder.setConstructor(new String[]{"log4jResource", "log4jFile", "ServerInfo", "classLoader"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
