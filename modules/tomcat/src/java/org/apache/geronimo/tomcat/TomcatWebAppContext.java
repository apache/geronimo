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

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.catalina.Context;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.J2EEApplication;
import org.apache.geronimo.j2ee.management.J2EEServer;
import org.apache.geronimo.j2ee.management.WebModule;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.security.jacc.RoleDesignateSource;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.OnlineUserTransaction;
import org.apache.geronimo.transaction.context.TransactionContextManager;

/**
 * Wrapper for a WebApplicationContext that sets up its J2EE environment.
 *
 * @version $Rev$ $Date$
 */
public class TomcatWebAppContext implements GBeanLifecycle, TomcatContext, WebModule {

    private static Log log = LogFactory.getLog(TomcatWebAppContext.class);

    protected final TomcatContainer container;

    private final ClassLoader webClassLoader;

    protected Context context = null;

    private final URI webAppRoot;

    private String path = null;

    private String docBase = null;

    private String virtualServer = null;

    private final Realm realm;

    private final List valveChain;

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

    public TomcatWebAppContext(
            ClassLoader classLoader,
            String objectName,
            String originalSpecDD,
            URI webAppRoot,
            URI[] webClassPath,
            boolean contextPriorityClassLoader,
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
            Map webServices,
            J2EEServer server,
            J2EEApplication application,
            Kernel kernel)
            throws Exception {

        assert classLoader != null;
        assert webAppRoot != null;
        assert webClassPath != null;
        assert configurationBaseUrl != null;
        assert transactionContextManager != null;
        assert trackedConnectionAssociator != null;
        assert componentContext != null;
        assert container != null;


        this.objectName = objectName;
        this.webAppRoot = webAppRoot;
        this.container = container;
        this.originalSpecDD = originalSpecDD;

        this.setDocBase(this.webAppRoot.getPath());
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

        if (tomcatRealm != null){
            realm = (Realm)tomcatRealm.getInternalObject();
            if (!(realm instanceof Realm)){
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

        this.webServices = webServices;

        URI root = URI.create(configurationBaseUrl.toString());
        if (configurationBaseUrl.getProtocol().equalsIgnoreCase("file")) {
            root = new URI("file", configurationBaseUrl.getPath(), null);
        } else {
            root = URI.create(configurationBaseUrl.toString());
        }
        URL webAppRootURL = webAppRoot.toURL();

        URL[] urls = new URL[webClassPath.length];
        for (int i = 0; i < webClassPath.length; i++) {
            URI classPathEntry = webClassPath[i];
            classPathEntry = root.resolve(classPathEntry);
            urls[i] = classPathEntry.toURL();
        }
        
        this.webClassLoader = new TomcatClassLoader(urls, webAppRootURL, classLoader, contextPriorityClassLoader);

        this.kernel = kernel;
        ObjectName myObjectName = JMXUtil.getObjectName(objectName);
        verifyObjectName(myObjectName);

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

    public String getServer() {
        return server.getObjectName();
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

    public ClassLoader getWebClassLoader() {
        return webClassLoader;
    }

    public Kernel getKernel() {
        return kernel;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
        return server.getJavaVMs();
    }

    public String getDeploymentDescriptor() {
        return originalSpecDD;
    }

    public void doStart() throws Exception {

        // See the note of TomcatContainer::addContext
        container.addContext(this);
        // Is it necessary - doesn't Tomcat Embedded take care of it?
        // super.start();

        log.info("TomcatWebAppContext started");
    }

    public void doStop() throws Exception {
        container.removeContext(this);

        log.info("TomcatWebAppContext stopped");
    }

    public void doFail() {
        container.removeContext(this);

        log.info("TomcatWebAppContext failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(
                "Tomcat WebApplication Context", TomcatWebAppContext.class,
                NameFactory.WEB_MODULE);

        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("deploymentDescriptor", String.class, true);
        infoBuilder.addAttribute("webAppRoot", URI.class, true);
        infoBuilder.addAttribute("webClassPath", URI[].class, true);
        infoBuilder.addAttribute("contextPriorityClassLoader", boolean.class, true);
        infoBuilder.addAttribute("configurationBaseUrl", URL.class, true);

        infoBuilder.addAttribute("path", String.class, true);

        infoBuilder.addAttribute("securityHolder", SecurityHolder.class, true);
        infoBuilder.addAttribute("virtualServer", String.class, true);
        infoBuilder.addAttribute("componentContext", Map.class, true);
        infoBuilder.addAttribute("unshareableResources", Set.class, true);
        infoBuilder.addAttribute("applicationManagedSecurityResources", Set.class, true);
        infoBuilder.addAttribute("userTransaction",
                OnlineUserTransaction.class, true);
        infoBuilder.addReference("transactionContextManager",
                TransactionContextManager.class, NameFactory.JTA_RESOURCE);
        infoBuilder.addReference("trackedConnectionAssociator",
                TrackedConnectionAssociator.class, NameFactory.JCA_CONNECTION_TRACKER);

        infoBuilder.addReference("Container", TomcatContainer.class,
                NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference("RoleDesignateSource",
                RoleDesignateSource.class, NameFactory.JACC_MANAGER);
        infoBuilder.addReference("TomcatRealm", ObjectRetriever.class);
        infoBuilder.addReference("TomcatValveChain", ValveGBean.class);
        infoBuilder.addAttribute("webServices", Map.class, true);
        infoBuilder.addReference("J2EEServer", J2EEServer.class);
        infoBuilder.addReference("J2EEApplication", J2EEApplication.class);
        infoBuilder.addAttribute("kernel", Kernel.class, false);

        infoBuilder.addInterface(WebModule.class);

        infoBuilder.setConstructor(new String[] {
                "classLoader",
                "objectName",
                "deploymentDescriptor",
                "webAppRoot",
                "webClassPath",
                "contextPriorityClassLoader",
                "configurationBaseUrl",
                "securityHolder",
                "virtualServer",
                "componentContext",
                "unshareableResources",
                "applicationManagedSecurityResources",
                "userTransaction",
                "transactionContextManager",
                "trackedConnectionAssociator",
                "Container",
                "RoleDesignateSource",
                "TomcatRealm",
                "TomcatValveChain",
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
