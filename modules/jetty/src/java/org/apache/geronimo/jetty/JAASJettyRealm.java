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
package org.apache.geronimo.jetty;

import java.security.Principal;
import java.util.HashMap;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.UserRealm;
import org.mortbay.jaas.callback.DefaultCallbackHandler;
import org.mortbay.util.LogSupport;


/**
 * @version $Revision: 1.2 $ $Date: 2004/06/02 05:33:03 $
 */
public class JAASJettyRealm implements UserRealm, GBean {

    private static Log log = LogFactory.getLog(JAASJettyRealm.class);

    protected final JettyContainer container;
    protected String realmName;
    protected String loginModuleName;
    protected HashMap userMap = new HashMap();

    public JAASJettyRealm(JettyContainer container) {
        this.container = container;
    }

    public String getName() {
        return realmName;
    }

    public void setName(String name) {
        realmName = name;
    }

    public void setLoginModuleName(String name) {
        loginModuleName = name;
    }

    public Principal getPrincipal(String username) {
        return (Principal) userMap.get(username);
    }

    public Principal authenticate(String username,
            Object credentials,
            HttpRequest request) {
        try {
            JAASJettyPrincipal userPrincipal = (JAASJettyPrincipal) userMap.get(username);

            //user has been previously authenticated, but
            //re-authentication has been requested, so remove them
            if (userPrincipal != null) {
                userMap.remove(username);
            }


            DefaultCallbackHandler callbackHandler = new DefaultCallbackHandler();

            callbackHandler.setUserName(username);
            callbackHandler.setCredential(credentials);

            //set up the login context
            LoginContext loginContext = new LoginContext(loginModuleName,
                    callbackHandler);

            loginContext.login();

            //login success
            userPrincipal = new JAASJettyPrincipal(username);
            userPrincipal.setSubject(loginContext.getSubject());

            userMap.put(username, userPrincipal);

            return userPrincipal;
        } catch (LoginException e) {
            log.warn(e);
            return null;
        }
    }

    public boolean reauthenticate(Principal user) {
        // TODO This is not correct if auth can expire! We need to
        // get the user out of the cache
        return (userMap.get(user.getName()) != null);
    }

    public boolean isUserInRole(Principal user, String role) {
        //TODO
        return true;
    }

    public void disassociate(Principal user) {
        //TODO
    }

    public Principal pushRole(Principal user, String role) {
        //TODO
        return user;
    }

    public Principal popRole(Principal user) {
        //TODO
        return user;
    }

    public void logout(Principal user) {
        log.warn(LogSupport.NOT_IMPLEMENTED);
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        container.addRealm(this);
        log.info("JAAS Jetty Realm - " + realmName + " - started");
    }

    public void doStop() throws WaitingException {
        container.removeRealm(this);
        log.info("JAAS Jetty Realm - " + realmName + " - stopped");
    }

    public void doFail() {
        container.removeRealm(this);
        log.info("JAAS Jetty Realm - " + realmName + " - failed");
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Jetty Realm", JAASJettyRealm.class.getName());
        infoFactory.setConstructor(new String[]{"JettyContainer"});
        infoFactory.addReference("JettyContainer", JettyContainer.class);
        infoFactory.addAttribute("Name", String.class, true);
        infoFactory.addAttribute("LoginModuleName", String.class, true);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
