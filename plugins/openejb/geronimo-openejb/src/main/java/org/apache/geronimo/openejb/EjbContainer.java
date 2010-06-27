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

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ManagedContainerInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.BmpEntityContainerInfo;
import org.apache.openejb.assembler.classic.CmpEntityContainerInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.SingletonSessionContainerInfo;

import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class EjbContainer implements GBeanLifecycle {
    private final OpenEjbSystem openEjbSystem;
    private final String id;
    private final Properties properties = new Properties();
    private final String provider;
    private final String type;
    private final Class<? extends ContainerInfo> infoType;

    public EjbContainer(AbstractName abstractName, Class<? extends ContainerInfo> infoType, OpenEjbSystem openEjbSystem, String provider, String type, Properties properties) {
        this.id = getId(abstractName);
        this.infoType = infoType;
        this.openEjbSystem = openEjbSystem;
        this.provider = provider;
        this.type = type;
        if (properties != null){
            this.properties.putAll(properties);
        }
    }

    public static String getId(AbstractName abstractName) {
        return (String) abstractName.getName().get("name");
    }

    protected Object set(String key, String value) {
        return properties.setProperty(key, value);
    }

    public String getId() {
        return id;
    }

    public OpenEjbSystem getOpenEjbSystem() {
        return openEjbSystem;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getProvider() {
        return provider;
    }

    public String getType() {
        return type;
    }

    public Class<? extends ContainerInfo> getInfoType() {
        return infoType;
    }

    public void doStart() throws Exception {
        openEjbSystem.createContainer(getInfoType(), id, properties, provider);
    }

    public void doStop() throws Exception {
    }

    public void doFail() {
    }

    public static Class<? extends ContainerInfo> getInfoType(String type) {
        if ("STATELESS".equalsIgnoreCase(type)) return StatelessSessionContainerInfo.class;
        if ("STATEFUL".equalsIgnoreCase(type)) return StatefulSessionContainerInfo.class;
        if ("BMP_ENTITY".equalsIgnoreCase(type)) return BmpEntityContainerInfo.class;
        if ("CMP_ENTITY".equalsIgnoreCase(type)) return CmpEntityContainerInfo.class;
        if ("CMP2_ENTITY".equalsIgnoreCase(type)) return CmpEntityContainerInfo.class;
        if ("MESSAGE".equalsIgnoreCase(type)) return MdbContainerInfo.class;
        if ("MANAGED".equalsIgnoreCase(type)) return ManagedContainerInfo.class;

        String className = type; // EjbModuleBuilder will pass in the className of the gbean
        if (className.endsWith("StatelessContainerGBean")) return StatelessSessionContainerInfo.class;
        if (className.endsWith("StatefulContainerGBean")) return StatefulSessionContainerInfo.class;
        if (className.endsWith("SingletonContainerGBean")) return SingletonSessionContainerInfo.class;
        if (className.endsWith("BmpContainerGBean")) return BmpEntityContainerInfo.class;
        if (className.endsWith("CmpContainerGBean")) return CmpEntityContainerInfo.class;
        if (className.endsWith("ManagedContainerGBean")) return ManagedContainerInfo.class;

        else return ContainerInfo.class;
    }

}
