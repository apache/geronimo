/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Realm;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.realm.JAASRealm;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.realm.TomcatGeronimoRealm;
import org.apache.geronimo.tomcat.realm.TomcatJAASRealm;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.webservices.SoapHandler;
import org.apache.geronimo.webservices.WebServiceContainer;



/**
 * Apache Tomcat GBean
 *
 * @see http://wiki.apache.org/geronimo/Tomcat
 * @see http://nagoya.apache.org/jira/browse/GERONIMO-215
 *
 * @version $Rev$ $Date$
 */
public class TomcatContainer implements SoapHandler, GBeanLifecycle, TomcatWebContainer {

    private static final Log log = LogFactory.getLog(TomcatContainer.class);

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
     **/
    private ClassLoader classLoader;

    private final Map webServices = new HashMap();
    private final String objectName;

    // Required as it's referenced by deployed webapps
    public TomcatContainer() {
        this.objectName = null; // is this OK??
        setCatalinaHome(DEFAULT_CATALINA_HOME);
    }

    /**
     * GBean constructor (invoked dynamically when the gbean is declared in a plan)
     */
    public TomcatContainer(ClassLoader classLoader, String catalinaHome, ObjectRetriever engineGBean, ServerInfo serverInfo, String objectName) {
        if (catalinaHome == null)
            catalinaHome = DEFAULT_CATALINA_HOME;

        setCatalinaHome(serverInfo.resolvePath(catalinaHome));

        if (classLoader == null){
            throw new IllegalArgumentException("classLoader cannot be null.");
        }

        if (engineGBean == null){
            throw new IllegalArgumentException("engineGBean cannot be null.");
        }

        this.classLoader = classLoader;

        this.engine = (Engine)engineGBean.getInternalObject();

        this.objectName = objectName;
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

    public void doFail() {
        try {
            doStop();
        } catch (Exception ignored) {
        }
    }

    /**
     * Instantiate and start up Tomcat's Embedded class
     *
     * See org.apache.catalina.startup.Embedded for details (TODO: provide the link to the javadoc)
     */
    public void doStart() throws Exception {
        log.debug("doStart()");

        log.debug("Endorsed Dirs set to:" + System.getProperty("java.endorsed.dirs"));

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

        TomcatClassLoader tcl = null;
        if (rootContext.exists())
            tcl = createRootClassLoader(rootContext, classLoader);

        Container[] hosts = engine.findChildren();
        Context defaultContext = null;
        for(int i = 0; i < hosts.length; i++){
            if (rootContext.exists()){
                defaultContext = embedded.createContext("","ROOT", tcl);
            } else {
                defaultContext = embedded.createContext("","", classLoader);
            }
            hosts[i].addChild(defaultContext);
        }

        // 6. Call addEngine() to attach this Engine to the set of defined
        // Engines for this object.
        embedded.addEngine(engine);

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
     *
     * It simply delegates the call to Tomcat's Embedded and Host classes
     *
     * @param ctx the context to be added
     *
     * @see org.apache.catalina.startup.Embedded
     * @see org.apache.catalina.Host
     */
    public void addContext(TomcatContext ctx) throws Exception{
        Context anotherCtxObj = embedded.createContext(ctx.getContextPath(), ctx.getDocBase(), ctx.getWebClassLoader());

        // Set the context for the Tomcat implementation
        ctx.setContext(anotherCtxObj);

        // Have the context to set its properties if its a GeronimoStandardContext
        if (anotherCtxObj instanceof GeronimoStandardContext)
            ((GeronimoStandardContext)anotherCtxObj).setContextProperties(ctx);

        //Was a virtual server defined?
        String virtualServer = ctx.getVirtualServer();
        if (virtualServer == null)
            virtualServer = engine.getDefaultHost();

        Container host = engine.findChild(virtualServer);
        if (host == null){
            throw new IllegalArgumentException("Invalid virtual host '" + virtualServer +"'.  Do you have a matching Host entry in the plan?");
        }

        //Get the security-realm-name if there is one
        String securityRealmName = null;
        SecurityHolder secHolder = ctx.getSecurityHolder();
        if (secHolder != null)
            securityRealmName = secHolder.getSecurityRealm();

        //Did we declare a GBean at the context level?
        if (ctx.getRealm() != null){
            Realm realm = ctx.getRealm();

            //Allow for the <security-realm-name> override from the
            //geronimo-web.xml file to be used if our Realm is a JAAS type
            if (securityRealmName != null){
                if (realm instanceof JAASRealm){
                    ((JAASRealm)realm).setAppName(securityRealmName);
                }
            }
            anotherCtxObj.setRealm(realm);
        } else {
            Realm realm = host.getRealm();
            //Check and see if we have a declared realm name and no match to a parent name
            if (securityRealmName != null){
                String parentRealmName = null;
                if (realm instanceof JAASRealm){
                    parentRealmName = ((JAASRealm)realm).getAppName();
                }

                //Do we have a match to a parent?
                if(!securityRealmName.equals(parentRealmName)){
                    //No...we need to create a default adapter

                    //Is the context requiring JACC?
                    if (secHolder.isSecurity()){
                        //JACC
                        realm = new TomcatGeronimoRealm();
                    } else {
                        //JAAS
                        realm = new TomcatJAASRealm();
                    }

                    log.info("The security-realm-name '" + securityRealmName +
                            "' was specified and a parent (Engine/Host) is not named the same or no RealmGBean was configured for this context. " +
                            "Creating a default " + realm.getClass().getName() +
                            " adapter for this context.");

                    ((JAASRealm)realm).setUserClassNames("org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
                    ((JAASRealm)realm).setRoleClassNames("org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal");
                    ((JAASRealm)realm).setAppName(securityRealmName);

                    anotherCtxObj.setRealm(realm);
                } else {
                    //Use the parent since a name matches
                    anotherCtxObj.setRealm(realm);
                }
            } else {
                anotherCtxObj.setRealm(realm);
            }
        }

        host.addChild(anotherCtxObj);
    }

    public void removeContext(TomcatContext ctx) {
        Context context = ctx.getContext();

        if (context != null){
            if (context instanceof GeronimoStandardContext){
                GeronimoStandardContext stdctx = (GeronimoStandardContext)context;
                
                try{
                    stdctx.stop();
                    stdctx.destroy();
                } catch (Exception e){
                    throw new RuntimeException(e);
                }

            }
            context.getParent().removeChild(context);
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
        if (host == null){
            throw new IllegalArgumentException("Invalid virtual host '" + virtualServer +"'.  Do you have a matchiing Host entry in the plan?");
        }

        host.addChild(webServiceContext);
        webServices.put(contextPath, webServiceContext);
    }

    public void removeWebService(String contextPath) {
        TomcatEJBWebServiceContext context = (TomcatEJBWebServiceContext) webServices.get(contextPath);
        try{
            context.stop();
            context.destroy();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        context.getParent().removeChild(context);
        webServices.remove(contextPath);
    }

    private TomcatClassLoader createRootClassLoader(File baseDir, ClassLoader cl) throws Exception{
        ArrayList urls = new ArrayList();

        File webInfDir = new File(baseDir, "WEB-INF");

        // check for a classes dir
        File classesDir = new File(webInfDir, "classes");
        if (classesDir.isDirectory()) {
            urls.add(classesDir.toURL());
        }

        // add all of the libs
        File libDir = new File(webInfDir, "lib");
        if (libDir.isDirectory()) {
            File[] libs = libDir.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file.isFile() && file.getName().endsWith(".jar");
                }
            });

            if (libs != null) {
                for (int i = 0; i < libs.length; i++) {
                    File lib = libs[i];
                    urls.add(lib.toURL());
                }
            }
        }

        return new TomcatClassLoader((URL[])urls.toArray(new URL[0]), null, cl, false);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Tomcat Web Container", TomcatContainer.class);

        infoFactory.setConstructor(new String[] { "classLoader", "catalinaHome", "EngineGBean", "ServerInfo", "objectName"});

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addAttribute("catalinaHome", String.class, true);

        infoFactory.addAttribute("objectName", String.class, false);

        infoFactory.addReference("EngineGBean", ObjectRetriever.class, NameFactory.GERONIMO_SERVICE);

        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");

        infoFactory.addOperation("addContext", new Class[] { TomcatContext.class });
        infoFactory.addOperation("removeContext", new Class[] { TomcatContext.class });

        infoFactory.addOperation("addConnector", new Class[] { Connector.class });
        infoFactory.addOperation("removeConnector", new Class[] { Connector.class });

        infoFactory.addInterface(SoapHandler.class);
        infoFactory.addInterface(TomcatWebContainer.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
