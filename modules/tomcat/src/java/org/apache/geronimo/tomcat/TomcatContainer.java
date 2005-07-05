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

import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.connector.Connector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.webservices.SoapHandler;
import org.apache.geronimo.webservices.WebServiceContainer;

/**
 * Apache Tomcat GBean
 * 
 * @see http://wiki.apache.org/geronimo/Tomcat
 * @see http://nagoya.apache.org/jira/browse/GERONIMO-215
 * 
 * @version $Rev: 46019 $ $Date: 2004-09-14 11:56:06 +0200 (Tue, 14 Sep 2004) $
 */
public class TomcatContainer implements SoapHandler, GBeanLifecycle {

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
     * Tomcat default Context
     * 
     * TODO: Make it a gbean
     */
    private Context defaultContext;

    /**
     * Geronimo class loader
     **/
    private ClassLoader classLoader;

    /**
     * Used only to resolve the paths
     */
    private ServerInfo serverInfo;

    private final Map webServices = new HashMap();

    // Required as it's referenced by deployed webapps
    public TomcatContainer() {
        setCatalinaHome(DEFAULT_CATALINA_HOME);
    }

    /**
     * GBean constructor (invoked dynamically when the gbean is declared in a plan)
     */
    public TomcatContainer(ClassLoader classLoader, String catalinaHome, ObjectRetriever engineGBean, ServerInfo serverInfo) {

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
        this.serverInfo = serverInfo;
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

        log.info("Endorsed Dirs set to:" + System.getProperty("java.endorsed.dirs"));
 
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
        Container[] hosts = engine.findChildren();
        for(int i = 0; i < hosts.length; i++){
            Context defaultContext = embedded.createContext("","", classLoader);
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
        Context anotherCtxObj = embedded.createContext(ctx.getPath(), ctx.getDocBase(), ctx.getWebClassLoader());

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
            throw new IllegalArgumentException("Invalid virtual host '" + virtualServer +"'.  Do you have a matchiing Host entry in the plan?");
        }
        
        if (ctx.getRealm() != null)
            anotherCtxObj.setRealm(ctx.getRealm());
        else
            anotherCtxObj.setRealm(host.getRealm());
            

        host.addChild(anotherCtxObj);
    }

    public void removeContext(TomcatContext ctx) {
        Context context = ctx.getContext();

        if (context != null)
            embedded.removeContext(context);

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
    
    public void addWebService(String contextPath, WebServiceContainer webServiceContainer, String securityRealmName, String realmName, String transportGuarantee, String authMethod, ClassLoader classLoader) throws Exception {
        Context webServiceContext = embedded.createEJBWebServiceContext(contextPath, webServiceContainer, securityRealmName, realmName, transportGuarantee, authMethod, classLoader);

        //TODO When OpenEJB supports virtual hosts, remove the next line
        String virtualServer = engine.getDefaultHost();

        //TODO When OpenEJB supports virtual hosts, uncomment the code below.  The 
        //virtualServer variable should be a String parameter from this function call
        //if (virtualServer == null)
        //    virtualServer = engine.getDefaultHost();
        
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
            context.destroy();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        embedded.removeContext(context);
        webServices.remove(contextPath);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Tomcat Web Container", TomcatContainer.class);

        infoFactory.setConstructor(new String[] { "classLoader", "catalinaHome", "engineGBean", "ServerInfo" });

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addAttribute("catalinaHome", String.class, true);

        infoFactory.addReference("engineGBean", ObjectRetriever.class, NameFactory.GERONIMO_SERVICE);

        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");

        infoFactory.addOperation("addContext", new Class[] { TomcatContext.class });
        infoFactory.addOperation("removeContext", new Class[] { TomcatContext.class });

        infoFactory.addOperation("addConnector", new Class[] { Connector.class });
        infoFactory.addOperation("removeConnector", new Class[] { Connector.class });

        infoFactory.addInterface(SoapHandler.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
