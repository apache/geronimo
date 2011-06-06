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

@GBean(name = "JAXWSEJBApplicationContext", j2eeType = "JAXWSEJBApplicationContext")
public class JAXWSEJBApplicationContext implements JAXWSApplicationContext, GBeanLifecycle {

    private static Map<String, JAXWSEJBApplicationContext> ejbModuleNameJAXWSEJBApplicationContextMap = new ConcurrentHashMap<String, JAXWSEJBApplicationContext>();

    private Bundle bundle;

    private Map<String, PortInfo> ejbNamePortInfoMap;

    private String ejbModuleName;

    public JAXWSEJBApplicationContext(@ParamAttribute(name = "ejbNamePortInfoMap") Map<String, PortInfo> ejbNamePortInfoMap,
                                                                        @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
                                                                        @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName abName) {
        this.ejbNamePortInfoMap = ejbNamePortInfoMap;
        this.bundle = bundle;
        this.ejbModuleName = abName.getNameProperty(NameFactory.EJB_MODULE);
    }

    @Override
    public Collection<PortInfo> getPortInfos() {
        return Collections.unmodifiableCollection(ejbNamePortInfoMap.values());
    }

    @Override
    public Set<String> getIds() {
        return Collections.unmodifiableSet(ejbNamePortInfoMap.keySet());
    }

    @Override
    public PortInfo getPortInfo(String ejbName) {
        return ejbNamePortInfoMap.get(ejbName);
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
        ejbModuleNameJAXWSEJBApplicationContextMap.put(ejbModuleName, this);
    }

    @Override
    public void doStop() throws Exception {
        ejbModuleNameJAXWSEJBApplicationContextMap.remove(ejbModuleName);
    }

    public static JAXWSEJBApplicationContext get(String ejbModuleName) {
        return ejbModuleNameJAXWSEJBApplicationContextMap.get(ejbModuleName);
    }
}
