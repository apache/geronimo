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


package org.apache.geronimo.security.jaspi;

import java.io.StringReader;

import javax.security.auth.message.config.AuthConfigFactory;

import org.apache.geronimo.components.jaspi.ConfigException;
import org.apache.geronimo.components.jaspi.JaspicUtil;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;

/**
 * Holds a bit of xml configuring an AuthConfigProvider, [Client|Server][AuthConfig|AuthContext|AuthModule]
 * and registers/unregisters it when start/stopped.
 *
 * @version $Rev$ $Date$
 */

@GBean
public class ServerAuthContextGBean implements GBeanLifecycle {

    private final String registrationID;

    public ServerAuthContextGBean(
            @ParamAttribute(name = "config") String config
    ) throws ConfigException {
        registrationID = JaspicUtil.registerServerAuthContext(new StringReader(config), true);
    }


    /**
     * Starts the GBean.  This informs the GBean that it is about to transition to the running state.
     *
     * @throws Exception if the target failed to start; this will cause a transition to the failed state
     */
    public void doStart() throws Exception {
    }

    /**
     * Stops the target.  This informs the GBean that it is about to transition to the stopped state.
     *
     * @throws Exception if the target failed to stop; this will cause a transition to the failed state
     */
    public void doStop() throws Exception {
        AuthConfigFactory authConfigFactory = AuthConfigFactory.getFactory();
        authConfigFactory.removeRegistration(registrationID);
    }

    /**
     * Fails the GBean.  This informs the GBean that it is about to transition to the failed state.
     */
    public void doFail() {
    }
}