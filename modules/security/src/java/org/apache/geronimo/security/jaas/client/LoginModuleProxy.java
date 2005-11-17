/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.security.jaas.client;

import javax.security.auth.Subject;
import javax.security.auth.spi.LoginModule;

import org.apache.geronimo.security.jaas.server.JaasSessionId;
import org.apache.geronimo.security.jaas.server.JaasLoginServiceMBean;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;


/**
 * @version $Revision$ $Date$
 */
public abstract class LoginModuleProxy implements LoginModule {
    final protected LoginModuleControlFlag controlFlag;
    final protected Subject subject;

    public LoginModuleProxy(LoginModuleControlFlag controlFlag, Subject subject)
    {
        this.controlFlag = controlFlag;
        this.subject = subject;
    }

    public LoginModuleControlFlag getControlFlag() {
        return controlFlag;
    }
}
