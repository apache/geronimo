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

package org.apache.geronimo.security;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.ProviderException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.Configuration;
import javax.security.jacc.PolicyContext;

import org.apache.geronimo.security.realm.providers.GeronimoCallerPrincipal;


/**
 * @version $Rev$ $Date$
 */
public class ContextManager {

    private static final ThreadLocal<Callers> callers = new ThreadLocal<Callers>();
    private static final ThreadLocal<ThreadData> threadData = new ThreadLocal<ThreadData>() {
        @Override
        protected ThreadData initialValue() {
            ThreadData threadData = new ThreadData();
            PolicyContext.setHandlerData(threadData);
            return threadData;
        }
    };
    private static Map<Subject, Context> subjectContexts = new IdentityHashMap<Subject, Context>();
    private static Map<SubjectId, Subject> subjectIds =  new ConcurrentHashMap<SubjectId, Subject>();
    private static long nextSubjectId = System.currentTimeMillis();

    private static SecretKey key;
    private static String algorithm;
    private static String password;

    public static final GeronimoSecurityPermission GET_CONTEXT = new GeronimoSecurityPermission("getContext");
    public static final GeronimoSecurityPermission SET_CONTEXT = new GeronimoSecurityPermission("setContext");

    static {
        password = "secret";
        ContextManager.setAlgorithm("HmacSHA1");
    }
    public final static Subject EMPTY = new Subject();
    static {
        EMPTY.getPrincipals().add(new Principal() {
            public String getName() {
                return "";
            }
        });
        EMPTY.setReadOnly();
        registerSubject(EMPTY);
    }

    /**
     *
     * @param realm
     * @param callbackHandler
     * @param configuration login Configuration to use, or null for the GeronimoLoginConfiguration gbean instance
     * @return
     * @throws LoginException
     */
    public static LoginContext login(String realm, CallbackHandler callbackHandler, Configuration configuration) throws LoginException {
        Subject subject = new Subject();
        LoginContext loginContext = new LoginContext(realm, subject, callbackHandler, configuration);
        loginContext.login();
        SubjectId id = ContextManager.registerSubject(subject);
        IdentificationPrincipal principal = new IdentificationPrincipal(id);
        subject.getPrincipals().add(principal);
        return loginContext;
    }

    /**
     * @deprecated use the method with Configuration
     * @param realm
     * @param callbackHandler
     * @return
     * @throws LoginException
     */
    public static LoginContext login(String realm, CallbackHandler callbackHandler) throws LoginException {
        return login(realm, callbackHandler, null);
    }

    public static LoginContext login(Subject subject, String realm, CallbackHandler callbackHandler, Configuration configuration) throws LoginException {
        LoginContext loginContext = new LoginContext(realm, subject, callbackHandler, configuration);
        loginContext.login();
        return loginContext;
    }

    /**
     * @deprecated use the method with Configuration
     * @param subject
     * @param realm
     * @param callbackHandler
     * @return
     * @throws LoginException
     */
    public static LoginContext login(Subject subject, String realm, CallbackHandler callbackHandler) throws LoginException {
        return login(subject, realm, callbackHandler, null);
    }

    public static void logout(LoginContext loginContext) throws LoginException {
        Subject subject = loginContext.getSubject();
        ContextManager.unregisterSubject(subject);
        loginContext.logout();
    }

