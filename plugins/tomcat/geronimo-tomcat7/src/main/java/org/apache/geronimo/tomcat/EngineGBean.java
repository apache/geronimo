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

import java.util.Map;

import org.apache.catalina.Cluster;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Manager;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardEngine;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.system.jmx.MBeanServerReference;
import org.apache.geronimo.tomcat.cluster.CatalinaClusterGBean;
import org.apache.geronimo.tomcat.security.jacc.JACCRealm;
import org.apache.tomcat.util.modeler.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class EngineGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {

    private static final Logger log = LoggerFactory.getLogger(EngineGBean.class);

    private static final String NAME = "name";
    private static final String DEFAULTHOST = "defaultHost";

    private final Engine engine;

    public EngineGBean(
            //fish engine out of server configured with server.xml
            @ParamReference(name = "Server") TomcatServerGBean server,
            @ParamAttribute(name = "serviceName") String serviceName,

            //Or (deprecated) set up an engine directly
            @ParamAttribute(name = "className") String className,
            @ParamAttribute(name = "initParams") Map initParams,
            @ParamReference(name = "DefaultHost", namingType = HostGBean.J2EE_TYPE) HostGBean defaultHost,
            @ParamReference(name = "RealmGBean", namingType = GBeanInfoBuilder.DEFAULT_J2EE_TYPE) ObjectRetriever realmGBean,
            @ParamReference(name = "ConfigurationFactory", namingType = GBeanInfoBuilder.DEFAULT_J2EE_TYPE) ConfigurationFactory configurationFactory,
            @ParamReference(name = "TomcatValveChain", namingType = ValveGBean.J2EE_TYPE) ValveGBean tomcatValveChain,
            @ParamReference(name = "LifecycleListenerChain", namingType = LifecycleListenerGBean.J2EE_TYPE) LifecycleListenerGBean listenerChain,
            @ParamReference(name = "CatalinaCluster", namingType = CatalinaClusterGBean.J2EE_TYPE) CatalinaClusterGBean clusterGBean,
            @ParamReference(name = "Manager", namingType = ManagerGBean.J2EE_TYPE) ManagerGBean manager,
            @ParamReference(name = "MBeanServerReference") MBeanServerReference mbeanServerReference) throws Exception {

        if (server == null) {
            //legacy configuration

            if (className == null) {
                className = "org.apache.geronimo.tomcat.TomcatEngine";
            }

            if (initParams == null) {
                throw new IllegalArgumentException("Must have 'name' value in initParams.");
            }

            //Be sure the defaulthost has been declared.
            if (defaultHost == null) {
                throw new IllegalArgumentException("Must have a 'defaultHost' attribute.");
            }

            //Be sure the name has been declared.
            if (!initParams.containsKey(NAME)) {
                throw new IllegalArgumentException("Must have a 'name' value initParams.");
            }

            //Deprecate the defaultHost initParam
            if (initParams.containsKey(DEFAULTHOST)) {
                log.warn("The " + DEFAULTHOST + " initParams value is no longer used and will be ignored.");
                initParams.remove(DEFAULTHOST);
            }

            engine = (Engine) Class.forName(className).newInstance();

            //Set the parameters
            setParameters(engine, initParams);

            //Set realm (must be before Hosts)
            engine.setRealm(JACCRealm.INSTANCE);

            //Set the default Host
            Host host = (Host) defaultHost.getInternalObject();
            if (host.getParent() != null) {
                throw new IllegalStateException("Default host is already in use by another engine: " + host.getParent());
            }
            engine.setDefaultHost(host.getName());
            addHost(host);

            if (manager != null)
                engine.setManager((Manager) manager.getInternalObject());

            //Add the valve and listener lists
            if (engine instanceof StandardEngine) {
                if (tomcatValveChain != null) {
                    ValveGBean valveGBean = tomcatValveChain;
                    while (valveGBean != null) {
                        ((StandardEngine) engine).addValve((Valve) valveGBean.getInternalObject());
                        valveGBean = valveGBean.getNextValve();
                    }
                }

                if (listenerChain != null) {
                    LifecycleListenerGBean listenerGBean = listenerChain;
                    while (listenerGBean != null) {
                        ((StandardEngine) engine).addLifecycleListener((LifecycleListener) listenerGBean.getInternalObject());
                        listenerGBean = listenerGBean.getNextListener();
                    }
                }
            }

            if (mbeanServerReference != null) {
                Registry.getRegistry(null, null).setMBeanServer(mbeanServerReference.getMBeanServer());
            }

            //Add clustering
            if (clusterGBean != null) {
                engine.setCluster((Cluster) clusterGBean.getInternalObject());
            }
        } else {
            //get engine from server gbean
            engine = (Engine) server.getService(serviceName).getContainer();
        }

    }

    public void removeHost(Host host) {
        engine.removeChild(host);
    }

    public void addHost(Host host) {
        engine.addChild(host);
    }

    public Object getInternalObject() {
        return engine;
    }

    public void doFail() {
        log.warn("Failed");
    }

    public void doStart() throws Exception {
        log.debug("Started");
    }

    public void doStop() throws Exception {
        log.debug("Stopped");
    }

}
