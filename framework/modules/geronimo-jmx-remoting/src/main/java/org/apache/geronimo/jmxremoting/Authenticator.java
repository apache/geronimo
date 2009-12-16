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
package org.apache.geronimo.jmxremoting;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectionNotification;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal;
/**
 * JMX Authenticator that checks the Credentials by logging in via JAAS.
 *
 * @version $Rev$ $Date$
 */
public class Authenticator implements JMXAuthenticator, NotificationListener {
    private final String configName;
    private final ClassLoader cl;
    private ThreadLocal<LoginContext> threadContext = new ThreadLocal<LoginContext>();
    private Map<String, LoginContext> contextMap = new ConcurrentHashMap<String, LoginContext>();

    /**
     * Constructor indicating which JAAS Application Configuration Entry to use.
     * @param configName the JAAS config name
     * @param cl classloader to use as TCCL for operations
     */
    public Authenticator(String configName, ClassLoader cl) {
        this.configName = configName;
        this.cl = cl;
    }

    public Subject authenticate(Object o) throws SecurityException {
        if (!(o instanceof String[])) {
            throw new IllegalArgumentException("Expected String[2], got " + (o == null ? null : o.getClass().getName()));
        }
        String[] params = (String[]) o;
        if (params.length != 2) {
            throw new IllegalArgumentException("Expected String[2] but length was " + params.length);
        }

        Thread thread = Thread.currentThread();
        ClassLoader oldCL = thread.getContextClassLoader();
        Credentials credentials = new Credentials(params[0], params[1]);
        try {
            thread.setContextClassLoader(cl);
            //TODO consider using ContextManager for login and checking a permission against the ACC
            //to do e.g. deployments.
            LoginContext context = new LoginContext(configName, credentials);
            context.login();
            threadContext.set(context);
            Subject sub = context.getSubject();
            Set<GeronimoGroupPrincipal> pricipalsGroup = sub.getPrincipals(GeronimoGroupPrincipal.class);
            boolean isInAdminGroup = false;
            for (GeronimoGroupPrincipal principal : pricipalsGroup) {
                if (principal.getName().equals("admin")) {
                    isInAdminGroup = true;
                    break;
                 }
            }
            if(!isInAdminGroup){
                throw new LoginException("Only users in admin group are allowed");
            }
            return context.getSubject();
        } catch (LoginException e) {
            // do not propogate cause - we don't know what information is may contain
            throw new SecurityException("Invalid login");
        } finally {
            credentials.clear();
            thread.setContextClassLoader(oldCL);
        }
    }

    public void handleNotification(Notification notification, Object o) {
        if (notification instanceof JMXConnectionNotification) {
            JMXConnectionNotification cxNotification = (JMXConnectionNotification) notification;
            String type = cxNotification.getType();
            String connectionId = cxNotification.getConnectionId();
            if (JMXConnectionNotification.OPENED.equals(type)) {
                LoginContext context = threadContext.get();
                threadContext.set(null);
                contextMap.put(connectionId, context);
            } else {
                LoginContext context = contextMap.remove(connectionId);
                if (context != null) {
                    try {
                        context.logout();
                    } catch (LoginException e) {
                        //nothing we can do here...
                    }
                }
            }
        }
    }
}
