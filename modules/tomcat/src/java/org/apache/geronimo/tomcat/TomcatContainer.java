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
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Embedded;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

/**
 * Apache Tomcat GBean
 * 
 * @see http://wiki.apache.org/geronimo/Tomcat
 * @see http://nagoya.apache.org/jira/browse/GERONIMO-215
 * 
 * @version $Rev: 46019 $ $Date: 2004-09-14 11:56:06 +0200 (Tue, 14 Sep 2004) $
 */
public class TomcatContainer implements GBeanLifecycle {

    private static final Log log = LogFactory.getLog(TomcatContainer.class);

    /**
     * Reference to the org.apache.catalina.Embedded shell.
     */
    private Embedded shell;

    /**
     * Tomcat Host that will contain deployed contexts (webapps)
     */
    private Host host;

    /**
     * Tomcat Engine that will contain the host
     */
    private Engine engine;

    /**
     * Tomcat Connector that will process requests
     * 
     * TODO: Make it a GBean
     */
    private Connector connector;

    /**
     * Tomcat default Context
     * 
     * TODO: Make it a gbean
     */
    private Context defaultContext;

    /**
     * GBean constructor (invoked dynamically when the gbean is declared in a
     * plan)
     */
    public TomcatContainer() {
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
     * See org.apache.catalina.startup.Embedded for details (TODO: provide the
     * link to the javadoc)
     */
    public void doStart() throws Exception {
        log.debug("doStart()");

        // The comments are from the javadoc of the Embedded class

        // 1. Instantiate a new instance of this class.
        if (shell == null) {
            shell = new Embedded();
        }

        // 2. Set the relevant properties of this object itself. In particular,
        // you will want to establish the default Logger to be used, as well as
        // the default Realm if you are using container-managed security.
        shell.setUseNaming(false);

        // 3. Call createEngine() to create an Engine object, and then call its
        // property setters as desired.
        engine = shell.createEngine();
        engine.setName("Geronimo");

        // 4. Call createHost() to create at least one virtual Host associated
        // with the newly created Engine, and then call its property setters as
        // desired. After you customize this Host, add it to the corresponding
        // Engine with engine.addChild(host).
        host = shell.createHost("localhost", "");
        // TODO: Make it that gbean's attribute or tomcatwebappcontext's one
        ((StandardHost) host).setWorkDir("var/catalina");
        
        engine.setDefaultHost(host.getName());
        host.setParent(engine);
        engine.addChild(host);

        // 5. Call createContext() to create at least one Context associated
        // with each newly created Host, and then call its property setters as
        // desired. You SHOULD create a Context with a pathname equal to a
        // zero-length string, which will be used to process all requests not
        // mapped to some other Context. After you customize this Context, add
        // it to the corresponding Host with host.addChild(context).
        defaultContext = shell.createContext("", "");
        host.addChild(defaultContext);

        // 6. Call addEngine() to attach this Engine to the set of defined
        // Engines for this object.
        shell.addEngine(engine);

        // 7. Call createConnector() to create at least one TCP/IP connector,
        // and then call its property setters as desired.

        // It doesn't work - there's no HTTP connector created
        // connector = shell.createConnector((String) null, 8080, "http");

        // Create an HTTP/1.1 connector manually
        connector = new Connector("HTTP/1.1");
        connector.setPort(8080);

        // 8. Call addConnector() to attach this Connector to the set of defined
        // Connectors for this object. The added Connector will use the most
        // recently added Engine to process its received requests.
        shell.addConnector(connector);

        // 9. Call start() to initiate normal operations of all the attached
        // components.
        shell.start();
    }

    public void doStop() throws Exception {
        if (shell != null) {
            shell.stop();
            shell = null;
        }
    }

    /**
     * Creates and adds the context to the running host
     * 
     * It simply delegates the call to Tomcat's Embedded and Host classes
     * 
     * @param ctx
     *            the context to be added
     * 
     * @see org.apache.catalina.startup.Embedded
     * @see org.apache.catalina.Host
     */
    public void addContext(Context ctx) {
        ctx.setParent(host);
        host.addChild(ctx);
    }

    public void removeContext(Context ctx) {
        shell.removeContext(ctx);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Tomcat Web Container", TomcatContainer.class);

        infoFactory.addOperation("addContext", new Class[] { Context.class });
        infoFactory.addOperation("removeContext", new Class[] { Context.class });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}