    public static void setCallers(Subject currentCaller, Subject nextCaller) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);
        assert currentCaller != null;
        assert nextCaller != null;
        Callers newCallers = new Callers(currentCaller, nextCaller);
        callers.set(newCallers);
    }

    public static void clearCallers() {
        callers.set(null);
    }

    public static Callers getCallers() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);
        return callers.get();
    }

    public static Callers setNextCaller(Subject nextCaller) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);
        assert nextCaller != null;
        Callers oldCallers = callers.get();
        assert oldCallers != null;
        Callers newCallers = new Callers(oldCallers.getNextCaller(), nextCaller);
        callers.set(newCallers);
        return oldCallers;
    }

    /**
     * Pusth the run-as identity as the next identity.  If the run-as identity is not specified,
     * push the current identity as the next identity.  Return the previous pair of current identity, next identity.
     * @param nextCaller next run-as identity or null
     * @return existing pair of (current identity, next identity)
     */
    public static Callers pushNextCaller(Subject nextCaller) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);
        Callers oldCallers = callers.get();
        Subject oldNextCaller = oldCallers == null? null: oldCallers.getNextCaller();
        Subject newNextCaller = (nextCaller == null || nextCaller == EMPTY)? oldNextCaller : nextCaller;
        Callers newCallers = new Callers(oldNextCaller, newNextCaller);
        callers.set(newCallers);
        return oldCallers;
    }

    public static void popCallers(Callers oldCallers) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);
        callers.set(oldCallers);
    }

    public static Subject getCurrentCaller() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        Callers callers = ContextManager.callers.get();
        return callers == null? null: callers.getCurrentCaller();
    }

    public static Subject getNextCaller() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        Callers callers = ContextManager.callers.get();
        return callers == null? null: callers.getNextCaller();
    }

    public static AccessControlContext getCurrentContext() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        Callers threadLocalCallers = callers.get();
        assert threadLocalCallers != null : "No current callers";
        Subject currentSubject = threadLocalCallers.getCurrentCaller();
        assert currentSubject != null : "No current caller";
        Context context = subjectContexts.get(currentSubject);

        assert context != null : "No registered context";

        return context.getContext();
    }

    public static Principal getCurrentPrincipal(Subject callerSubject) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        if (callerSubject == null) {
            return EMPTY.getPrincipals().iterator().next();
        }
        Context context = subjectContexts.get(callerSubject);

        assert context != null : "No registered context";

        return context.getPrincipal();
    }

    public static SubjectId getCurrentId() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        Callers threadLocalCallers = callers.get();
        assert threadLocalCallers != null : "No current callers";
        Subject currentSubject = threadLocalCallers.getCurrentCaller();
        assert currentSubject != null : "No current caller";
        Context context = subjectContexts.get(currentSubject);

        assert context != null : "No registered context";

        return context.getId();
    }

    public static SubjectId getSubjectId(Subject subject) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        Context context = subjectContexts.get(subject);

        return (context != null ? context.getId() : null);
    }

    public static Subject getRegisteredSubject(SubjectId id) {
        return subjectIds.get(id);
    }

    public static synchronized SubjectId registerSubject(Subject subject) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);

        if (subject == null) throw new IllegalArgumentException("Subject must not be null");

        AccessControlContext acc = (AccessControlContext) Subject.doAsPrivileged(subject, new PrivilegedAction() {
            public Object run() {
                return AccessController.getContext();
            }
        }, null);

        Set<? extends Principal> principals = subject.getPrincipals(GeronimoCallerPrincipal.class);
        Principal principal = null;
        if (!principals.isEmpty()) {
            principal = principals.iterator().next();
        } else if (!(principals = subject.getPrincipals(PrimaryRealmPrincipal.class)).isEmpty()) {
            principal = principals.iterator().next();
        } else if (!(principals = subject.getPrincipals(RealmPrincipal.class)).isEmpty()) {
            principal = principals.iterator().next();
        } else if (!(principals = subject.getPrincipals()).isEmpty()) {
            principal = principals.iterator().next();
        }
        Long id = nextSubjectId++;
        SubjectId subjectId;
        try {
            subjectId = new SubjectId(id, hash(id));
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException("No such algorithm: " + algorithm + ".  This can be caused by a misconfigured java.ext.dirs, JAVA_HOME or JRE_HOME environment variable");
        } catch (InvalidKeyException e) {
            throw new ProviderException("Invalid key: " + key.toString());
        }
        List<String> groups = Collections.emptyList();
        Context context = new Context(subjectId, acc, subject, principal, groups);
        subjectIds.put(context.getId(), subject);
        subjectContexts.put(subject, context);

        return context.getId();
    }

    public static synchronized AccessControlContext registerSubjectShort(Subject subject, Principal callerPrincipal, List<String> groups) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);

        if (subject == null) throw new IllegalArgumentException("Subject must not be null");
        
        Context test = subjectContexts.get(subject);
        if (test != null) {
            return test.getContext();
        }

        AccessControlContext acc = (AccessControlContext) Subject.doAsPrivileged(subject, new PrivilegedAction() {
            public Object run() {
                return AccessController.getContext();
            }
        }, null);

        Long id = nextSubjectId++;
        SubjectId subjectId;
        try {
            subjectId = new SubjectId(id, hash(id));
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException("No such algorithm: " + algorithm + ".  This can be caused by a misconfigured java.ext.dirs, JAVA_HOME or JRE_HOME environment variable");
        } catch (InvalidKeyException e) {
            throw new ProviderException("Invalid key: " + key.toString());
        }
        IdentificationPrincipal principal = new IdentificationPrincipal(subjectId);
        
        if(!subject.isReadOnly()){
            subject.getPrincipals().add(principal);
        }
        Context context = new Context(subjectId, acc, subject, callerPrincipal, groups);
        subjectIds.put(context.getId(), subject);
        subjectContexts.put(subject, context);

        return acc;
    }

    public static synchronized void unregisterSubject(Subject subject) {
        
        if (subject == EMPTY) {
            return;
        }
        
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);

        if (subject == null) throw new IllegalArgumentException("Subject must not be null");

        Context context = subjectContexts.get(subject);
        if (context == null) return;

        subjectIds.remove(context.getId());
        subjectContexts.remove(subject);
    }

    /**
     * Obtain the thread's identifying principal.
     * <p/>
     * Clients should use <code>Subject.doAs*</code> to associate a Subject
     * with the thread's call stack.  It is this Subject that will be used for
     * authentication checks.
     * <p/>
     * Return a <code>IdentificationPrincipal</code>.  This kind of principal
     * is inserted into a subject if one uses one of the Geronimo LoginModules.
     * It is a secure id that identifies the Subject.
     *
     * @return the principal that identifies the Subject of this thread.
     * @see Subject#doAs(javax.security.auth.Subject, java.security.PrivilegedAction)
     * @see Subject#doAs(javax.security.auth.Subject, java.security.PrivilegedExceptionAction)
     * @see Subject#doAsPrivileged(javax.security.auth.Subject, java.security.PrivilegedAction, java.security.AccessControlContext)
     * @see Subject#doAsPrivileged(javax.security.auth.Subject, java.security.PrivilegedExceptionAction, java.security.AccessControlContext)
     */
    public static IdentificationPrincipal getThreadPrincipal() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        Subject subject = Subject.getSubject(AccessController.getContext());
        if (subject != null) {
            Set set = subject.getPrincipals(IdentificationPrincipal.class);
            if (!set.isEmpty()) return (IdentificationPrincipal) set.iterator().next();
        }
        return null;
    }

    public static ThreadData getThreadData() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);
        return threadData.get();
    }

    public static String getAlgorithm() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        return algorithm;
    }

    public static void setAlgorithm(String algorithm) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);

        ContextManager.algorithm = algorithm;

        key = new SecretKeySpec(password.getBytes(), algorithm);

        /**
         * Make sure that we can generate the  Mac.
         */
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(key);
        } catch (NoSuchAlgorithmException e) {
            assert false : "Should never have reached here";
            throw new ProviderException("No such algorithm: " + algorithm + ".  This can be caused by a misconfigured java.ext.dirs, JAVA_HOME or JRE_HOME environment variable.");
        } catch (InvalidKeyException e) {
            assert false : "Should never have reached here";
            throw new ProviderException("Invalid key: " + key.toString());
        }
    }

    public static String getPassword() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        return password;
    }

    public static void setPassword(String password) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);

        ContextManager.password = password;

        key = new SecretKeySpec(password.getBytes(), algorithm);
    }

    private static byte[] hash(Long id) throws NoSuchAlgorithmException, InvalidKeyException {
        long n = id;
        byte[] bytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (n);
            n >>>= 8;
        }

        Mac mac = Mac.getInstance(algorithm);
        mac.init(key);
        mac.update(bytes);

        return mac.doFinal();
    }

}
