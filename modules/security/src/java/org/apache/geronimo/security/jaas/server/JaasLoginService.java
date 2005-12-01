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
package org.apache.geronimo.security.jaas.server;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.SubjectId;
import org.apache.geronimo.security.jaas.LoginUtils;
import org.apache.geronimo.security.realm.SecurityRealm;


/**
 * The single point of contact for Geronimo JAAS realms.  Instead of attempting
 * to interact with JAAS realms directly, a client should either interact with
 * this service, or use a LoginModule implementation that interacts with this
 * service.
 *
 * @version $Rev$ $Date$
 */
public class JaasLoginService implements GBeanLifecycle, JaasLoginServiceMBean {
    public static final Log log = LogFactory.getLog(JaasLoginService.class);
    private final static int DEFAULT_EXPIRED_LOGIN_SCAN_INTERVAL = 300000; // 5 mins
    private final static int DEFAULT_MAX_LOGIN_DURATION = 1000 * 3600 * 24; // 1 day
    private final static ClockDaemon clockDaemon;
    private static long nextLoginModuleId = System.currentTimeMillis();
    private ReferenceCollection realms;
    private Object expiredLoginScanIdentifier;
    private final String objectName;
    private final SecretKey key;
    private final String algorithm;
    private final ClassLoader classLoader;
    private final Map activeLogins = new Hashtable();
    private int expiredLoginScanIntervalMillis = DEFAULT_EXPIRED_LOGIN_SCAN_INTERVAL;
    private int maxLoginDurationMillis = DEFAULT_MAX_LOGIN_DURATION;


    public JaasLoginService(String algorithm, String password, ClassLoader classLoader, String objectName) {
        this.classLoader = classLoader;
        this.algorithm = algorithm;
        key = new SecretKeySpec(password.getBytes(), algorithm);
        this.objectName = objectName;
    }

    public String getObjectName() {
        return objectName;
    }

    /**
     * GBean property
     */
    public Collection getRealms() throws GeronimoSecurityException {
        return realms;
    }

    /**
     * GBean property
     */
    public void setRealms(Collection realms) {
        this.realms = (ReferenceCollection) realms;
        //todo: add listener to drop logins when realm is removed
    }

    /**
     * GBean property
     */
    public int getMaxLoginDurationMillis() {
        return maxLoginDurationMillis;
    }

    /**
     * GBean property
     */
    public void setMaxLoginDurationMillis(int maxLoginDurationMillis) {
        if (maxLoginDurationMillis == 0) {
            maxLoginDurationMillis = DEFAULT_MAX_LOGIN_DURATION;
        }
        this.maxLoginDurationMillis = maxLoginDurationMillis;
    }

    /**
     * GBean property
     */
    public int getExpiredLoginScanIntervalMillis() {
        return expiredLoginScanIntervalMillis;
    }

    /**
     * GBean property
     */
    public void setExpiredLoginScanIntervalMillis(int expiredLoginScanIntervalMillis) {
        if (expiredLoginScanIntervalMillis == 0) {
            expiredLoginScanIntervalMillis = DEFAULT_EXPIRED_LOGIN_SCAN_INTERVAL;
        }
        this.expiredLoginScanIntervalMillis = expiredLoginScanIntervalMillis;
    }

    public void doStart() throws Exception {
        expiredLoginScanIdentifier = clockDaemon.executePeriodically(expiredLoginScanIntervalMillis, new ExpirationMonitor(), true);
    }

    public void doStop() throws Exception {
        ClockDaemon.cancel(expiredLoginScanIdentifier);
        //todo: shut down all logins
    }

    public void doFail() {
        //todo: shut down all logins
    }

    /**
     * Starts a new authentication process on behalf of an end user.  The
     * returned ID will identify that user throughout the user's interaction
     * with the server.  On the server side, that means maintaining the
     * Subject and Principals for the user.
     *
     * @return The client handle used as an argument for the rest of the
     *         methods in this class.
     */
    public JaasSessionId connectToRealm(String realmName) {
        SecurityRealm realm = null;
        realm = getRealm(realmName);
        if (realm == null) {
            throw new GeronimoSecurityException("No such realm (" + realmName + ")");
        } else {
            return initializeClient(realm);
        }
    }

    /**
     * Gets the login module configuration for the specified realm.  The
     * caller needs that in order to perform the authentication process.
     */
    public JaasLoginModuleConfiguration[] getLoginConfiguration(JaasSessionId sessionHandle) throws LoginException {
        JaasSecuritySession session = (JaasSecuritySession) activeLogins.get(sessionHandle);
        if (session == null) {
            throw new ExpiredLoginModuleException();
        }
        JaasLoginModuleConfiguration[] config = session.getModules();
        // strip out non-serializable configuration options
        JaasLoginModuleConfiguration[] result = new JaasLoginModuleConfiguration[config.length];
        for (int i = 0; i < config.length; i++) {
            result[i] = LoginUtils.getSerializableCopy(config[i]);
        }
        return result;
    }

