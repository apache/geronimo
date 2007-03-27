/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb;

import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.SubjectId;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.security.jaas.UsernamePasswordCallbackHandler;
import org.apache.openejb.spi.SecurityService;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.jacc.EJBMethodPermission;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.Principal;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoSecurityService implements SecurityService {
    public void init(Properties props) throws Exception {
    }

    public Object login(String user, String pass) throws LoginException {
        LoginContext context = new LoginContext("OpenEJB", new UsernamePasswordCallbackHandler(user, pass));
        context.login();

        Subject subject = context.getSubject();
        SubjectId subjectId = ContextManager.registerSubject(subject);
        return subjectId;
    }

    public void logout(Object securityIdentity) {
        Subject subject = ContextManager.getRegisteredSubject((SubjectId) securityIdentity);
        ContextManager.unregisterSubject(subject);
    }

    public void associate(Object securityIdentity) throws LoginException {
        if (securityIdentity == null) {
            return;
        }

        Subject subject = ContextManager.getRegisteredSubject((SubjectId) securityIdentity);
        if (subject == null) {
            return;
        }
        ContextManager.setCallers(subject, subject);
    }

    public void unassociate(Object securityIdentity) {
        // this is only called before the thread is put back in the pool so it should be ok
        ContextManager.popCallers(null);
    }

    public boolean isCallerAuthorized(Method method, InterfaceType typee) {
        if (true) return true;
        ThreadContext threadContext = ThreadContext.getThreadContext();

        try {
            CoreDeploymentInfo deploymentInfo = threadContext.getDeploymentInfo();

            String ejbName = deploymentInfo.getEjbName();

            InterfaceType type = deploymentInfo.getInterfaceType(method.getDeclaringClass());

            String name = (type == null)? null: type.getSpecName();

            Permission permission = new EJBMethodPermission(ejbName, name, method);

            AccessControlContext accessContext = ContextManager.getCurrentContext();

            if (permission != null) accessContext.checkPermission(permission);

        } catch (AccessControlException e) {
            return false;
        }
        return true;
    }

    public boolean isCallerInRole(String role) {
        if (role == null) throw new IllegalArgumentException("Role must not be null");

        ThreadContext threadContext = ThreadContext.getThreadContext();

        CoreDeploymentInfo deployment = threadContext.getDeploymentInfo();
        return ContextManager.isCallerInRole(deployment.getEjbName(), role);
    }

    public Principal getCallerPrincipal() {
        Subject callerSubject = ContextManager.getCurrentCaller();
        return ContextManager.getCurrentPrincipal(callerSubject);
    }

    //
    // Unused
    //

    public Object getSecurityIdentity() {
        // throw new UnsupportedOperationException();
        return null;
    }

    public void setSecurityIdentity(Object securityIdentity) {
        throw new UnsupportedOperationException();
    }

    public <T> T translateTo(Object securityIdentity, Class<T> type) {
        throw new UnsupportedOperationException();
    }

    public Subject getCurrentSubject() {
        throw new UnsupportedOperationException();
    }

}
