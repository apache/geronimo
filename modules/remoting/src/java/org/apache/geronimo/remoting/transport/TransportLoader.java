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

package org.apache.geronimo.remoting.transport;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.remoting.router.Router;

/**
 * @version $Revision: 1.12 $ $Date: 2004/06/05 07:53:22 $
 */
public class TransportLoader implements GBeanLifecycle {
    private static final Log log = LogFactory.getLog(TransportLoader.class);
    private URI bindURI;
    private TransportServer transportServer;
    private Router router;

    public URI getBindURI() {
        return bindURI;
    }

    public void setBindURI(URI bindURI) {
        this.bindURI = bindURI;
    }

    public Router getRouter() {
        return router;
    }

    public void setRouter(Router router) {
        this.router = router;
    }

    public URI getClientConnectURI() {
        if (transportServer == null) {
            return null;
        }
        return transportServer.getClientConnectURI();
    }

    public void doStart() throws Exception {
        if (router == null) {
            throw new IllegalStateException("Target router was not set.");
        }
        TransportFactory tf = TransportFactory.getTransportFactory(bindURI);
        transportServer = tf.createSever();
        transportServer.bind(bindURI, router);
        transportServer.start();
    }

    public void doStop() throws Exception {
        if (transportServer != null) {
            transportServer.stop();
            transportServer.dispose();
            transportServer = null;
        }
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            log.error("Failed to shutdown", e);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(TransportLoader.class);

        infoFactory.addAttribute("ClientConnectURI", URI.class, false);
        infoFactory.addAttribute("BindURI", URI.class, true);

        infoFactory.addReference("Router", Router.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