    /**
     * Retrieves callbacks for a server side login module.  When the client
     * is going through the configured login modules, if a specific login
     * module is client-side, it will be handled directly.  If it is
     * server-side, the client gets the callbacks (using this method),
     * populates them, and sends them back to the server.
     */
    public Callback[] getServerLoginCallbacks(JaasSessionId sessionHandle, int loginModuleIndex) throws LoginException {
        JaasSecuritySession session = (JaasSecuritySession) activeLogins.get(sessionHandle);
        checkContext(session, loginModuleIndex);
        LoginModule module = session.getLoginModule(loginModuleIndex);

        session.getHandler().setExploring();
        try {
            module.initialize(session.getSubject(), session.getHandler(), new HashMap(), session.getOptions(loginModuleIndex));
        } catch (Exception e) {
            System.err.println("Failed to initialize module");
            e.printStackTrace();
        }
        try {
            module.login();
        } catch (LoginException e) {
        }
        try {
            module.abort();
        } catch (LoginException e) {
        }
        return session.getHandler().finalizeCallbackList();
    }

    /**
     * Returns populated callbacks for a server side login module.  When the
     * client is going through the configured login modules, if a specific
     * login module is client-side, it will be handled directly.  If it is
     * server-side, the client gets the callbacks, populates them, and sends
     * them back to the server (using this method).
     */
    public boolean performLogin(JaasSessionId sessionHandle, int loginModuleIndex, Callback[] results) throws LoginException {
        JaasSecuritySession session = (JaasSecuritySession) activeLogins.get(sessionHandle);
        checkContext(session, loginModuleIndex);
        try {
            session.getHandler().setClientResponse(results);
        } catch (IllegalArgumentException iae) {
            throw new LoginException(iae.toString());
        }
        return session.getLoginModule(loginModuleIndex).login();
    }

    /**
     * Indicates that the overall login succeeded, and some principals were
     * generated by a client-side login module.  This method needs to be called
     * once for each client-side login module, to specify Principals for each
     * module.
     */
    public boolean performCommit(JaasSessionId sessionHandle, int loginModuleIndex) throws LoginException {
        JaasSecuritySession session = (JaasSecuritySession) activeLogins.get(sessionHandle);
        checkContext(session, loginModuleIndex);
        return session.getLoginModule(loginModuleIndex).commit();
    }

    /**
     * Indicates that the overall login succeeded.  All login modules that were
     * touched should have been logged in and committed before calling this.
     */
    public Principal loginSucceeded(JaasSessionId sessionHandle) throws LoginException {
        JaasSecuritySession session = (JaasSecuritySession) activeLogins.get(sessionHandle);
        if (session == null) {
            throw new ExpiredLoginModuleException();
        }

        Subject subject = session.getSubject();
        ContextManager.registerSubject(subject);
        SubjectId id = ContextManager.getSubjectId(subject);
        IdentificationPrincipal principal = new IdentificationPrincipal(id);
        subject.getPrincipals().add(principal);
        return principal;
    }

    /**
     * Indicates that the overall login failed, and the server should release
     * any resources associated with the user ID.
     */
    public void loginFailed(JaasSessionId sessionHandle) {
        activeLogins.remove(sessionHandle);
    }

    /**
     * Indicates that the client has logged out, and the server should release
     * any resources associated with the user ID.
     */
    public void logout(JaasSessionId sessionHandle) throws LoginException {
        JaasSecuritySession session = (JaasSecuritySession) activeLogins.get(sessionHandle);
        if (session == null) {
            throw new ExpiredLoginModuleException();
        }
        ContextManager.unregisterSubject(session.getSubject());
        activeLogins.remove(sessionHandle);
        for (int i = 0; i < session.getModules().length; i++) {
            if (session.isServerSide(i)) {
                session.getLoginModule(i).logout();
            }
        }
    }

    /**
     * Syncs the shared state that's on thye client with the shared state that
     * is on the server.
     *
     * @param sessionHandle
     * @param sharedState   the shared state that is on the client
     * @return the sync'd shared state that is on the server
     */
    public Map syncShareState(JaasSessionId sessionHandle, Map sharedState) throws LoginException {
        JaasSecuritySession session = (JaasSecuritySession) activeLogins.get(sessionHandle);
        if (session == null) {
            throw new ExpiredLoginModuleException();
        }
        session.getSharedContext().putAll(sharedState);
        return LoginUtils.getSerializableCopy(session.getSharedContext());
    }

    /**
     * Syncs the set of principals that are on the client with the set of principals that
     * are on the server.
     *
     * @param sessionHandle
     * @param principals    the set of principals that are on the client side
     * @return the sync'd set of principals that are on the server
     */
    public Set syncPrincipals(JaasSessionId sessionHandle, Set principals) throws LoginException {
        JaasSecuritySession session = (JaasSecuritySession) activeLogins.get(sessionHandle);
        if (session == null) {
            throw new ExpiredLoginModuleException();
        }
        session.getSubject().getPrincipals().addAll(principals);

        return LoginUtils.getSerializableCopy(session.getSubject().getPrincipals());
    }

