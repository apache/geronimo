/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import java.lang.reflect.Array;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import javax.management.ObjectName;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.management.J2EEDeployedObject;
import org.apache.geronimo.management.J2EEResource;
import org.apache.geronimo.management.geronimo.EJBManager;
import org.apache.geronimo.management.geronimo.J2EEServer;
import org.apache.geronimo.management.geronimo.JMSManager;
import org.apache.geronimo.management.geronimo.JVM;
import org.apache.geronimo.management.geronimo.LoginService;
import org.apache.geronimo.management.geronimo.SecurityRealm;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.management.geronimo.KeystoreManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.configuration.ConfigurationInstaller;
import org.apache.geronimo.system.threads.ThreadPool;

/**
 * @version $Rev$ $Date$
 */
public class J2EEServerImpl implements J2EEServer {
    private static final String SERVER_VENDOR = "The Apache Software Foundation";
    private final Kernel kernel;
    private final String baseName;
    private final ServerInfo serverInfo;
    private final String objectName;

    public J2EEServerImpl(Kernel kernel, String objectName, ServerInfo serverInfo) {
        this.objectName = objectName;
        ObjectName myObjectName = JMXUtil.getObjectName(this.objectName);
        verifyObjectName(myObjectName);

        // build the base name used to query the server for child modules
        Hashtable keyPropertyList = myObjectName.getKeyPropertyList();
        String name = (String) keyPropertyList.get("name");
        baseName = myObjectName.getDomain() + ":J2EEServer=" + name + ",";

        this.kernel = kernel;
        this.serverInfo = serverInfo;
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
        return Util.getObjectNames(kernel,
                baseName,
                new String[]{"J2EEApplication", "AppClientModule", "EJBModule", "WebModule", "ResourceAdapterModule"});
    }

    public J2EEDeployedObject[] getDeployedObjectInstances() {
        return (J2EEDeployedObject[]) Util.getObjects(kernel, baseName,
                new String[]{"J2EEApplication", "AppClientModule", "EJBModule", "WebModule", "ResourceAdapterModule"}, J2EEDeployedObject.class);
    }

    public String[] getResources() {
        return Util.getObjectNames(kernel,
                baseName,
                new String[]{"JCAResource", "JavaMailResource", "JDBCResource", "JMSResource", "JNDIResource", "JTAResource", "RMI_IIOPResource", "URLResource"});
    }

    public J2EEResource[] getResourceInstances() {
        return (J2EEResource[]) Util.getObjects(kernel, baseName,
                new String[]{"JCAResource", "JavaMailResource", "JDBCResource", "JMSResource", "JNDIResource", "JTAResource", "RMI_IIOPResource", "URLResource"}, J2EEResource.class);
    }

    public String[] getJavaVMs() {
        return Util.getObjectNames(kernel, baseName, new String[]{"JVM"});
    }

    public JVM[] getJavaVMInstances() {
        return (JVM[]) Util.getObjects(kernel, baseName, new String[]{"JVM"}, JVM.class);
    }

    public WebManager[] getWebManagers() {
        return (WebManager[]) getObjects(WebManager.class, false);
    }

    public EJBManager[] getEJBManagers() {
        return (EJBManager[]) getObjects(EJBManager.class, false);
    }

    public JMSManager[] getJMSManagers() {
        return (JMSManager[]) getObjects(JMSManager.class, false);
    }

    public ThreadPool[] getThreadPools() {
        return (ThreadPool[]) getObjects(ThreadPool.class, true);
    }

    public Repository[] getRepositories() {
        return (Repository[]) getObjects(Repository.class, true);
    }

    public SecurityRealm[] getSecurityRealms() {
        return (SecurityRealm[]) getObjects(SecurityRealm.class, true);
    }

    public ServerInfo getServerInfo() {
        return (ServerInfo) getObject(ServerInfo.class);
    }

    public LoginService getLoginService() {
        return (LoginService) getObject(LoginService.class);
    }

    public KeystoreManager getKeystoreManager() {
        return (KeystoreManager) getObject(KeystoreManager.class);
    }

    public ConfigurationInstaller getConfigurationInstaller() {
        return (ConfigurationInstaller) getObject(ConfigurationInstaller.class);
    }

    public ConfigurationManager getConfigurationManager() {
        return ConfigurationUtil.getConfigurationManager(kernel);
    }

    public String getServerVendor() {
        return SERVER_VENDOR;
    }

    public String getServerVersion() {
        return serverInfo.getVersion();
    }

    private Object getObject(Class type) {
        Set names = kernel.listGBeans(new AbstractNameQuery(type.getName()));
        if (names.isEmpty()) {
            return null;
        }
        AbstractName name = (AbstractName) names.iterator().next();
        return kernel.getProxyManager().createProxy(name, type);
    }

    private Object[] getObjects(Class type, boolean returnEmpty) {
        Set names = kernel.listGBeans(new AbstractNameQuery(type.getName()));

        if(names.size() == 0) {
            if (returnEmpty) {
                return (Object[]) Array.newInstance(type, 0);
            } else {
                return null;
            }
        }

        Object[] results = (Object[]) Array.newInstance(type, names.size());
        ProxyManager mgr = kernel.getProxyManager();
        int i=0;
        for (Iterator it = names.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            results[i++] = mgr.createProxy(name, type.getClassLoader());
        }
        return results;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(J2EEServerImpl.class, NameFactory.J2EE_SERVER);

        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("deployedObjects", String[].class, false);
        infoFactory.addAttribute("resources", String[].class, false);
        infoFactory.addAttribute("javaVMs", String[].class, false);
        infoFactory.addAttribute("serverVendor", String.class, false);
        infoFactory.addAttribute("serverVersion", String.class, false);
        infoFactory.addInterface(J2EEServer.class);

        infoFactory.addReference("ServerInfo", ServerInfo.class, NameFactory.GERONIMO_SERVICE);

        infoFactory.setConstructor(new String[]{"kernel", "objectName", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
