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

import org.apache.catalina.startup.Catalina;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.system.serverinfo.ServerInfo;

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
     * Reference to the org.apache.catalina.startup.Bootstrap shell. Right now
     * we're just wrapping up the shell, but we'll be replacing it with our own
     * GBean shell for ease of management.
     */
    private Catalina shell;

    /**
     * Used to resolve the location of the configuration file.
     */
    private final ServerInfo serverInfo;

    private String catalinaHome;

    private String catalinaBase;

    private String catalinaConfig = "var/catalina/server.xml";

    /**
     * Reference to the Catalina shell, to which calls are delegated.
     * 
     * The catalina shell relies on the "catalina.home" and "catalina.base"
     * System properties. Presumably, these could be added in a simple
     * properties file, but I'm going to work under the assumption that we'll
     * want them as persistent attributes in a server configuration. This will
     * make them more easily manageable (in theory--we'll see)
     */
    public TomcatContainer(ServerInfo serverInfo, String catalinaHome, String catalinaBase, String catalinaConfig) {
        this.serverInfo = serverInfo;
        this.catalinaHome = catalinaHome;
        this.catalinaBase = catalinaBase;
        this.catalinaConfig = catalinaConfig;
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception ignored) {
        }
    }

    public void doStart() throws Exception {
        log.debug("catalinaHome: " + catalinaHome + ", catalinaBase: " + catalinaBase + ", catalinaConfig: "
                + catalinaConfig);
        if (shell == null) {
            shell = new Catalina();
        }
        if (catalinaHome != null && catalinaHome.length() > 0) {
            catalinaHome = serverInfo.resolve(catalinaHome).getPath();
            log.debug("catalinaHome ServerInfo.resolve'd: " + catalinaHome);
            shell.setCatalinaHome(catalinaHome);
        }
        if (catalinaBase != null && catalinaBase.length() > 0) {
            catalinaBase = serverInfo.resolve(catalinaBase).getPath();
            log.debug("catalinaBase ServerInfo.resolve'd: " + catalinaBase);
            shell.setCatalinaBase(catalinaBase);
        }
        if (catalinaConfig != null && catalinaConfig.length() > 0) {
            shell.setConfig(catalinaConfig);
        }
        shell.setParentClassLoader(this.getClass().getClassLoader());
        shell.setUseNaming(false);
        shell.start();
    }

    public void doStop() throws Exception {
        if (shell != null) {
            shell.stop();
            shell = null;
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Tomcat Web Container", TomcatContainer.class);

        infoFactory.setConstructor(new String[] { "ServerInfo", "CatalinaHome", "CatalinaBase", "CatalinaConfig" });

        infoFactory.addReference("ServerInfo", ServerInfo.class);
        infoFactory.addAttribute("CatalinaHome", String.class, true);
        infoFactory.addAttribute("CatalinaBase", String.class, true);
        infoFactory.addAttribute("CatalinaConfig", String.class, true);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}