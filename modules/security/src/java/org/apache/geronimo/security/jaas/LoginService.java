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
import java.util.List;
import java.util.Arrays;
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
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
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
 * @version $Rev$ $Date$
 */
public class LoginService implements LoginServiceMBean, GBeanLifecycle {
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

    public SerializableACE[] getAppConfigurationEntries(String realmName) {

        for (Iterator iter = getRealms().iterator(); iter.hasNext();) {
            SecurityRealm securityRealm = (SecurityRealm) iter.next();

            if (realmName.equals(securityRealm.getRealmName())) {
                javax.security.auth.login.AppConfigurationEntry[] entries = securityRealm.getAppConfigurationEntries();
                SerializableACE[] results = new SerializableACE[entries.length];
                for(int i = 0; i < entries.length; i++) {
                    AppConfigurationEntry entry = entries[i];

                    HashMap options = new HashMap();

                    options.put(LoginModuleConstants.REALM_NAME, realmName);
                    options.put(LoginModuleConstants.MODULE, entry.getLoginModuleName());

                    SerializableACE wrapper;

                    if (securityRealm.isLoginModuleLocal()) {
                        wrapper = new SerializableACE("org.apache.geronimo.security.jaas.RemoteLoginModuleLocalWrapper",
                                LoginModuleControlFlag.REQUIRED,
                                options);

                    } else {
                        options.putAll(entry.getOptions());
                        wrapper = new SerializableACE("org.apache.geronimo.security.jaas.RemoteLoginModuleRemoteWrapper",
                                LoginModuleControlFlag.REQUIRED,
                                options);
                    }
                    results[i] = wrapper;
                }
                return results;
            }
        }
        return null;
    }