    private void checkContext(JaasSecuritySession session, int loginModuleIndex) throws LoginException {
        if (session == null) {
            throw new ExpiredLoginModuleException();
        }
        if (loginModuleIndex < 0 || loginModuleIndex >= session.getModules().length || !session.isServerSide(loginModuleIndex)) {
            throw new LoginException("Invalid login module specified");
        }
    }

    /**
     * Prepares a new security context for a new client.  Each client uses a
     * unique security context to sture their authentication progress,
     * principals, etc.
     *
     * @param realm The realm the client is authenticating to
     */
    private JaasSessionId initializeClient(SecurityRealm realm) {
        long id;
        synchronized (JaasLoginService.class) {
            id = ++nextLoginModuleId;
        }
        JaasSessionId sessionHandle = new JaasSessionId(id, hash(id));
        JaasLoginModuleConfiguration[] modules = realm.getAppConfigurationEntries();
        JaasSecuritySession session = new JaasSecuritySession(realm.getRealmName(), modules, new HashMap(), classLoader);
        activeLogins.put(sessionHandle, session);
        return sessionHandle;
    }

    private SecurityRealm getRealm(String realmName) {
        for (Iterator it = realms.iterator(); it.hasNext();) {
            SecurityRealm test = (SecurityRealm) it.next();
            if (test.getRealmName().equals(realmName)) {
                return test;
            }
        }
        return null;
    }

    /**
     * Hashes a unique ID.  The client keeps an object around with the ID and
     * the hash of the ID.  That way it's not so easy to forge an ID and steal
     * someone else's account.
     */
    private byte[] hash(long id) {
        byte[] bytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (id);
            id >>>= 8;
        }

        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(key);
            mac.update(bytes);

            return mac.doFinal();
        } catch (NoSuchAlgorithmException e) {
        } catch (InvalidKeyException e) {
        }
        assert false : "Should never have reached here";
        return null;
    }


    // This stuff takes care of whacking old logins
    static {
        clockDaemon = new ClockDaemon();
        clockDaemon.setThreadFactory(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "LoginService login modules monitor");
                t.setDaemon(true);
                return t;
            }
        });
    }

    private class ExpirationMonitor implements Runnable { //todo: different timeouts per realm?

        public void run() {
            long now = System.currentTimeMillis();
            List list = new LinkedList();
            synchronized (activeLogins) {
                for (Iterator it = activeLogins.keySet().iterator(); it.hasNext();) {
                    JaasSessionId id = (JaasSessionId) it.next();
                    JaasSecuritySession session = (JaasSecuritySession) activeLogins.get(id);
                    int age = (int) (now - session.getCreated());
                    if (session.isDone() || age > maxLoginDurationMillis) {
                        list.add(session);
                        session.setDone(true);
                        it.remove();
                    }
                }
            }
            for (Iterator it = list.iterator(); it.hasNext();) {
                JaasSecuritySession session = (JaasSecuritySession) it.next();
                ContextManager.unregisterSubject(session.getSubject());
            }
        }
    }


    // This stuff takes care of making this object into a GBean
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(JaasLoginService.class, "JaasLoginService");

        infoFactory.addAttribute("algorithm", String.class, true);
        infoFactory.addAttribute("password", String.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("maxLoginDurationMillis", int.class, true);
        infoFactory.addAttribute("expiredLoginScanIntervalMillis", int.class, true);
        infoFactory.addAttribute("objectName", String.class, false);

        infoFactory.addOperation("connectToRealm", new Class[]{String.class});
        infoFactory.addOperation("getLoginConfiguration", new Class[]{JaasSessionId.class});
        infoFactory.addOperation("getServerLoginCallbacks", new Class[]{JaasSessionId.class, int.class});
        infoFactory.addOperation("performLogin", new Class[]{JaasSessionId.class, int.class, Callback[].class});
        infoFactory.addOperation("performCommit", new Class[]{JaasSessionId.class, int.class});
        infoFactory.addOperation("loginSucceeded", new Class[]{JaasSessionId.class});
        infoFactory.addOperation("loginFailed", new Class[]{JaasSessionId.class});
        infoFactory.addOperation("logout", new Class[]{JaasSessionId.class});
        infoFactory.addOperation("syncShareState", new Class[]{JaasSessionId.class, Map.class});
        infoFactory.addOperation("syncPrincipals", new Class[]{JaasSessionId.class, Set.class});

        infoFactory.addReference("Realms", SecurityRealm.class, NameFactory.SECURITY_REALM);

        infoFactory.setConstructor(new String[]{"algorithm", "password", "classLoader", "objectName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
