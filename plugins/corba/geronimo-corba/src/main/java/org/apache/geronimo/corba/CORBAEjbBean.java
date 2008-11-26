/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.corba;

import org.apache.geronimo.openejb.OpenEjbSystem;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.corba.security.config.ConfigAdapter;
import org.apache.geronimo.corba.security.config.ssl.SSLConfig;

/**
 * @version $Rev$ $Date$
 */
public class CORBAEjbBean extends CORBABean {

    private final OpenEjbSystem ejbSystem;

    /**
     * Instantiate a CORBABean instance.
     *
     * @param abstractName  The server-created abstract name for this bean instance.
     * @param configAdapter The ORB ConfigAdapter used to interface with the
     *                      JVM-configured ORB instance.
     * @param host          The hostname we publish ourselves under.
     * @param listenerPort  The initial listener port to use.
     * @param classLoader   The ClassLoader used for ORB context class loading.
     * @param nameService   The initial name service the created ORB will use
     *                      for object resolution.
     * @param ssl           The SSL configuration, including the KeystoreManager.
     */
    public CORBAEjbBean(AbstractName abstractName, ConfigAdapter configAdapter, String host, int listenerPort, ClassLoader classLoader, NameService nameService, OpenEjbSystem ejbSystem, SSLConfig ssl) {
        super(abstractName, configAdapter, host, listenerPort, classLoader, nameService, ssl);
        this.ejbSystem = ejbSystem;
    }

    /**
     * Start the ORB associated with this bean instance.
     *
     * @throws Exception
     */
    @Override
    public void doStart() throws Exception {
        super.doStart();
        // if we have an OpenEjbSystem reference, inform the ejb subsystem
        // there's now an ORB available for the JNDI context.
        if (ejbSystem != null) {
            ejbSystem.setORBContext(getORB(), getHandleDelegate());
        }
    }
}
