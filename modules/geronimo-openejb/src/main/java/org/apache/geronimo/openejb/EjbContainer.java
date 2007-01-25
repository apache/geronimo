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
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.BmpEntityContainerInfo;
import org.apache.openejb.assembler.classic.CmpEntityContainerInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;

/**
 * @version $Rev$ $Date$
 */
public class EjbContainer implements GBeanLifecycle {
    private OpenEjbSystem openEjbSystem;
    private String id;
    private Properties properties;
    private String provider;
    private String type;
    private Class<? extends ContainerInfo> infoType;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private Class<? extends ContainerInfo> getInfoType(String type) {
        if ("STATELESS".equalsIgnoreCase(type)) return StatelessSessionContainerInfo.class;
        if ("STATEFUL".equalsIgnoreCase(type)) return StatefulSessionContainerInfo.class;
        if ("BMP_ENTITY".equalsIgnoreCase(type)) return BmpEntityContainerInfo.class;
        if ("CMP_ENTITY".equalsIgnoreCase(type)) return CmpEntityContainerInfo.class;
        if ("CMP2_ENTITY".equalsIgnoreCase(type)) return CmpEntityContainerInfo.class;
        if ("MESSAGE".equalsIgnoreCase(type)) return MdbContainerInfo.class;
        else return ContainerInfo.class;
    }

    public Class<? extends ContainerInfo> getInfoType() {
        return infoType == null? getInfoType(type): infoType;
    }

    public void setInfoType(Class<? extends ContainerInfo> infoType) {
        this.infoType = infoType;
    }

    public void doStart() throws Exception {
        openEjbSystem.createContainer(getInfoType(), id, properties, provider);
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
        infoBuilder.addAttribute("type", String.class, true);
        infoBuilder.addAttribute("infoType", Class.class, true);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
