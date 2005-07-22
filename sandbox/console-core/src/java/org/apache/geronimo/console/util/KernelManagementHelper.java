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
package org.apache.geronimo.console.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import org.apache.geronimo.j2ee.management.geronimo.JVM;
import org.apache.geronimo.j2ee.management.geronimo.J2EEApplication;
import org.apache.geronimo.j2ee.management.J2EEDomain;
import org.apache.geronimo.j2ee.management.J2EEServer;
import org.apache.geronimo.j2ee.management.J2EEDeployedObject;
import org.apache.geronimo.j2ee.management.AppClientModule;
import org.apache.geronimo.j2ee.management.WebModule;
import org.apache.geronimo.j2ee.management.EJBModule;
import org.apache.geronimo.j2ee.management.ResourceAdapterModule;
import org.apache.geronimo.j2ee.management.J2EEResource;
import org.apache.geronimo.j2ee.management.JCAResource;
import org.apache.geronimo.j2ee.management.JDBCResource;
import org.apache.geronimo.j2ee.management.JMSResource;
import org.apache.geronimo.j2ee.management.J2EEModule;
import org.apache.geronimo.j2ee.management.EJB;
import org.apache.geronimo.j2ee.management.Servlet;
import org.apache.geronimo.j2ee.management.ResourceAdapter;
import org.apache.geronimo.j2ee.management.JDBCDataSource;
import org.apache.geronimo.j2ee.management.JDBCDriver;
import org.apache.geronimo.j2ee.management.JCAConnectionFactory;
import org.apache.geronimo.j2ee.management.JCAManagedConnectionFactory;
import org.apache.geronimo.j2ee.management.impl.Util;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An implementation of the ManagementHelper interface that uses a Geronimo
 * kernel. That may be an in-VM kernel or a remote kernel, we don't really
 * care.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class KernelManagementHelper implements ManagementHelper {
    private final static Log log = LogFactory.getLog(KernelManagementHelper.class);
    private Kernel kernel;
    private ProxyManager pm;

    public KernelManagementHelper(Kernel kernel) {
        this.kernel = kernel;
        pm = kernel.getProxyManager();
    }

    public J2EEDomain[] getDomains() {
        String[] names = Util.getObjectNames(kernel, "*:", new String[]{"J2EEDomain"});
        J2EEDomain[] domains = new J2EEDomain[names.length];
        for (int i = 0; i < domains.length; i++) {
            try {
                domains[i] = (J2EEDomain)kernel.getProxyManager().createProxy(ObjectName.getInstance(names[i]), J2EEDomain.class);
            } catch (MalformedObjectNameException e) {
                log.error(e);
            }
        }
        return domains;
    }

    public J2EEServer[] getServers(J2EEDomain domain) {
        J2EEServer[] servers = new J2EEServer[0];
        try {
            String[] names = domain.getServers();
            Object[] temp = pm.createProxies(names);
            servers = new J2EEServer[temp.length];
            System.arraycopy(temp, 0, servers, 0, temp.length);
        } catch (Exception e) {
            log.error(e);
        }
        return servers;
    }

    public J2EEDeployedObject[] getDeployedObjects(J2EEServer server) {
        J2EEDeployedObject[] result = new J2EEDeployedObject[0];
        try {
            String[] names = server.getDeployedObjects();
            Object[] temp = pm.createProxies(names);
            result = new J2EEDeployedObject[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error(e);
        }
        return result;
    }

    public J2EEApplication[] getApplications(J2EEServer server) {
        List list = new ArrayList();
        try {
            String[] names = server.getDeployedObjects();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.J2EE_APPLICATION)) {
                    list.add(pm.createProxy(name));
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return (J2EEApplication[]) list.toArray(new J2EEApplication[list.size()]);
    }

    public AppClientModule[] getAppClients(J2EEServer server) {
        List list = new ArrayList();
        try {
            String[] names = server.getDeployedObjects();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.APP_CLIENT_MODULE)) {
                    list.add(pm.createProxy(name));
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return (AppClientModule[]) list.toArray(new AppClientModule[list.size()]);
    }

    public WebModule[] getWebModules(J2EEServer server) {
        List list = new ArrayList();
        try {
            String[] names = server.getDeployedObjects();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.WEB_MODULE)) {
                    list.add(pm.createProxy(name));
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return (WebModule[]) list.toArray(new WebModule[list.size()]);
    }

    public EJBModule[] getEJBModules(J2EEServer server) {
        List list = new ArrayList();
        try {
            String[] names = server.getDeployedObjects();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.EJB_MODULE)) {
                    list.add(pm.createProxy(name));
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return (EJBModule[]) list.toArray(new EJBModule[list.size()]);
    }

    public ResourceAdapterModule[] getRAModules(J2EEServer server) {
        List list = new ArrayList();
        try {
            String[] names = server.getDeployedObjects();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.RESOURCE_ADAPTER_MODULE)) {
                    list.add(pm.createProxy(name));
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return (ResourceAdapterModule[]) list.toArray(new ResourceAdapterModule[list.size()]);
    }

    public J2EEResource[] getResources(J2EEServer server) {
        J2EEResource[] result = new J2EEResource[0];
        try {
            String[] names = server.getResources();
            Object[] temp = pm.createProxies(names);
            result = new J2EEResource[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error(e);
        }
        return result;
    }

    public JCAResource[] getJCAResources(J2EEServer server) {
        List list = new ArrayList();
        try {
            //todo: filter based on ObjectName or something, but what counts as a "JCAResource"?
            J2EEResource[] all = getResources(server);
            for (int i = 0; i < all.length; i++) {
                if(all[i] instanceof JCAResource) {
                    list.add(all[i]);
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return (JCAResource[]) list.toArray(new JCAResource[list.size()]);
    }

    public JDBCResource[] getJDBCResources(J2EEServer server) {
        return new JDBCResource[0]; // Geronimo uses JCA resources for this
    }

    public JMSResource[] getJMSResources(J2EEServer server) {
        return new JMSResource[0];  // Geronimo uses JCA resources for this
    }

    public JVM[] getJavaVMs(J2EEServer server) {
        JVM[] result = new JVM[0];
        try {
            String[] names = server.getJavaVMs();
            Object[] temp = pm.createProxies(names);
            result = new JVM[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error(e);
        }
        return result;
    }

    // application properties
    public J2EEModule[] getModules(J2EEApplication application) {
        J2EEModule[] result = new J2EEModule[0];
        try {
            String[] names = application.getModules();
            Object[] temp = pm.createProxies(names);
            result = new J2EEModule[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error(e);
        }
        return result;
    }

    public AppClientModule[] getAppClients(J2EEApplication application) {
        List list = new ArrayList();
        try {
            String[] names = application.getModules();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.APP_CLIENT_MODULE)) {
                    list.add(pm.createProxy(name));
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return (AppClientModule[]) list.toArray(new AppClientModule[list.size()]);
    }

    public WebModule[] getWebModules(J2EEApplication application) {
        List list = new ArrayList();
        try {
            String[] names = application.getModules();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.WEB_MODULE)) {
                    list.add(pm.createProxy(name));
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return (WebModule[]) list.toArray(new WebModule[list.size()]);
    }

    public EJBModule[] getEJBModules(J2EEApplication application) {
        List list = new ArrayList();
        try {
            String[] names = application.getModules();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.EJB_MODULE)) {
                    list.add(pm.createProxy(name));
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return (EJBModule[]) list.toArray(new EJBModule[list.size()]);
    }

    public ResourceAdapterModule[] getRAModules(J2EEApplication application) {
        List list = new ArrayList();
        try {
            String[] names = application.getModules();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.RESOURCE_ADAPTER_MODULE)) {
                    list.add(pm.createProxy(name));
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return (ResourceAdapterModule[]) list.toArray(new ResourceAdapterModule[list.size()]);
    }


    public J2EEResource[] getResources(J2EEApplication application) {
        J2EEResource[] result = new J2EEResource[0];
        try {
            String[] names = application.getResources();
            Object[] temp = pm.createProxies(names);
            result = new J2EEResource[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error(e);
        }
        return result;
    }

    public JCAResource[] getJCAResources(J2EEApplication application) {
        List list = new ArrayList();
        try {
            //todo: filter based on ObjectName or something, but what counts as a "JCAResource"?
            J2EEResource[] all = getResources(application);
            for (int i = 0; i < all.length; i++) {
                if(all[i] instanceof JCAResource) {
                    list.add(all[i]);
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return (JCAResource[]) list.toArray(new JCAResource[list.size()]);
    }

    public JDBCResource[] getJDBCResources(J2EEApplication application) {
        return new JDBCResource[0];  // Geronimo uses JCAResources for this
    }

    public JMSResource[] getJMSResources(J2EEApplication application) {
        return new JMSResource[0];  // Geronimo uses JCAResources for this
    }

    // module properties
    public EJB[] getEJBs(EJBModule module) {
        return new EJB[0];  //todo
    }

    public Servlet[] getServlets(WebModule module) {
        return new Servlet[0];  //todo
    }

    public ResourceAdapter getResourceAdapters(ResourceAdapterModule module) {
        return null;  //todo
    }

    // resource adapter properties
    public JCAResource[] getRAResources(ResourceAdapter adapter) {
        return new JCAResource[0];  //todo
    }

    // resource properties
    public JDBCDataSource[] getDataSource(JDBCResource resource) {
        return new JDBCDataSource[0];  //todo
    }

    public JDBCDriver[] getDriver(JDBCDataSource dataSource) {
        return new JDBCDriver[0];  //todo
    }

    public JCAConnectionFactory[] getConnectionFactories(JCAResource resource) {
        return new JCAConnectionFactory[0];  //todo
    }

    public JCAManagedConnectionFactory getManagedConnectionFactory(JCAConnectionFactory factory) {
        return null;  //todo
    }

    /**
     * Helper method to connect to a remote kernel.
     */
    public static KernelManagementHelper getRemoteKernelManager(String host, String user, String password) throws java.io.IOException {
        String uri = "jmx:rmi://"+host+"/jndi/rmi:/JMXConnector";
        java.util.Map environment = new java.util.HashMap();
        String[] credentials = new String[]{user, password};
        environment.put(javax.management.remote.JMXConnector.CREDENTIALS, credentials);
        javax.management.remote.JMXServiceURL address = new javax.management.remote.JMXServiceURL("service:" + uri);
        javax.management.remote.JMXConnector jmxConnector = javax.management.remote.JMXConnectorFactory.connect(address, environment);
        javax.management.MBeanServerConnection mbServerConnection = jmxConnector.getMBeanServerConnection();
        Kernel kernel = new org.apache.geronimo.kernel.jmx.KernelDelegate(mbServerConnection);
        return new KernelManagementHelper(kernel);
    }

    /**
     * For test purposes; start the server, deploy an app or two, and run this.
     * Should be changed to a JUnit test with the Maven plugin to start and
     * stop the server.
     */
    public static void main(String[] args) {
        try {
            ManagementHelper mgr = getRemoteKernelManager("localhost", "system", "manager");
            J2EEDomain domain = mgr.getDomains()[0];
            System.out.println("Found domain "+domain.getObjectName()+" with "+domain.getServers().length+" servers");
            J2EEServer server = mgr.getServers(domain)[0];
            System.out.println("Found server "+server.getObjectName()+" with "+server.getDeployedObjects().length+" deployments");
            System.out.println("  "+mgr.getApplications(server).length+" applications");
            System.out.println("  "+mgr.getAppClients(server).length+" app clients");
            System.out.println("  "+mgr.getEJBModules(server).length+" EJB JARs");
            System.out.println("  "+mgr.getWebModules(server).length+" web apps");
            System.out.println("  "+mgr.getRAModules(server).length+" RA modules");
            J2EEDeployedObject[] deployments = mgr.getDeployedObjects(server);
            for (int i = 0; i < deployments.length; i++) {
                J2EEDeployedObject deployment = deployments[i];
                System.out.println("Deployment "+i+": "+deployment.getObjectName());
            }
            J2EEApplication[] applications = mgr.getApplications(server);
            for (int i = 0; i < applications.length; i++) {
                J2EEApplication app = applications[i];
                System.out.println("Application "+i+": "+app.getObjectName());
                J2EEModule[] modules = mgr.getModules(app);
                for (int j = 0; j < modules.length; j++) {
                    J2EEModule deployment = modules[j];
                    System.out.println("  Module "+j+": "+deployment.getObjectName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