    public LoginModuleId allocateLoginModules(String realmName) {
        LoginModuleCacheObject lm = null;

        synchronized (LoginService.class) {
            try {
                for (Iterator iter = getRealms().iterator(); iter.hasNext();) {
                    SecurityRealm securityRealm = (SecurityRealm) iter.next();

                    if (realmName.equals(securityRealm.getRealmName())) {
                        AppConfigurationEntry[] entries = securityRealm.getAppConfigurationEntries();
                        Subject subject = new Subject();
                        CallbackProxy callback = new CallbackProxy();
                        LoginModuleConfiguration[] modules = new LoginModuleConfiguration[entries.length];
                        for(int i = 0; i < entries.length; i++) {
                            AppConfigurationEntry entry = entries[i];

                            final String finalClass = entry.getLoginModuleName();

                            LoginModule module = (LoginModule) AccessController.doPrivileged(new java.security.PrivilegedExceptionAction() {
                                public Object run() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
                                    return Class.forName(finalClass, true, classLoader).newInstance();
                                }
                            });
                            module.initialize(subject, callback, new HashMap(), entry.getOptions());
                            modules[i] = new LoginModuleConfiguration(module, LoginModuleControlFlag.getInstance(entry.getControlFlag()));

                        }
                        lm = allocateLoginModuleCacheObject(securityRealm.getMaxLoginModuleAge());
                        lm.setRealmName(realmName);
                        lm.setLoginModules(modules);
                        lm.setSubject(subject);
                        lm.setCallbackHandler(callback);
                        log.trace("LoginModule [" + lm.getLoginModuleId() + "] created for realm " + realmName);

                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return lm.getLoginModuleId();
    }

    public void removeLoginModules(LoginModuleId loginModuleId) throws ExpiredLoginModuleException {
        LoginModuleCacheObject lm = (LoginModuleCacheObject) loginCache.get(loginModuleId);
        if (lm == null) throw new ExpiredLoginModuleException();

        lm.setDone(true);
        log.trace("LoginModule [" + lm.getLoginModuleId() + "] marked done");
    }

    //todo: this is pretty cheesy
    public Collection getCallbacks(LoginModuleId loginModuleId) throws ExpiredLoginModuleException {
        LoginModuleCacheObject lm = (LoginModuleCacheObject) loginCache.get(loginModuleId);
        if (lm == null) throw new ExpiredLoginModuleException();

        LoginModuleConfiguration[] modules = lm.getLoginModules();

        CallbackProxy proxy = (CallbackProxy) lm.getCallbackHandler();
        proxy.setExploring();
        for(int i = 0; i < modules.length; i++) {
            LoginModuleConfiguration module = modules[i];
            try {
                module.getModule().login();
            } catch (LoginException e) {
            }
            try {
                module.getModule().abort();
            } catch (LoginException e) {
            }
        }
        return proxy.finalizeCallbackList();
    }

    /**
     * A intermediate callaback handler that is used to obtain the callbacks
     * that a login module will use and to fill in the reply that a remote
     * client has provided.
     */
    static class CallbackProxy implements CallbackHandler {
        private List callbacks = new ArrayList();
        private boolean exploring = true;

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            if (exploring) {
                this.callbacks.addAll(Arrays.asList(callbacks));
                throw new UnsupportedCallbackException(callbacks[0], "DO NOT PROCEED WITH THIS LOGIN");
            } else {
                assert this.callbacks.size() == callbacks.length : "Callback lengths should not have changed";

                for (int i = 0; i < callbacks.length; i++) {
                    callbacks[i] = (Callback) this.callbacks.get(i);
                }
            }
        }

        public void setExploring() {
            exploring = true;
            callbacks.clear();
        }

        public List finalizeCallbackList() {
            exploring = false;
            return callbacks;
        }
    }

    public boolean login(LoginModuleId loginModuleId, Collection callbacks) throws LoginException {
        LoginModuleCacheObject lm = (LoginModuleCacheObject) loginCache.get(loginModuleId);
        if (lm == null) throw new ExpiredLoginModuleException();
        LoginModuleConfiguration[] modules = lm.getLoginModules();
        if(modules.length == 0) {
            throw new LoginException("No login modules configured for realm "+lm.getRealmName());
        }
        CallbackProxy callback = (CallbackProxy) lm.getCallbackHandler();
        callback.callbacks = new ArrayList(callbacks);
        return LoginUtils.computeLogin(modules);
    }

    public boolean commit(LoginModuleId loginModuleId) throws LoginException {
        LoginModuleCacheObject lm = (LoginModuleCacheObject) loginCache.get(loginModuleId);
        if (lm == null) throw new ExpiredLoginModuleException();

        LoginModuleConfiguration[] modules = lm.getLoginModules();
        for(int i = 0; i < modules.length; i++) {
            LoginModuleConfiguration configuration = modules[i];
            configuration.getModule().commit();
        }

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

        LoginModuleConfiguration[] modules = lm.getLoginModules();
        for(int i = 0; i < modules.length; i++) {
            LoginModuleConfiguration configuration = modules[i];
            configuration.getModule().abort();
        }

        return true;
    }

    public boolean logout(LoginModuleId loginModuleId) throws LoginException {
        LoginModuleCacheObject lm = (LoginModuleCacheObject) loginCache.get(loginModuleId);
        if (lm == null) throw new ExpiredLoginModuleException();

        lm.getSubject().getPrincipals(RealmPrincipal.class).clear();
        lm.getSubject().getPrincipals(IdentificationPrincipal.class).clear();
        LoginModuleConfiguration[] modules = lm.getLoginModules();
        for(int i = 0; i < modules.length; i++) {
            LoginModuleConfiguration configuration = modules[i];
            configuration.getModule().logout();
        }
        return true;
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
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(LoginService.class);

        infoFactory.addOperation("getAppConfigurationEntries", new Class[]{String.class});
        infoFactory.addOperation("allocateLoginModules", new Class[]{String.class});
        infoFactory.addOperation("getCallbacks", new Class[]{LoginModuleId.class});
        infoFactory.addOperation("login", new Class[]{LoginModuleId.class, Collection.class});
        infoFactory.addOperation("commit", new Class[]{LoginModuleId.class});
        infoFactory.addOperation("abort", new Class[]{LoginModuleId.class});
        infoFactory.addOperation("logout", new Class[]{LoginModuleId.class});
        infoFactory.addOperation("retrieveSubject", new Class[]{LoginModuleId.class});

        infoFactory.addAttribute("reclaimPeriod", long.class, true);
        infoFactory.addAttribute("algorithm", String.class, true);
        infoFactory.addAttribute("password", String.class, true);

        infoFactory.addReference("Realms", SecurityRealm.class);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
