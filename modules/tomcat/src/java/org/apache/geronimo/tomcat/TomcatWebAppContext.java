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

package org.apache.geronimo.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.cluster.CatalinaCluster;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.management.geronimo.WebModule;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebConnector;
import org.apache.geronimo.security.jacc.RoleDesignateSource;
import org.apache.geronimo.tomcat.cluster.CatalinaClusterGBean;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.OnlineUserTransaction;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.naming.resources.DirContextURLStreamHandler;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.naming.directory.DirContext;

import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/**
 * Wrapper for a WebApplicationContext that sets up its J2EE environment.
 *
 * @version $Rev$ $Date$
 */
public class TomcatWebAppContext implements GBeanLifecycle, TomcatContext, WebModule {

    private static Log log = LogFactory.getLog(TomcatWebAppContext.class);

    protected final TomcatContainer container;

    private final ClassLoader classLoader;

    protected Context context = null;

    private String path = null;

    private String docBase = null;

    private String virtualServer = null;

    private final Realm realm;

    private final List valveChain;

    private final CatalinaCluster catalinaCluster;

    private final Manager manager;

    private final boolean crossContext;

    private final boolean disableCookies;

    private final Map componentContext;

    private final Kernel kernel;

    private final Set unshareableResources;

    private final Set applicationManagedSecurityResources;

    private final TrackedConnectionAssociator trackedConnectionAssociator;

    private final TransactionContextManager transactionContextManager;

    private final RoleDesignateSource roleDesignateSource;

    private final SecurityHolder securityHolder;

    private final J2EEServer server;

    private final J2EEApplication application;

    private final Map webServices;

    private final String objectName;

    private final String originalSpecDD;

    private final URL configurationBaseURL;

