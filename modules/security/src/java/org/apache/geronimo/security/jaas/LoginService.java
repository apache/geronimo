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

package org.apache.geronimo.security.jaas;

import java.io.IOException;
import java.security.AccessController;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.SubjectId;
import org.apache.geronimo.security.realm.SecurityRealm;


/**
 * An MBean that maintains a list of security realms.
 *
 * @version $Revision: 1.6 $ $Date: 2004/06/02 05:33:04 $
 */
public class LoginService implements LoginServiceMBean, GBean {
    /**
     * The JMX name of the SecurityService.
     */
    public static final ObjectName LOGIN_SERVICE = JMXUtil.getObjectName("geronimo.security:type=LoginService");

    private final static Log log = LogFactory.getLog(LoginService.class);

    /**
     * Manages the thread that can used to schedule short
     * running tasks in the future.
     */
    protected final static ClockDaemon clockDaemon;
    private Map loginCache = new Hashtable();
    private long reclaimPeriod;

    private Collection realms = Collections.EMPTY_SET;
    private Collection loginModules = Collections.EMPTY_SET;
    private final static ClassLoader classLoader;

    private SecretKey key;
    private String algorithm;
    private String password;

    static {
        classLoader = (ClassLoader) AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });

        clockDaemon = new ClockDaemon();
        clockDaemon.setThreadFactory(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "LoginService login modules monitor");
                t.setDaemon(true);
                return t;
            }
        });
    }


    public long getReclaimPeriod() {
        return reclaimPeriod;
    }

    public void setReclaimPeriod(long reclaimPeriod) {
        this.reclaimPeriod = reclaimPeriod;
    }

    public Collection getRealms() throws GeronimoSecurityException {
        return realms;
    }


    public void setRealms(Collection realms) {
        this.realms = realms;
    }

    public Collection getLoginModules() throws GeronimoSecurityException {
        return loginModules;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SerializableACE getAppConfigurationEntry(String realmName) {

        for (Iterator iter = getRealms().iterator(); iter.hasNext();) {
            SecurityRealm securityRealm = (SecurityRealm) iter.next();

            if (realmName.equals(securityRealm.getRealmName())) {
                javax.security.auth.login.AppConfigurationEntry entry = securityRealm.getAppConfigurationEntry();

                HashMap options = new HashMap();

                options.put(LoginModuleConstants.REALM_NAME, realmName);
                options.put(LoginModuleConstants.MODULE, entry.getLoginModuleName());

                SerializableACE wrapper;

                if (securityRealm.isLoginModuleLocal()) {
                    wrapper = new SerializableACE("org.apache.geronimo.security.jaas.RemoteLoginModuleLocalWrapper",
                            SerializableACE.LoginModuleControlFlag.REQUIRED,
                            options);

                } else {
                    options.putAll(entry.getOptions());
                    wrapper = new SerializableACE("org.apache.geronimo.security.jaas.RemoteLoginModuleRemoteWrapper",
                            SerializableACE.LoginModuleControlFlag.REQUIRED,
                            options);
                }
                return wrapper;
            }
        }
        return null;
    }

    public LoginModuleId allocateLoginModule(String realmName) {
        LoginModuleCacheObject lm = null;

        synchronized (LoginService.class) {
            try {
                for (Iterator iter = getRealms().iterator(); iter.hasNext();) {
                    SecurityRealm securityRealm = (SecurityRealm) iter.next();

                    if (realmName.equals(securityRealm.getRealmName())) {
                        AppConfigurationEntry entry = securityRealm.getAppConfigurationEntry();

                        final String finalClass = entry.getLoginModuleName();

                        LoginModule module = (LoginModule) AccessController.doPrivileged(new java.security.PrivilegedExceptionAction() {
                            public Object run() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
                                return Class.forName(finalClass, true, classLoader).newInstance();
                            }
                        });
                        Subject subject = new Subject();
                        CallbackProxy callback = new CallbackProxy();
                        module.initialize(subject, callback, new HashMap(), entry.getOptions());

                        lm = allocateLoginModuleCacheObject(securityRealm.getMaxLoginModuleAge());
                        lm.setRealmName(realmName);
                        lm.setLoginModule(module);
                        lm.setSubject(subject);
                        lm.setCallbackHandler(callback);

                        log.trace("LoginModule [" + lm.getLoginModuleId() + "] created for realm " + realmName);

                        break;
                    }
                }
            } catch (Exception e) {
                return null;
            }
        }
        return lm.getLoginModuleId();
    }

    public void removeLoginModule(LoginModuleId loginModuleId) throws ExpiredLoginModuleException {
        LoginModuleCacheObject lm = (LoginModuleCacheObject) loginCache.get(loginModuleId);
        if (lm == null) throw new ExpiredLoginModuleException();

        lm.setDone(true);
        log.trace("LoginModule [" + lm.getLoginModuleId() + "] marked done");
    }

    public Collection getCallbacks(LoginModuleId loginModuleId) throws ExpiredLoginModuleException {
        LoginModuleCacheObject lm = (LoginModuleCacheObject) loginCache.get(loginModuleId);
        if (lm == null) throw new ExpiredLoginModuleException();

        LoginModule module = lm.getLoginModule();
        CallbackProxy callback = (CallbackProxy) lm.getCallbackHandler();

        try {
            module.login();
        } catch (LoginException e) {
        }
        try {
            module.abort();
        } catch (LoginException e) {
        }
        ArrayList callbacks = new ArrayList();
        for (int i = 0; i < callback.callbacks.length; i++) {
            callbacks.add(callback.callbacks[i]);
        }
        return callbacks;
    }

    /**
     * A intermediate callaback handler that is used to obtain the callbacks
     * that a login module will use and to fill in the reply that a remote
     * client has provided.
     */
    class CallbackProxy implements CallbackHandler {
        Callback[] callbacks;

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            if (this.callbacks == null) {
                this.callbacks = callbacks;
                throw new UnsupportedCallbackException(callbacks[0], "DO NOT PROCEED WITH THIS LOGIN");
            } else {
                assert this.callbacks.length == callbacks.length : "Callback lengths should not have changed";

                for (int i = 0; i < callbacks.length; i++) {
                    callbacks[i] = this.callbacks[i];
                }
            }
        }
    }

    public boolean login(LoginModuleId loginModuleId, Collection callbacks) throws LoginException {
        LoginModuleCacheObject lm = (LoginModuleCacheObject) loginCache.get(loginModuleId);
        if (lm == null) throw new ExpiredLoginModuleException();

        LoginModule module = lm.getLoginModule();

        CallbackProxy callback = (CallbackProxy) lm.getCallbackHandler();
        callback.callbacks = (Callback[]) callbacks.toArray(new Callback[]{});

        return module.login();
    }

    public boolean commit(LoginModuleId loginModuleId) throws LoginException {
        LoginModuleCacheObject lm = (LoginModuleCacheObject) loginCache.get(loginModuleId);
        if (lm == null) throw new ExpiredLoginModuleException();

        LoginModule module = lm.getLoginModule();
        if (!module.commit()) return false;

        Subject subject = lm.getSubject();
        RealmPrincipal principal;
        Set principals = new HashSet();
        Iterator iter = subject.getPrincipals().iterator();
        while (iter.hasNext()) {
            principal = new RealmPrincipal(lm.getRealmName(), (Principal) iter.next());
            principals.add(ContextManager.registerPrincipal(principal));
        }
        subject.getPrincipals().addAll(principals);

        ContextManager.registerSubject(subject);

        SubjectId id = ContextManager.getSubjectId(lm.getSubject());

        subject.getPrincipals().add(new IdentificationPrincipal(id));

        return true;
    }

    public boolean abort(LoginModuleId loginModuleId) throws LoginException {
        LoginModuleCacheObject lm = (LoginModuleCacheObject) loginCache.get(loginModuleId);
        if (lm == null) throw new ExpiredLoginModuleException();

        LoginModule module = lm.getLoginModule();

        return module.abort();
    }

    public boolean logout(LoginModuleId loginModuleId) throws LoginException {
        LoginModuleCacheObject lm = (LoginModuleCacheObject) loginCache.get(loginModuleId);
        if (lm == null) throw new ExpiredLoginModuleException();

        lm.getSubject().getPrincipals(RealmPrincipal.class).clear();

        LoginModule module = lm.getLoginModule();

        return module.logout();
    }

    public Subject retrieveSubject(LoginModuleId loginModuleId) throws LoginException {
        LoginModuleCacheObject lm = (LoginModuleCacheObject) loginCache.get(loginModuleId);
        if (lm == null) throw new ExpiredLoginModuleException();

        return lm.getSubject();
    }

    private byte[] hash(Long id) {
        long n = id.longValue();
        byte[] bytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (n);
            n >>>= 8;
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

    private static long nextLoginModuleId = System.currentTimeMillis();

    private LoginModuleCacheObject allocateLoginModuleCacheObject(long maxAge) {
        synchronized (loginCache) {
            Long id = new Long(nextLoginModuleId++);
            LoginModuleId loginModuleId = new LoginModuleId(id, hash(id));

            LoginModuleCacheObject lm = (LoginModuleCacheObject) loginCache.get(loginModuleId);
            if (lm == null) {
                lm = new LoginModuleCacheObject(loginModuleId);

                loginCache.put(loginModuleId, lm);

                LoginModuleCacheMonitor cm = new LoginModuleCacheMonitor(loginModuleId, lm, maxAge);
                cm.clockTicket = clockDaemon.executePeriodically(reclaimPeriod, cm, true);
            }
            return lm;
        }
    }

    /**
     * This class periodically checks one login module.
     */
    private class LoginModuleCacheMonitor implements Runnable {
        final LoginModuleId key;
        final LoginModuleCacheObject loginModule;
        Object clockTicket;
        final long maxAge;

        LoginModuleCacheMonitor(LoginModuleId key, LoginModuleCacheObject loginModule, long maxAge) {
            this.key = key;
            this.loginModule = loginModule;
            this.maxAge = maxAge;
        }

        public void run() {
            long currentTime = System.currentTimeMillis();
            if (loginModule.isDone() || (currentTime - loginModule.getCreated()) > maxAge) {
                log.trace("LoginModule [" + loginModule.getLoginModuleId() + "] reclaimed");

                ClockDaemon.cancel(clockTicket);
                loginCache.remove(key);
                ContextManager.unregisterSubject(loginModule.getSubject());
            }
        }
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        key = new SecretKeySpec(password.getBytes(), algorithm);

        /**
         * Simple test to make sure that the algorithm and key are fine.
         * This should stop the service from being started if there is a
         * problem.
         */
        Mac mac = Mac.getInstance(algorithm);
        mac.init(key);

        log.info("Login server has been started");
    }

    public void doStop() throws WaitingException, Exception {
        clockDaemon.shutDown();

        Iterator keys = loginCache.keySet().iterator();
        while (keys.hasNext()) {
            LoginModuleCacheObject loginModule = (LoginModuleCacheObject) loginCache.get(keys.next());

            log.trace("LoginModule [" + loginModule.getLoginModuleId() + "] reclaimed");

            ContextManager.unregisterSubject(loginModule.getSubject());
        }
        loginCache.clear();

        log.info("Login server has been stopped");
    }

    public void doFail() {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(LoginService.class);

        infoFactory.addOperation("getAppConfigurationEntry", new Class[]{String.class});
        infoFactory.addOperation("allocateLoginModule", new Class[]{String.class});
        infoFactory.addOperation("getCallbacks", new Class[]{LoginModuleId.class});
        infoFactory.addOperation("login", new Class[]{LoginModuleId.class, Collection.class});
        infoFactory.addOperation("commit", new Class[]{LoginModuleId.class});
        infoFactory.addOperation("abort", new Class[]{LoginModuleId.class});
        infoFactory.addOperation("logout", new Class[]{LoginModuleId.class});
        infoFactory.addOperation("retrieveSubject", new Class[]{LoginModuleId.class});

        infoFactory.addAttribute("ReclaimPeriod", long.class, true);
        infoFactory.addAttribute("Algorithm", String.class, true);
        infoFactory.addAttribute("Password", String.class, true);

        infoFactory.addReference("Realms", SecurityRealm.class);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
