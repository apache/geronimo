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

package org.apache.geronimo.j2ee.management.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.management.AppClientModule;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.management.J2EEDeployedObject;
import org.apache.geronimo.management.J2EEResource;
import org.apache.geronimo.management.geronimo.EJBManager;
import org.apache.geronimo.management.geronimo.J2EEApplication;
import org.apache.geronimo.management.geronimo.J2EEServer;
import org.apache.geronimo.management.geronimo.JMSManager;
import org.apache.geronimo.management.geronimo.JVM;
import org.apache.geronimo.management.geronimo.KeystoreManager;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.apache.geronimo.management.geronimo.SecurityRealm;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.management.geronimo.WebModule;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.threads.ThreadPool;

/**
 * @version $Rev$ $Date$
 */
public class J2EEServerImpl implements J2EEServer {
    private static final String SERVER_VENDOR = "The Apache Software Foundation";
    private final String objectName;
    private final ServerInfo serverInfo;
    private final Collection jvms;
    private final Collection resources;
    private final Collection j2eeApplications;
    private final Collection appClientModules;
    private final Collection webModules;
    private final Collection ejbModules;
    private final Collection resourceAdapterModules;
    private final Collection webManagers;
    private final Collection ejbManagers;
    private final Collection jmsManagers;
    private final Collection threadPools;
    private final Collection repositories;
    private final Collection writableRepos;
    private final Collection securityRealms;
    private final Collection keystoreManagers;
    private final ConfigurationManager configurationManager;

    public J2EEServerImpl(String objectName,
                          ServerInfo serverInfo,
                          Collection jvms,
                          Collection resources,
                          Collection applications,
                          Collection appClientModules,
                          Collection webModules,
                          Collection ejbModules,
                          Collection resourceAdapterModules,
                          Collection webManagers,
                          Collection ejbManagers,
                          Collection jmsManagers,
                          Collection threadPools,
                          Collection repositories,
                          Collection writableRepos,
                          Collection securityRealms,
                          Collection keystoreManagers,
                          ConfigurationManager configurationManager) {

        this.objectName = objectName;
        ObjectName myObjectName = ObjectNameUtil.getObjectName(this.objectName);
        verifyObjectName(myObjectName);

        this.serverInfo = serverInfo;
        this.jvms = jvms;
        this.resources = resources;

        this.j2eeApplications = applications;
        this.appClientModules = appClientModules;
        this.webModules = webModules;
        this.ejbModules = ejbModules;
        this.resourceAdapterModules = resourceAdapterModules;

        this.webManagers = webManagers;
        this.ejbManagers = ejbManagers;
        this.jmsManagers = jmsManagers;

        this.threadPools = threadPools;
        this.repositories = repositories;
        this.writableRepos = writableRepos;
        this.securityRealms = securityRealms;
        this.keystoreManagers = keystoreManagers;
        this.configurationManager = configurationManager;
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return true;
    }

