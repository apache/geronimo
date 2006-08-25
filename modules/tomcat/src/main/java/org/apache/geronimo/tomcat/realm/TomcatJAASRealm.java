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
package org.apache.geronimo.tomcat.realm;

import org.apache.catalina.realm.JAASCallbackHandler;
import org.apache.catalina.realm.JAASRealm;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.tomcat.JAASTomcatPrincipal;

import javax.security.auth.Subject;
import javax.security.auth.login.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @version $Rev$ $Date$
 */
public class TomcatJAASRealm extends JAASRealm implements Cloneable {
    private static final Log log = LogFactory.getLog(TomcatJAASRealm.class);

    private static final String DEFAULT_NAME = "tomcat";

    /**
     * Descriptive information about this <code>Realm</code> implementation.
     */
    protected static final String info = "org.apache.geronimo.tomcat.realm.TomcatJAASRealm/1.0";

    /**
     * Descriptive information about this <code>Realm</code> implementation.
     */
    protected static final String name = "TomcatJAASRealm";

    public TomcatJAASRealm() {
        super();
    }


    /**
     * Return the <code>Principal</code> associated with the specified
     * username and credentials, if there is one; otherwise return
     * <code>null</code>.
     * <p/>
     * If there are any errors with the JDBC connection, executing the query or
     * anything we return null (don't authenticate). This event is also logged,
     * and the connection will be closed so that a subsequent request will
     * automatically re-open it.
     *
     * @param username    Username of the <code>Principal</code> to look up
     * @param credentials Password or other credentials to use in authenticating this
     *                    username
     */
    public Principal authenticate(String username, String credentials) {

        // Establish a LoginContext to use for authentication
        try {
            LoginContext loginContext = null;
            if (appName == null)
                appName = DEFAULT_NAME;

            if (log.isDebugEnabled())
                log.debug(sm.getString("jaasRealm.beginLogin", username, appName));

            // What if the LoginModule is in the container class loader ?
            ClassLoader ocl = null;

            if (isUseContextClassLoader()) {
                ocl = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            }

            try {
                loginContext = new LoginContext(appName, new JAASCallbackHandler(this, username, credentials));
            } catch (Throwable e) {
                log.error(sm.getString("jaasRealm.unexpectedError"), e);
                return (null);
            } finally {
                if (isUseContextClassLoader()) {
                    Thread.currentThread().setContextClassLoader(ocl);
                }
            }

            if (log.isDebugEnabled())
                log.debug("Login context created " + username);

            // Negotiate a login via this LoginContext
            Subject subject = null;
            try {
                loginContext.login();
                Subject tempSubject = loginContext.getSubject();
                if (tempSubject == null) {
                    if (log.isDebugEnabled())
                        log.debug(sm.getString("jaasRealm.failedLogin", username));
                    return (null);
                }

                subject = ContextManager.getServerSideSubject(tempSubject);
                if (subject == null) {
                    if (log.isDebugEnabled())
                        log.debug(sm.getString("jaasRealm.failedLogin", username));
                    return (null);
                }

            } catch (AccountExpiredException e) {
                if (log.isDebugEnabled())
                    log.debug(sm.getString("jaasRealm.accountExpired", username));
                return (null);
            } catch (CredentialExpiredException e) {
                if (log.isDebugEnabled())
                    log.debug(sm.getString("jaasRealm.credentialExpired", username));
                return (null);
            } catch (FailedLoginException e) {
                if (log.isDebugEnabled())
                    log.debug(sm.getString("jaasRealm.failedLogin", username));
                return (null);
            } catch (LoginException e) {
                log.warn(sm.getString("jaasRealm.loginException", username), e);
                return (null);
            } catch (Throwable e) {
                log.error(sm.getString("jaasRealm.unexpectedError"), e);
                return (null);
            }

            if (log.isDebugEnabled())
                log.debug(sm.getString("jaasRealm.loginContextCreated", username));

            // Return the appropriate Principal for this authenticated Subject
            Principal principal = createPrincipal(username, subject);
            if (principal == null) {
                log.debug(sm.getString("jaasRealm.authenticateFailure", username));
                return (null);
            }
            if (log.isDebugEnabled()) {
                log.debug(sm.getString("jaasRealm.authenticateSuccess", username));
            }

            return (principal);

        } catch (Throwable t) {
            log.error("error ", t);
            return null;
        }
    }

    protected Principal createPrincipal(String username, Subject subject) {
        // Prepare to scan the Principals for this Subject
        String password = null; // Will not be carried forward

        List roles = new ArrayList();
        Principal userPrincipal = null;

        // Scan the Principals for this Subject
        Iterator principals = subject.getPrincipals().iterator();
        while (principals.hasNext()) {
            Principal principal = (Principal) principals.next();

            String principalClass = principal.getClass().getName();

            if( log.isDebugEnabled() ) {
                log.debug(sm.getString("jaasRealm.checkPrincipal", principal, principalClass));
            }

            if (userPrincipal == null && userClasses.contains(principalClass)) {
                userPrincipal = principal;
                if( log.isDebugEnabled() ) {
                    log.debug(sm.getString("jaasRealm.userPrincipalSuccess", principal.getName()));
                }
            }

            if (roleClasses.contains(principalClass)) {
                roles.add(principal.getName());
                if( log.isDebugEnabled() ) {
                    log.debug(sm.getString("jaasRealm.rolePrincipalAdd", principal.getName()));
                }
            }
        }

        // Print failure message if needed
        if (userPrincipal == null) {
            if (log.isDebugEnabled()) {
                log.debug(sm.getString("jaasRealm.userPrincipalFailure"));
                log.debug(sm.getString("jaasRealm.rolePrincipalFailure"));
            }
        } else {
            if (roles.size() == 0) {
                if (log.isDebugEnabled()) {
                    log.debug(sm.getString("jaasRealm.rolePrincipalFailure"));
                }
            }
        }

        JAASTomcatPrincipal jaasPrincipal = new JAASTomcatPrincipal(username);
        jaasPrincipal.setSubject(subject);
        jaasPrincipal.setRoles(roles);

        // Return the resulting Principal for our authenticated user
        return jaasPrincipal;
    }


    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }


    public boolean hasRole(Principal principal, String role) {

        if ((principal == null) || (role == null) ||
            !(principal instanceof JAASTomcatPrincipal))
            return (false);

        JAASTomcatPrincipal jtp = (JAASTomcatPrincipal) principal;
        if (jtp.getRoles().contains(role))
            return true;

        return false;
    }

}
