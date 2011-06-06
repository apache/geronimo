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

package org.apache.geronimo.jaxws;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
@GBean(name = "JAXWSWebApplicationContext", j2eeType = "JAXWSWebApplicationContext")
public class JAXWSWebApplicationContext implements JAXWSApplicationContext, GBeanLifecycle {

    private static Map<String, JAXWSWebApplicationContext> webModuleNameJAXWSWebApplicationContextMap = new ConcurrentHashMap<String, JAXWSWebApplicationContext>();

    private Bundle bundle;

    private Map<String, PortInfo> servletNamePortInfoMap;

    private String webModuleName;

    public JAXWSWebApplicationContext(@ParamAttribute(name = "servletNamePortInfoMap") Map<String, PortInfo> servletNamePortInfoMap,
                                                                           @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
                                                                           @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName abName) {
        this.servletNamePortInfoMap = servletNamePortInfoMap;
        this.webModuleName = abName.getNameProperty(NameFactory.WEB_MODULE);
    }

    @Override
    public Collection<PortInfo> getPortInfos() {
        return Collections.unmodifiableCollection(servletNamePortInfoMap.values());
    }

    @Override
    public Set<String> getIds() {
        return Collections.unmodifiableSet(servletNamePortInfoMap.keySet());
    }

    @Override
    public PortInfo getPortInfo(String servletName) {
        return servletNamePortInfoMap.get(servletName);
    }

    @Override
    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
        }
    }

    @Override
    public void doStart() throws Exception {
        webModuleNameJAXWSWebApplicationContextMap.put(webModuleName, this);
    }

    @Override
    public void doStop() throws Exception {
        webModuleNameJAXWSWebApplicationContextMap.remove(webModuleName);
    }

    public static JAXWSWebApplicationContext get(String webModuleName) {
        return webModuleNameJAXWSWebApplicationContextMap.get(webModuleName);
    }
}