    public TomcatWebAppContext(
            ClassLoader classLoader,
            String objectName,
            String originalSpecDD,
            URL configurationBaseUrl,
            SecurityHolder securityHolder,
            String virtualServer,
            Map componentContext,
            Set unshareableResources,
            Set applicationManagedSecurityResources,
            OnlineUserTransaction userTransaction,
            TransactionContextManager transactionContextManager,
            TrackedConnectionAssociator trackedConnectionAssociator,
            TomcatContainer container,
            RoleDesignateSource roleDesignateSource,
            ObjectRetriever tomcatRealm,
            ValveGBean tomcatValveChain,
            CatalinaClusterGBean cluster,
            ManagerGBean manager,
            boolean crossContext,
            boolean disableCookies,
            Map webServices,
            J2EEServer server,
            J2EEApplication application,
            Kernel kernel)
            throws Exception {

        assert classLoader != null;
        assert configurationBaseUrl != null;
        assert transactionContextManager != null;
        assert trackedConnectionAssociator != null;
        assert componentContext != null;
        assert container != null;


        this.objectName = objectName;
        URI root;
//        TODO is there a simpler way to do this?
        if (configurationBaseUrl.getProtocol().equalsIgnoreCase("file")) {
            root = new URI("file", configurationBaseUrl.getPath(), null);
        } else {
            root = URI.create(configurationBaseUrl.toString());
        }
        this.setDocBase(root.getPath());
        this.container = container;
        this.originalSpecDD = originalSpecDD;

        this.virtualServer = virtualServer;
        this.securityHolder = securityHolder;

        this.componentContext = componentContext;
        this.transactionContextManager = transactionContextManager;
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
        this.trackedConnectionAssociator = trackedConnectionAssociator;

        this.roleDesignateSource = roleDesignateSource;
        this.server = server;
        this.application = application;

        this.configurationBaseURL = configurationBaseUrl;

        if (tomcatRealm != null){
            realm = (Realm)tomcatRealm.getInternalObject();
            if (realm == null){
                throw new IllegalArgumentException("tomcatRealm must be an instance of org.apache.catalina.Realm.");
            }
        } else{
            realm = null;
        }

        //Add the valve list
        if (tomcatValveChain != null){
            ArrayList chain = new ArrayList();
            ValveGBean valveGBean = tomcatValveChain;
            while(valveGBean != null){
                chain.add((Valve)valveGBean.getInternalObject());
                valveGBean = valveGBean.getNextValve();
            }
            valveChain = chain;
        } else {
            valveChain = null;
        }

        //Add the cluster
        if (cluster != null)
           catalinaCluster = (CatalinaCluster)cluster.getInternalObject();
        else
            catalinaCluster = null;

        //Add the manager
        if (manager != null)
           this.manager = (Manager)manager.getInternalObject();
        else
            this.manager = null;

        this.crossContext = crossContext;

        this.disableCookies = disableCookies;

        this.webServices = webServices;

        this.classLoader = classLoader;

        this.kernel = kernel;
        if (objectName != null) {
            ObjectName myObjectName = ObjectNameUtil.getObjectName(objectName);
            verifyObjectName(myObjectName);
        }

        if (securityHolder != null){
            if (roleDesignateSource == null) {
                throw new IllegalArgumentException("RoleDesignateSource must be supplied for a secure web app");
            }
        }
        userTransaction.setUp(transactionContextManager,
                trackedConnectionAssociator);

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

    public URL getWARDirectory() {
        return configurationBaseURL;
    }

    public String getWARName() {
        //todo: make this return something more consistent
        try {
            return ObjectName.getInstance(objectName).getKeyProperty(NameFactory.J2EE_NAME);
        } catch (MalformedObjectNameException e) {
            return null;
        }
    }

    public WebContainer getContainer() {
        return container;
    }

    public String getServer() {
        return server == null? null: server.getObjectName();
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    public Map getComponentContext() {
        return componentContext;
    }

    public String getVirtualServer() {
        return virtualServer;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Kernel getKernel() {
        return kernel;
    }

    public boolean isDisableCookies() {
        return disableCookies;
    }

    public TransactionContextManager getTransactionContextManager() {
        return transactionContextManager;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public URL getURLFor() {
        WebConnector[] connectors = (WebConnector[]) container.getConnectors();
        Map map = new HashMap();
        for (int i = 0; i < connectors.length; i++) {
            WebConnector connector = connectors[i];
            map.put(connector.getProtocol(), connector.getConnectUrl());
        }
        String urlPrefix;
        if((urlPrefix = (String) map.get("HTTP")) == null) {
            if((urlPrefix = (String) map.get("HTTPS")) == null) {
                urlPrefix = (String) map.get("AJP");
            }
        }
        if(urlPrefix == null) {
            return null;
        }
        try {
            return new URL(urlPrefix + getContextPath());
        } catch (MalformedURLException e) {
            log.error("Bad URL to connect to web app", e);
            return null;
        }
    }

    public String getContextPath() {
        return path;
    }

    public void setContextPath(String path) {
        this.path = path.startsWith("/") ? path : "/" + path;
    }

    public SecurityHolder getSecurityHolder() {
        return securityHolder;
    }


    public Set getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }

    public TrackedConnectionAssociator getTrackedConnectionAssociator() {
        return trackedConnectionAssociator;
    }

    public Set getUnshareableResources() {
        return unshareableResources;
    }

    public Realm getRealm() {
        return realm;
    }

    public List getValveChain() {
        return valveChain;
    }

    public CatalinaCluster getCluster() {
        return catalinaCluster;
    }

    public Manager getManager() {
        return manager;
    }

    public boolean isCrossContext() {
        return crossContext;
    }

    public Map getWebServices(){
        return webServices;
    }

    public String[] getServlets(){
        String[] result = null;
        if ((context != null) && (context instanceof StandardContext))
            result = ((StandardContext)context).getServlets();

        return result;
    }

    /**
     * ObjectName must match this pattern: <p/>
     * domain:j2eeType=WebModule,name=MyName,J2EEServer=MyServer,J2EEApplication=MyApplication
     */
    private void verifyObjectName(ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException(
                    "ObjectName can not be a pattern", objectName);
        }
        Hashtable keyPropertyList = objectName.getKeyPropertyList();
        if (!NameFactory.WEB_MODULE.equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException(
                    "WebModule object name j2eeType property must be 'WebModule'",
                    objectName);
        }
        if (!keyPropertyList.containsKey(NameFactory.J2EE_NAME)) {
            throw new InvalidObjectNameException(
                    "WebModule object must contain a name property", objectName);
        }
        if (!keyPropertyList.containsKey(NameFactory.J2EE_SERVER)) {
            throw new InvalidObjectNameException(
                    "WebModule object name must contain a J2EEServer property",
                    objectName);
        }
        if (!keyPropertyList.containsKey(NameFactory.J2EE_APPLICATION)) {
            throw new InvalidObjectNameException(
                    "WebModule object name must contain a J2EEApplication property",
                    objectName);
        }
        if (keyPropertyList.size() != 4) {
            throw new InvalidObjectNameException(
                    "WebModule object name can only have j2eeType, name, J2EEApplication, and J2EEServer properties",
                    objectName);
        }
    }

    public String[] getJavaVMs() {
        return server == null? new String[0]: server.getJavaVMs();
    }

    public String getDeploymentDescriptor() {
        return originalSpecDD;
    }

    public void doStart() throws Exception {

        // See the note of TomcatContainer::addContext
        container.addContext(this);
        // Is it necessary - doesn't Tomcat Embedded take care of it?
        // super.start();
        //register the classloader <> dir context association so that tomcat's jndi based getResources works.
        DirContext resources = context.getResources();
        DirContextURLStreamHandler.bind((ClassLoader) classLoader, resources);

        log.debug("TomcatWebAppContext started for " + path);
    }

    public void doStop() throws Exception {
        container.removeContext(this);
        DirContextURLStreamHandler.unbind((ClassLoader) classLoader);
 
        // No more logging will occur for this ClassLoader. Inform the LogFactory to avoid a memory leak.
//        LogFactory.release(classLoader);

        log.debug("TomcatWebAppContext stopped");
    }

    public void doFail() {
        container.removeContext(this);

        // No more logging will occur for this ClassLoader. Inform the LogFactory to avoid a memory leak.
//        LogFactory.release(classLoader);

        log.warn("TomcatWebAppContext failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("Tomcat WebApplication Context", TomcatWebAppContext.class, NameFactory.WEB_MODULE);

        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("deploymentDescriptor", String.class, true);
        infoBuilder.addAttribute("configurationBaseUrl", URL.class, true);

        infoBuilder.addAttribute("contextPath", String.class, true);

        infoBuilder.addAttribute("securityHolder", SecurityHolder.class, true);
        infoBuilder.addAttribute("virtualServer", String.class, true);
        infoBuilder.addAttribute("componentContext", Map.class, true);
        infoBuilder.addAttribute("unshareableResources", Set.class, true);
        infoBuilder.addAttribute("applicationManagedSecurityResources", Set.class, true);
        infoBuilder.addAttribute("userTransaction", OnlineUserTransaction.class, true);
        infoBuilder.addReference("TransactionContextManager", TransactionContextManager.class, NameFactory.TRANSACTION_CONTEXT_MANAGER);
        infoBuilder.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class, NameFactory.JCA_CONNECTION_TRACKER);

        infoBuilder.addReference("Container", TomcatContainer.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference("RoleDesignateSource", RoleDesignateSource.class, NameFactory.JACC_MANAGER);
        infoBuilder.addReference("TomcatRealm", ObjectRetriever.class);
        infoBuilder.addReference("TomcatValveChain", ValveGBean.class);
        infoBuilder.addReference("Cluster", CatalinaClusterGBean.class, CatalinaClusterGBean.J2EE_TYPE);
        infoBuilder.addReference("Manager", ManagerGBean.class);
        infoBuilder.addAttribute("crossContext", boolean.class, true);
        infoBuilder.addAttribute("disableCookies", boolean.class, true);
        infoBuilder.addAttribute("webServices", Map.class, true);
        infoBuilder.addReference("J2EEServer", J2EEServer.class);
        infoBuilder.addReference("J2EEApplication", J2EEApplication.class);
        infoBuilder.addAttribute("kernel", Kernel.class, false);

        infoBuilder.addInterface(WebModule.class);

        infoBuilder.setConstructor(new String[] {
                "classLoader",
                "objectName",
                "deploymentDescriptor",
                "configurationBaseUrl",
                "securityHolder",
                "virtualServer",
                "componentContext",
                "unshareableResources",
                "applicationManagedSecurityResources",
                "userTransaction",
                "TransactionContextManager",
                "TrackedConnectionAssociator",
                "Container",
                "RoleDesignateSource",
                "TomcatRealm",
                "TomcatValveChain",
                "Cluster",
                "Manager",
                "crossContext",
                "disableCookies",
                "webServices",
                "J2EEServer",
                "J2EEApplication",
                "kernel"
                }
        );

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