    /**
     * ObjectName must match this pattern:
     * <p/>
     * domain:j2eeType=J2EEServer,name=MyName
     * @param objectName object name to verify pattern
     */
    private void verifyObjectName(ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException("ObjectName can not be a pattern", objectName);
        }
        Hashtable keyPropertyList = objectName.getKeyPropertyList();
        if (!"J2EEServer".equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException("J2EEServer object name j2eeType property must be 'J2EEServer'", objectName);
        }
        if (!keyPropertyList.containsKey("name")) {
            throw new InvalidObjectNameException("J2EEServer object must contain a name property", objectName);
        }
        if (keyPropertyList.size() != 2) {
            throw new InvalidObjectNameException("J2EEServer object name can only have j2eeType, and name", objectName);
        }
    }


    public String[] getDeployedObjects() {
        return Util.getObjectNames(getDeployedObjectInstances());
    }

    public J2EEDeployedObject[] getDeployedObjectInstances() {
        ArrayList objects = new ArrayList();
        if (j2eeApplications != null) {
            objects.addAll(j2eeApplications);
        }
        if (appClientModules  != null) {
            objects.addAll(appClientModules);
        }
        if (ejbModules  != null) {
            objects.addAll(ejbModules);
        }
        if (webModules  != null) {
            objects.addAll(webModules);
        }
        if (resourceAdapterModules != null) {
            objects.addAll(resourceAdapterModules);
        }

        return (J2EEDeployedObject[]) objects.toArray(new J2EEDeployedObject[objects.size()]);
    }

    public String[] getResources() {
        return Util.getObjectNames(getResourceInstances());
    }

    public J2EEResource[] getResourceInstances() {
        if (resources == null) return new J2EEResource[0];
        return (J2EEResource[]) resources.toArray(new J2EEResource[resources.size()]);
    }

    public String[] getJavaVMs() {
        return Util.getObjectNames(getJavaVMInstances());
    }

    public JVM[] getJavaVMInstances() {
        if (jvms == null) return new JVM[0];
        return (JVM[]) jvms.toArray(new JVM[jvms.size()]);
    }

    public J2EEApplication[] getApplications() {
        if (j2eeApplications == null) return new J2EEApplication[0];
        return (J2EEApplication[]) j2eeApplications.toArray(new J2EEApplication[j2eeApplications.size()]);
    }

    public AppClientModule[] getAppClients() {
        if (appClientModules == null) return new AppClientModule[0];
        return (AppClientModule[]) appClientModules.toArray(new AppClientModule[appClientModules.size()]);
    }

    public WebModule[] getWebModules() {
        if (webModules == null) return new WebModule[0];
        return (WebModule[]) webModules.toArray(new WebModule[webModules.size()]);
    }

    public EJBModule[] getEJBModules() {
        if (ejbModules == null) return new EJBModule[0];
        return (EJBModule[]) ejbModules.toArray(new EJBModule[ejbModules.size()]);
    }

    public ResourceAdapterModule[] getResourceAdapterModules() {
        if (resourceAdapterModules == null) return new ResourceAdapterModule[0];
        return (ResourceAdapterModule[]) resourceAdapterModules.toArray(new ResourceAdapterModule[resourceAdapterModules.size()]);
    }


    public WebManager[] getWebManagers() {
        if (webManagers == null) return new WebManager[0];
        return (WebManager[]) webManagers.toArray(new WebManager[webManagers.size()]);
    }

    public EJBManager[] getEJBManagers() {
        if (ejbManagers == null) return new EJBManager[0];
        return (EJBManager[]) ejbManagers.toArray(new EJBManager[ejbManagers.size()]);
    }

    public JMSManager[] getJMSManagers() {
        if (jmsManagers == null) return new JMSManager[0];
        return (JMSManager[]) jmsManagers.toArray(new JMSManager[jmsManagers.size()]);
    }

    public ThreadPool[] getThreadPools() {
        if (threadPools == null) return new ThreadPool[0];
        return (ThreadPool[]) threadPools.toArray(new ThreadPool[threadPools.size()]);
    }

    public ListableRepository[] getRepositories() {
        if (repositories == null) return new ListableRepository[0];
        return (ListableRepository[]) repositories.toArray(new ListableRepository[repositories.size()]);
    }

    public WritableListableRepository[] getWritableRepositories() {
        if (writableRepos == null) return new WritableListableRepository[0];
        return (WritableListableRepository[]) writableRepos.toArray(new WritableListableRepository[writableRepos.size()]);
    }

    public SecurityRealm[] getSecurityRealms() {
        if (securityRealms == null) return new SecurityRealm[0];
        return (SecurityRealm[]) securityRealms.toArray(new SecurityRealm[securityRealms.size()]);
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public KeystoreManager getKeystoreManager() {
        if (keystoreManagers == null) return null;
        return (KeystoreManager) keystoreManagers.iterator().next();
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public String getServerVendor() {
        return SERVER_VENDOR;
    }

    public String getServerVersion() {
        return serverInfo.getVersion();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(J2EEServerImpl.class, NameFactory.J2EE_SERVER);

        infoFactory.addReference("ServerInfo", ServerInfo.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoFactory.addReference("JVMs", JVM.class, NameFactory.JVM);
        infoFactory.addReference("Resources", J2EEResource.class); // several types match this
        infoFactory.addReference("Applications", J2EEApplication.class, NameFactory.J2EE_APPLICATION);
        infoFactory.addReference("AppClientModules", AppClientModule.class, NameFactory.APP_CLIENT_MODULE);
        infoFactory.addReference("WebModules", WebModule.class, NameFactory.WEB_MODULE);
        infoFactory.addReference("EJBModules", EJBModule.class, NameFactory.EJB_MODULE);
        infoFactory.addReference("ResourceAdapterModules", ResourceAdapterModule.class, NameFactory.RESOURCE_ADAPTER_MODULE);
        infoFactory.addReference("WebManagers", WebManager.class);
        infoFactory.addReference("EJBManagers", EJBManager.class);
        infoFactory.addReference("JMSManagers", JMSManager.class);
        infoFactory.addReference("ThreadPools", ThreadPool.class);
        infoFactory.addReference("Repositories", ListableRepository.class);
        infoFactory.addReference("WritableRepos", WritableListableRepository.class);
        infoFactory.addReference("SecurityRealms", SecurityRealm.class);
        infoFactory.addReference("KeystoreManagers", KeystoreManager.class);
        infoFactory.addReference("ConfigurationManager", ConfigurationManager.class);

        infoFactory.setConstructor(new String[]{
                "objectName",
                "ServerInfo",
                "JVMs",
                "Resources",
                "Applications",
                "AppClientModules",
                "WebModules",
                "EJBModules",
                "ResourceAdapterModules",
                "WebManagers",
                "EJBManagers",
                "JMSManagers",
                "ThreadPools",
                "Repositories",
                "WritableRepos",
                "SecurityRealms",
                "KeystoreManagers",
                "ConfigurationManager",
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
