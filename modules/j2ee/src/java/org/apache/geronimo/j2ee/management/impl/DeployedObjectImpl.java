/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.j2ee.management.impl;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.j2ee.management.J2EEServer;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:41 $
 */
public abstract class DeployedObjectImpl {
    private final String dd;
    private final J2EEServer server;

    public DeployedObjectImpl(String dd, J2EEServer server) {
        this.dd = dd;
        this.server = server;
    }

    /**
     * The deploymentDescriptor string must contain the original XML deployment
     * descriptor that was created for this module during the deployment process.
     * @see "JSR77.3.5.0.1"
     * @return this module's deployment descriptor
     */
    public String getdeploymentDescriptor() {
        return dd;
    }

    /**
     * The J2EE server the application or module is deployed on.
     * @see "JSR77.3.5.0.2"
     * @return the server this module is deployed on
     */
    public String getserver() {
        return server.getobjectName();
    }

    protected static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(DeployedObjectImpl.class);
        infoFactory.addAttribute("deploymentDescriptor", true);
        infoFactory.addReference("server", J2EEServer.class);
        infoFactory.setConstructor(
                new String[]{"deploymentDescriptor", "server"},
                new Class[]{String.class, J2EEServer.class}
        );
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
