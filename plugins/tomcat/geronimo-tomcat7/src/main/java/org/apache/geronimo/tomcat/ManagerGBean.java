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
package org.apache.geronimo.tomcat;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.catalina.Manager;
import org.apache.catalina.Store;
import org.apache.catalina.session.PersistentManagerBase;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.tomcat.util.IntrospectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagerGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever{

    private static final Logger log = LoggerFactory.getLogger(ManagerGBean.class);

    public static final String J2EE_TYPE = "Manager";

    protected final Manager manager;

    // no-arg constructor required for gbean refs
    public ManagerGBean(){
        manager = null;
    }

    protected ManagerGBean(String className) throws Exception{
       super();
       manager = (Manager)Class.forName(className).newInstance();
    }

    public ManagerGBean(String className,
            Map initParams) throws Exception {
        super(); // TODO: make it an attribute
        //Validate
        if (className == null){
            className = "org.apache.catalina.session.StandardManager";
        }

        //Create the Manager object
        manager = (Manager)Class.forName(className).newInstance();

        //Set the parameters
        if (CLASSNAME_PARAMETERHANDLER_MAP.containsKey(className)) {
            CLASSNAME_PARAMETERHANDLER_MAP.get(className).handle(manager, initParams);
        } else {
            CLASSNAME_PARAMETERHANDLER_MAP.get(DEFAULT_PARAMETER_HANDLER).handle(manager, initParams);
        }
    }

    public void doStart() throws Exception {
    }

    public void doStop() throws Exception {
    }

    public void doFail() {
    }

    public Object getInternalObject() {
        return manager;
    }

    public static final GBeanInfo GBEAN_INFO;

    public static final Map<String, ParametersHandler> CLASSNAME_PARAMETERHANDLER_MAP = new HashMap<String, ParametersHandler>();

    public static final String DEFAULT_PARAMETER_HANDLER = "default";

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("TomcatManager", ManagerGBean.class, J2EE_TYPE);
        infoFactory.addAttribute("className", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addOperation("getInternalObject");
        infoFactory.setConstructor(new String[] {
                "className",
                "initParams"});
        GBEAN_INFO = infoFactory.getBeanInfo();
        //Initialize handler map
        CLASSNAME_PARAMETERHANDLER_MAP.put(DEFAULT_PARAMETER_HANDLER, new DefaultParametersHandler());
        CLASSNAME_PARAMETERHANDLER_MAP.put("org.apache.catalina.session.PersistentManager", new PersistentManagerBaseParametersHandler());
        CLASSNAME_PARAMETERHANDLER_MAP.put("org.apache.catalina.session.DistributedManager", new PersistentManagerBaseParametersHandler());
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public interface ParametersHandler {
        public void handle(Object managerObject, Map<String, String> nameValueMap) throws Exception;
    }

    static class DefaultParametersHandler implements ParametersHandler {
        public void handle(Object managerObject, Map<String, String> nameValueMap) throws Exception {
            for (Entry<String,String> entry : nameValueMap.entrySet()) {
                String currentParameterName = entry.getKey();
                String currentParameterValue = entry.getValue();
                if (currentParameterValue != null) {
                    currentParameterValue = currentParameterValue.trim();
                }
                IntrospectionUtils.setProperty(managerObject, currentParameterName, currentParameterValue);
            }
        }
    }

    static class PersistentManagerBaseParametersHandler implements ParametersHandler {
        private static final String STORE_CLASSNAME = "store.className";

        private static final String STORE_PARAMETER_PREFIX = "store.";

        public void handle(Object managerObject, Map<String, String> nameValueMap) throws Exception {
            //Search the Store implementation
            if (!nameValueMap.containsKey(STORE_CLASSNAME) || nameValueMap.get(STORE_CLASSNAME) == null) {
                throw new IllegalArgumentException("store.className should be set to indicate which implementation is used");
            }
            Store store = (Store) Class.forName(nameValueMap.get(STORE_CLASSNAME).trim()).newInstance();
            nameValueMap.remove(STORE_CLASSNAME);
            //Initialize store object
            for(Entry<String,String> entry : nameValueMap.entrySet()){
                String sCurrentParameterName = entry.getKey();
                String sCurrentParameterValue = entry.getValue();
                if (sCurrentParameterValue != null) {
                    sCurrentParameterValue = sCurrentParameterValue.trim();
                }
                if (sCurrentParameterName.indexOf(STORE_PARAMETER_PREFIX) == 0) {
                    int iDotIndex = sCurrentParameterName.indexOf('.');
                    String sStoreParameterName = sCurrentParameterName.substring(iDotIndex + 1);
                    if (IntrospectionUtils.setProperty(store, sStoreParameterName, sCurrentParameterValue)) {
                        log.debug("Property [" + sStoreParameterName + "] of the store object is set with [" + sCurrentParameterValue + "]");
                    } else {
                        log.warn("Fail to set the property [" + sStoreParameterName + "] of the store object with [" + sCurrentParameterValue + "]");
                    }
                } else {
                    if (IntrospectionUtils.setProperty(managerObject, sCurrentParameterName, sCurrentParameterValue)) {
                        log.debug("Property [" + sCurrentParameterName + "] of the manager object is set with [" + sCurrentParameterValue + "]");
                    } else {
                        log.warn("Fail to set the property [" + sCurrentParameterName + "] of the manager object with [" + sCurrentParameterValue + "]");
                    }
                }
            }
            //Set Store to Manager
            ((PersistentManagerBase) managerObject).setStore(store);
        }
    }
}
