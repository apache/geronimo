/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.security.jaas;

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

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;
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
 * @version $Revision: 1.3 $ $Date: 2004/02/18 03:54:21 $
 */
public class LoginService implements LoginServiceMBean, GBean {

    private static final GBeanInfo GBEAN_INFO;

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

    private Kernel kernel;
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

    public Kernel getKernel() {
        return kernel;
    }

    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
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

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(LoginService.class.getName());
        infoFactory.addOperation(new GOperationInfo("getAppConfigurationEntry", new String[]{String.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("allocateLoginModule", new String[]{String.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("getCallbacks", new String[]{LoginModuleId.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("login", new String[]{LoginModuleId.class.getName(), Collection.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("commit", new String[]{LoginModuleId.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("abort", new String[]{LoginModuleId.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("logout", new String[]{LoginModuleId.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("retrieveSubject", new String[]{LoginModuleId.class.getName()}));
        infoFactory.addAttribute(new GAttributeInfo("Kernel", true));
        infoFactory.addAttribute(new GAttributeInfo("ReclaimPeriod", true));
        infoFactory.addAttribute(new GAttributeInfo("Algorithm", true));
        infoFactory.addAttribute(new GAttributeInfo("Password", true));
        infoFactory.addReference(new GReferenceInfo("Realms", SecurityRealm.class.getName()));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
