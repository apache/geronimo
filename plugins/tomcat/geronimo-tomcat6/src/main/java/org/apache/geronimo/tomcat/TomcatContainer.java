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

import java.io.File;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Realm;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.realm.JAASRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.realm.TomcatGeronimoRealm;
import org.apache.geronimo.tomcat.realm.TomcatJAASRealm;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.webservices.SoapHandler;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.naming.resources.DirContextURLStreamHandlerFactory;


/**
 * Apache Tomcat GBean
 * http://wiki.apache.org/geronimo/Tomcat
 * http://nagoya.apache.org/jira/browse/GERONIMO-215
 *
 * @version $Rev$ $Date$
 */
public class TomcatContainer implements SoapHandler, GBeanLifecycle, TomcatWebContainer {

    private static final Logger log = LoggerFactory.getLogger(TomcatContainer.class);

    /**
     * The default value of CATALINA_HOME variable
     */
    private static final String DEFAULT_CATALINA_HOME = "var/catalina";

    /**
     * Reference to the org.apache.catalina.Embedded embedded.
     */
    private TomcatGeronimoEmbedded embedded;

    /**
     * Tomcat Engine that will contain the host
     */
    private Engine engine;

    /**
     * Geronimo class loader
     */
    private ClassLoader classLoader;

    private final Map webServices = new HashMap();
    private final String objectName;
    private final String[] applicationListeners;
    private final WebManager manager;
    private static boolean first = true;
    private final LifecycleListenerGBean listenerChain;

    // Required as it's referenced by deployed webapps
    public TomcatContainer() {
        this.objectName = null; // is this OK??
        this.applicationListeners = null;
        setCatalinaHome(DEFAULT_CATALINA_HOME);
        manager = null;
        listenerChain=null;
    }

    /**
     * GBean constructor (invoked dynamically when the gbean is declared in a plan)
     */
    public TomcatContainer(
            ClassLoader classLoader, 
            String catalinaHome, 
            String[] applicationListeners, 
            ObjectRetriever engineGBean, 
            LifecycleListenerGBean listenerChain,
            ServerInfo serverInfo, 
            String objectName, 
            WebManager manager) {
        // Register a stream handler factory for the JNDI protocol
        URLStreamHandlerFactory streamHandlerFactory =
            new DirContextURLStreamHandlerFactory();
        if (first) {
            first = false;
            try {
                URL.setURLStreamHandlerFactory(streamHandlerFactory);
            } catch (Exception e) {
                // Log and continue anyway, this is not critical
                log.error("Error registering jndi stream handler", e);
            } catch (Throwable t) {
                // This is likely a dual registration
                log.info("Dual registration of jndi stream handler: "
                         + t.getMessage());
            }
        }


        if (catalinaHome == null)
            catalinaHome = DEFAULT_CATALINA_HOME;

        setCatalinaHome(serverInfo.resolveServerPath(catalinaHome));

        if (classLoader == null) {
            throw new IllegalArgumentException("classLoader cannot be null.");
        }

        if (engineGBean == null) {
            throw new IllegalArgumentException("engineGBean cannot be null.");
        }
        

        this.classLoader = classLoader;

        this.engine = (Engine) engineGBean.getInternalObject();
        this.listenerChain = listenerChain;
        this.objectName = objectName;
        this.applicationListeners = applicationListeners;
        this.manager = manager;
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false; // todo: return true once stats are integrated
    }

    public boolean isEventProvider() {
        return true;
    }

    public NetworkConnector[] getConnectors() {
        return manager.getConnectorsForContainer(this);
    }

