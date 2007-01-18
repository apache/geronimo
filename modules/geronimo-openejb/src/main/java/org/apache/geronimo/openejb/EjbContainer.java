/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb;

import java.util.Properties;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.openejb.assembler.classic.ContainerInfo;

/**
 * @version $Rev$ $Date$
 */
public class EjbContainer implements GBeanLifecycle {
    private OpenEjbSystem openEjbSystem;
    private String id;
    private Properties properties;
    private String provider;
    private Class<ContainerInfo> type = ContainerInfo.class;

    public OpenEjbSystem getOpenEjbSystem() {
        return openEjbSystem;
    }

    public void setOpenEjbSystem(OpenEjbSystem openEjbSystem) {
        this.openEjbSystem = openEjbSystem;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Class<ContainerInfo> getType() {
        return type;
    }

    public void setType(Class<ContainerInfo> type) {
        this.type = type;
    }

    public void doStart() throws Exception {
        openEjbSystem.createContainer(type, id, properties, provider);
    }

    public void doStop() throws Exception {
    }

    public void doFail() {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(EjbContainer.class);
        infoBuilder.addReference("OpenEjbSystem", OpenEjbSystem.class);
        infoBuilder.addAttribute("id", String.class, true);
        infoBuilder.addAttribute("properties", Properties.class, true);
        infoBuilder.addAttribute("provider", String.class, true);
        infoBuilder.addAttribute("type", Class.class, true);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
