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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;

/**
 * Wrapper for a WebApplicationContext that sets up its J2EE environment.
 * 
 * @version $Rev: 56022 $ $Date: 2004-10-30 07:16:18 +0200 (Sat, 30 Oct 2004) $
 */
public class TomcatWebAppContext extends StandardContext implements GBeanLifecycle {

    private static Log log = LogFactory.getLog(TomcatWebAppContext.class);

    private final TomcatContainer container;

    private final URI webAppRoot;

    public TomcatWebAppContext(URI webAppRoot, URI[] webClassPath, URL configurationBaseUrl, TomcatContainer container)
            throws MalformedURLException {

        assert webAppRoot != null;
        assert webClassPath != null;
        assert configurationBaseUrl != null;
        assert container != null;

        this.webAppRoot = webAppRoot;
        this.container = container;

        this.setDocBase(this.webAppRoot.getPath());
    }

    public void doStart() throws WaitingException, Exception {

        // See the note of TomcatContainer::addContext
        container.addContext(this);
        // Is it necessary - doesn't Tomcat Embedded take care of it?
        // super.start();

        log.info("TomcatWebAppContext started");
    }

    public void doStop() throws Exception {
        super.stop();
        container.removeContext(this);

        log.info("TomcatWebAppContext stopped");
    }

    public void doFail() {
        try {
            super.stop();
        } catch (LifecycleException e) {
        }

        container.removeContext(this);
        log.info("TomcatWebAppContext failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Tomcat WebApplication Context", TomcatWebAppContext.class);

        infoFactory.addAttribute("webAppRoot", URI.class, true);
        infoFactory.addAttribute("webClassPath", URI[].class, true);
        infoFactory.addAttribute("configurationBaseUrl", URL.class, true);

        infoFactory.addAttribute("path", String.class, true);

        infoFactory.addReference("Container", TomcatContainer.class);

        infoFactory.setConstructor(new String[] { "webAppRoot", "webClassPath", "configurationBaseUrl", "Container", });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
