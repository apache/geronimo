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
package org.apache.geronimo.jetty6.cluster;

import org.apache.geronimo.clustering.SessionManager;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jetty6.PreHandler;
import org.apache.geronimo.jetty6.SessionHandlerFactory;
import org.mortbay.jetty.servlet.SessionHandler;

/**
 *
 * @version $Rev$ $Date$
 */
public class ClusteredSessionHandlerFactory implements SessionHandlerFactory {
    private final SessionManager sessionManager;

    public ClusteredSessionHandlerFactory(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public SessionHandler createHandler(PreHandler preHandler) {
        ClusteredSessionManager clusteredSessionManager = new ClusteredSessionManager(sessionManager);
        return new ClusteredSessionHandler(clusteredSessionManager, preHandler);
    }

    public static final GBeanInfo GBEAN_INFO;

    public static final String GBEAN_REF_SESSION_MANAGER = "SessionManager";

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Clustered Web Application Handler Factory",
                ClusteredSessionHandlerFactory.class, NameFactory.GERONIMO_SERVICE);

        infoFactory.addReference(GBEAN_REF_SESSION_MANAGER, SessionManager.class, NameFactory.GERONIMO_SERVICE);

        infoFactory.addInterface(SessionHandlerFactory.class);

        infoFactory.setConstructor(new String[]{GBEAN_REF_SESSION_MANAGER});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
