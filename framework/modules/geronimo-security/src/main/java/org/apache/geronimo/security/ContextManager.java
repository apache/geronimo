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
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.ProviderException;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.jacc.EJBRoleRefPermission;

import org.apache.geronimo.security.realm.providers.GeronimoCallerPrincipal;


/**
 * @version $Rev$ $Date$
 */
public class ContextManager {

    private static final ThreadLocal<Callers> callers = new ThreadLocal<Callers>();
    private static Map<Subject, Context> subjectContexts = new IdentityHashMap<Subject, Context>();
    private static Map<SubjectId, Subject> subjectIds =  Collections.synchronizedMap(new HashMap<SubjectId, Subject>());
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
        EMPTY.setReadOnly();
        registerSubject(EMPTY);
    }

    public static LoginContext login(String realm, CallbackHandler callbackHandler) throws LoginException {
        Subject subject = new Subject();
        LoginContext loginContext = new LoginContext(realm, subject, callbackHandler);
        loginContext.login();
        SubjectId id = ContextManager.registerSubject(subject);
        IdentificationPrincipal principal = new IdentificationPrincipal(id);
        subject.getPrincipals().add(principal);
        return loginContext;
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

        return context.context;
    }

    public static Principal getCurrentPrincipal(Subject callerSubject) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        if (callerSubject == null) {
            return new Principal() {
                public String getName() {
                    return "";
                }
            };
        }
        Context context = subjectContexts.get(callerSubject);

        assert context != null : "No registered context";

        return context.principal;
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

        return context.id;
    }

    public static SubjectId getSubjectId(Subject subject) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        Context context = subjectContexts.get(subject);

        return (context != null ? context.id : null);
    }

    public static boolean isCallerInRole(String EJBName, String role) {
        if (EJBName == null) throw new IllegalArgumentException("EJBName must not be null");
        if (role == null) throw new IllegalArgumentException("Role must not be null");

        try {
            Callers currentCallers = callers.get();
            if (currentCallers == null) {
                return false;
            }
            Subject currentSubject = currentCallers.getCurrentCaller();
            if (currentSubject == null) {
                return false;
            }

            Context context = subjectContexts.get(currentSubject);

            assert context != null : "No registered context";

            context.context.checkPermission(new EJBRoleRefPermission(EJBName, role));
        } catch (AccessControlException e) {
            return false;
        }
        return true;
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

        Context context = new Context();
        context.subject = subject;
        context.context = acc;
        Set<? extends Principal> principals = subject.getPrincipals(GeronimoCallerPrincipal.class);
        if (!principals.isEmpty()) {
            context.principal = principals.iterator().next();
        } else if (!(principals = subject.getPrincipals(PrimaryRealmPrincipal.class)).isEmpty()) {
            context.principal = principals.iterator().next();
        } else if (!(principals = subject.getPrincipals(RealmPrincipal.class)).isEmpty()) {
            context.principal = principals.iterator().next();
        } else if (!(principals = subject.getPrincipals()).isEmpty()) {
            context.principal = principals.iterator().next();
        }
        Long id = nextSubjectId++;
        try {
            context.id = new SubjectId(id, hash(id));
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException("No such algorithm: " + algorithm + ".  This can be caused by a misconfigured java.ext.dirs, JAVA_HOME or JRE_HOME environment variable");
        } catch (InvalidKeyException e) {
            throw new ProviderException("Invalid key: " + key.toString());
        }
        subjectIds.put(context.id, subject);
        subjectContexts.put(subject, context);

        return context.id;
    }

    public static synchronized void unregisterSubject(Subject subject) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);

        if (subject == null) throw new IllegalArgumentException("Subject must not be null");

        Context context = subjectContexts.get(subject);
        if (context == null) return;

        subjectIds.remove(context.id);
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

    private static class Context {
        SubjectId id;
        AccessControlContext context;
        Subject subject;
        Principal principal;
    }

}
