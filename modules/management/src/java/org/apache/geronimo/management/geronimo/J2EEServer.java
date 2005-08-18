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
package org.apache.geronimo.management.geronimo;

/**
 * Geronimo-specific extensions to the standard J2EE server management
 * interface.
 *
 * @version $Rev: 46228 $ $Date: 2004-09-16 21:21:04 -0400 (Thu, 16 Sep 2004) $
 */
public interface J2EEServer extends org.apache.geronimo.management.J2EEServer {
    /**
     * Gets the ObjectName of the Web Container associated with this
     * J2EEServer, or null if there is none in the current server
     * configuration.
     *
     * @return The ObjectName of the web container, in String form.
     */
    public String getWebContainer();

    /**
     * Gets the ObjectName of the EJB Container associated with this
     * J2EEServer, or null if there is none in the current server
     * configuration.
     *
     * @return The ObjectName of the EJB container, in String form.
     */
    public String getEJBContainer();

    /**
     * Gets the ObjectNames of the thread pools associated with this
     * J2EEServer.
     *
     * @return The ObjectNames of the thread pools, in String form.
     */
    public String[] getThreadPools();
}