    public NetworkConnector[] getConnectors(String protocol) {
        return manager.getConnectorsForContainer(this, protocol);
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception ignored) {
        }
    }

    /**
     * Instantiate and start up Tomcat's Embedded class
     * <p/>
     * See org.apache.catalina.startup.Embedded for details (TODO: provide the link to the javadoc)
     */
    public void doStart() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("doStart()");
            log.debug("Java Endorsed Dirs set to:" + System.getProperty("java.endorsed.dirs"));
            log.debug("Java Ext Dirs set to:" + System.getProperty("java.ext.dirs"));
        }

        // The comments are from the javadoc of the Embedded class

        // 1. Instantiate a new instance of this class.
        if (embedded == null) {
            embedded = new TomcatGeronimoEmbedded();
        }

        // Assemble FileLogger as a gbean
        /*
         * FileLogger fileLog = new FileLogger(); fileLog.setDirectory("."); fileLog.setPrefix("vsjMbedTC5");
         * fileLog.setSuffix(".log"); fileLog.setTimestamp(true);
         */

        // 2. Set the relevant properties of this object itself. In particular,
        // you will want to establish the default Logger to be used, as well as
        // the default Realm if you are using container-managed security.
        embedded.setUseNaming(false);

        //Add default contexts
        File rootContext = new File(System.getProperty("catalina.home") + "/ROOT");

        String docBase = "";
        if (rootContext.exists()) {
            docBase = "ROOT";
        }

        Container[] hosts = engine.findChildren();
        Context defaultContext;
        ObjectName objName = objectName == null ? null : ObjectName.getInstance(objectName);
        for (int i = 0; i < hosts.length; i++) {
            defaultContext = embedded.createContext("", docBase, classLoader);
            if(objName != null) {
                defaultContext.setName(objName.getKeyProperty(NameFactory.J2EE_NAME));
            }
            if (defaultContext instanceof GeronimoStandardContext) {
                GeronimoStandardContext ctx = (GeronimoStandardContext) defaultContext;
                // Without this the Tomcat FallBack Application is left behind,
                // MBean - ...J2EEApplication=none,J2EEServer=none,..........
                ctx.setJ2EEApplication(null);
                // if objectName != null extract J2EEServer from objectName/host
                ctx.setJ2EEServer(objName == null ? "geronimo" : objName.getKeyProperty(NameFactory.J2EE_SERVER));
                ctx.setJavaVMs(new String[]{});
                ctx.setServer(objName == null ? "geronimo" : objName.getKeyProperty(NameFactory.J2EE_SERVER));
            }
            hosts[i].addChild(defaultContext);
        }

        // 6. Call addEngine() to attach this Engine to the set of defined
        // Engines for this object.
        embedded.addEngine(engine);
        
        if (listenerChain != null){
            LifecycleListenerGBean listenerGBean = listenerChain;
            while(listenerGBean != null){
                embedded.addLifecycleListener((LifecycleListener)listenerGBean.getInternalObject());
                listenerGBean = listenerGBean.getNextListener();
            }
        }

        // 9. Call start() to initiate normal operations of all the attached
        // components.
        embedded.start();
    }

    public void doStop() throws Exception {
        if (embedded != null) {
            embedded.stop();
            embedded = null;
        }

    }

    /**
     * Creates and adds the context to the running host
     * <p/>
     * It simply delegates the call to Tomcat's Embedded and Host classes
     *
     * @param ctx the context to be added
     * @see org.apache.catalina.startup.Embedded
     * @see org.apache.catalina.Host
     */
    public void addContext(TomcatContext ctx) throws Exception {
        Context anotherCtxObj = embedded.createContext(ctx.getContextPath(), ctx.getDocBase(), ctx.getClassLoader());

        // Set the context for the Tomcat implementation
        ctx.setContext(anotherCtxObj);

        // Have the context to set its properties if its a GeronimoStandardContext
        if (anotherCtxObj instanceof GeronimoStandardContext) {
            ((GeronimoStandardContext) anotherCtxObj).setContextProperties(ctx);
        }
        //Was a virtual server defined?
        String virtualServer = ctx.getVirtualServer();
        if (virtualServer == null) {
            virtualServer = engine.getDefaultHost();
        }
        Container host = engine.findChild(virtualServer);
        if (host == null) {
            throw new IllegalArgumentException("Invalid virtual host '" + virtualServer + "'.  Do you have a matching Host entry in the plan?");
        }

        //Get the security-realm-name if there is one
        String securityRealmName = null;
        SecurityHolder secHolder = ctx.getSecurityHolder();
        if (secHolder != null)
            securityRealmName = secHolder.getSecurityRealm();

        //Did we declare a GBean at the context level?
        if (ctx.getRealm() != null) {
            Realm realm = ctx.getRealm();

            //Allow for the <security-realm-name> override from the
            //geronimo-web.xml file to be used if our Realm is a JAAS type
            if (securityRealmName != null) {
                if (realm instanceof JAASRealm) {
                    ((JAASRealm) realm).setAppName(securityRealmName);
                }
            }
            anotherCtxObj.setRealm(realm);
        } else {
            Realm realm = host.getRealm();
            //Check and see if we have a declared realm name and no match to a parent name
            if (securityRealmName != null) {
                String parentRealmName = null;
                if (realm instanceof JAASRealm) {
                    parentRealmName = ((JAASRealm) realm).getAppName();
                }

                //Do we have a match to a parent?
                if (!securityRealmName.equals(parentRealmName)) {
                    //No...we need to create a default adapter

                    //Is the context requiring JACC?
                    if (secHolder.isSecurity()) {
                        //JACC
                        realm = new TomcatGeronimoRealm();
                    } else {
                        //JAAS
                        realm = new TomcatJAASRealm();
                        ((JAASRealm) realm).setUserClassNames("org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
                        ((JAASRealm) realm).setRoleClassNames("org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal");
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("The security-realm-name '" + securityRealmName +
                            "' was specified and a parent (Engine/Host) is not named the same or no RealmGBean was configured for this context. " +
                            "Creating a default " + realm.getClass().getName() +
                            " adapter for this context.");
                    }

                    ((JAASRealm) realm).setAppName(securityRealmName);

                    anotherCtxObj.setRealm(realm);
                } else {
                    //Use the parent since a name matches
                    anotherCtxObj.setRealm(realm);
                }
            } else {
                anotherCtxObj.setRealm(realm);
            }
        }
        
        // add application listeners to the new context
        if (applicationListeners != null) {
            for (String listener : applicationListeners) {
                anotherCtxObj.addApplicationListener(listener);
            }
        }
        
        try {
            host.addChild(anotherCtxObj);
        } catch (IllegalArgumentException ex) {
            log.error("Unable to add the child container: " + anotherCtxObj.getName() 
                    + " .  Please check if your project's context-root is unique.", ex);
        }
    }

    public void removeContext(TomcatContext ctx) {
        Context context = ctx.getContext();

        if (context != null) {
            if (context instanceof GeronimoStandardContext) {
                GeronimoStandardContext stdctx = (GeronimoStandardContext) context;
                
                try {
                    stdctx.kill();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
            if (context.getParent() != null) {
                context.getParent().removeChild(context);
            }
        }

    }

    public void setCatalinaHome(String catalinaHome) {
        System.setProperty("catalina.home", catalinaHome);
    }

    public void addConnector(Connector connector) {
        embedded.addConnector(connector);
    }

    public void removeConnector(Connector connector) {
        embedded.removeConnector(connector);
    }

    public void addWebService(String contextPath, String[] virtualHosts, WebServiceContainer webServiceContainer, String securityRealmName, String realmName, String transportGuarantee, String authMethod, ClassLoader classLoader) throws Exception {
        Context webServiceContext = embedded.createEJBWebServiceContext(contextPath, webServiceContainer, securityRealmName, realmName, transportGuarantee, authMethod, classLoader);

        String virtualServer;
        if (virtualHosts != null && virtualHosts.length > 0) {
            virtualServer = virtualHosts[0];
        } else {
            virtualServer = engine.getDefaultHost();
        }

        Container host = engine.findChild(virtualServer);
        if (host == null) {
            throw new IllegalArgumentException("Invalid virtual host '" + virtualServer + "'.  Do you have a matchiing Host entry in the plan?");
        }

        host.addChild(webServiceContext);
        webServices.put(contextPath, webServiceContext);
    }

    public void removeWebService(String contextPath) {
        TomcatEJBWebServiceContext context = (TomcatEJBWebServiceContext) webServices.get(contextPath);
        try {
            context.stop();
            context.destroy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        context.getParent().removeChild(context);
        webServices.remove(contextPath);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Tomcat Web Container", TomcatContainer.class);

        infoFactory.setConstructor(new String[]{
                "classLoader", 
                "catalinaHome", 
                "applicationListeners", 
                "EngineGBean", 
                "LifecycleListenerChain",
                "ServerInfo", 
                "objectName", 
                "WebManager"});

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addAttribute("catalinaHome", String.class, true);

        infoFactory.addAttribute("applicationListeners", String[].class, true);

        infoFactory.addAttribute("objectName", String.class, false);

        infoFactory.addReference("EngineGBean", ObjectRetriever.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoFactory.addReference("LifecycleListenerChain", LifecycleListenerGBean.class, LifecycleListenerGBean.J2EE_TYPE);

        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addReference("WebManager", WebManager.class);

        infoFactory.addOperation("addContext", new Class[]{TomcatContext.class});
        infoFactory.addOperation("removeContext", new Class[]{TomcatContext.class});

        infoFactory.addOperation("addConnector", new Class[]{Connector.class});
        infoFactory.addOperation("removeConnector", new Class[]{Connector.class});

        infoFactory.addInterface(SoapHandler.class);
        infoFactory.addInterface(TomcatWebContainer.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
