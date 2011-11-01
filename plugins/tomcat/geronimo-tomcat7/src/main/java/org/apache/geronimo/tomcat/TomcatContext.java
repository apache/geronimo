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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.UserTransaction;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.tomcat.InstanceManager;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public interface TomcatContext {
    String getContextPath();

    void setContext(Context ctx);

    Context getContext();

    String getDocBase();

    SecurityHolder getSecurityHolder();

    String getVirtualServer();

    ClassLoader getClassLoader();

    UserTransaction getUserTransaction();

    javax.naming.Context getJndiContext();

    Kernel getKernel();

    Set<String> getApplicationManagedSecurityResources();

    TrackedConnectionAssociator getTrackedConnectionAssociator();

    Set<String> getUnshareableResources();

    Realm getRealm();

    Valve getClusteredValve();

    List<Valve> getValveChain();

    List<LifecycleListener> getLifecycleListenerChain();

    CatalinaCluster getCluster();

    Manager getManager();

    boolean isCrossContext();

    String getWorkDir();

    boolean isDisableCookies();

    Map<String, WebServiceContainer> getWebServices();

    InstanceManager getInstanceManager();

    Bundle getBundle();

    String getModulePath();

    String getDeploymentDescriptor();

    Collection<String> getListeners();

    WebAppInfo getWebAppInfo();

    Map<String, String> getContextAttributes();

    AbstractName getAbstractName();
}
