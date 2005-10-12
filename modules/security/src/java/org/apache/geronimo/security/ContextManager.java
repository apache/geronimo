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

package org.apache.geronimo.security;

import java.io.Serializable;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.Subject;
import javax.security.jacc.EJBRoleRefPermission;

import org.apache.geronimo.security.realm.providers.GeronimoCallerPrincipal;


/**
 * @version $Rev$ $Date$
 */
public class ContextManager {
    private static ThreadLocal currentCallerId = new ThreadLocal();
    private static ThreadLocal currentCaller = new ThreadLocal();
    private static ThreadLocal nextCaller = new ThreadLocal();
    private static Map subjectContexts = new IdentityHashMap();
    private static Map subjectIds = new Hashtable();
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

    /**
     * After a login, the client is left with a relatively empty Subject, while
     * the Subject used by the server has more important contents.  This method
     * lets a server-side component acting as an authentication client (such
     * as Tocmat/Jetty) access the fully populated server-side Subject.
     */
    public static Subject getServerSideSubject(Subject clientSideSubject) {
        Set set = clientSideSubject.getPrincipals(IdentificationPrincipal.class);
        if(set == null || set.size() == 0) {
            return null;
        }
        IdentificationPrincipal idp = (IdentificationPrincipal)set.iterator().next();
        return getRegisteredSubject(idp.getId());
    }

    public static void setCurrentCallerId(Serializable id) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);

        currentCallerId.set(id);
    }

    public static Serializable getCurrentCallerId() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        return (Serializable) currentCallerId.get();
    }

    public static void setNextCaller(Subject subject) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);

        nextCaller.set(subject);
    }

    public static Subject getNextCaller() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        return (Subject) nextCaller.get();
    }

    public static void setCurrentCaller(Subject subject) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);

        currentCaller.set(subject);
    }

    public static Subject getCurrentCaller() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        return (Subject) currentCaller.get();
    }

    public static AccessControlContext getCurrentContext() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        Subject currentSubject = (Subject) currentCaller.get();
        assert currentSubject != null : "No current caller";
        Context context = (Context) subjectContexts.get(currentSubject);

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
        Context context = (Context) subjectContexts.get(callerSubject);

        assert context != null : "No registered context";

        return context.principal;
    }

    public static SubjectId getCurrentId() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        Context context = (Context) subjectContexts.get(currentCaller.get());

        assert context != null : "No registered context";

        return context.id;
    }

    public static SubjectId getSubjectId(Subject subject) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        Context context = (Context) subjectContexts.get(subject);

        return (context != null ? context.id : null);
    }

    public static boolean isCallerInRole(String EJBName, String role) {
        if (EJBName == null) throw new IllegalArgumentException("EJBName must not be null");
        if (role == null) throw new IllegalArgumentException("Role must not be null");

        try {
            Object caller = currentCaller.get();
            if (caller == null) return false;

            Context context = (Context) subjectContexts.get(currentCaller.get());

            assert context != null : "No registered context";

            context.context.checkPermission(new EJBRoleRefPermission(EJBName, role));
        } catch (AccessControlException e) {
            return false;
        }
        return true;
    }

    public static Subject getRegisteredSubject(SubjectId id) {
        return (Subject) subjectIds.get(id);
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
        Set principals = subject.getPrincipals(GeronimoCallerPrincipal.class);
        if (!principals.isEmpty()) {
            context.principal = (Principal) principals.iterator().next();
        } else if (!(principals = subject.getPrincipals(PrimaryRealmPrincipal.class)).isEmpty()) {
            context.principal = (PrimaryRealmPrincipal) principals.iterator().next();
        } else if (!(principals = subject.getPrincipals(RealmPrincipal.class)).isEmpty()) {
            context.principal = (RealmPrincipal) principals.iterator().next();
        } else if (!(principals = subject.getPrincipals()).isEmpty()) {
            context.principal = (Principal) principals.iterator().next();
        }
        Long id = new Long(nextSubjectId++);
        context.id = new SubjectId(id, hash(id));

        subjectIds.put(context.id, subject);
        subjectContexts.put(subject, context);

        return context.id;
    }

    public static synchronized void unregisterSubject(Subject subject) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);

        if (subject == null) throw new IllegalArgumentException("Subject must not be null");

        Context context = (Context) subjectContexts.get(subject);
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
        } catch (InvalidKeyException e) {
            assert false : "Should never have reached here";
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

    private static byte[] hash(Long id) {
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

    private static class Context {
        SubjectId id;
        AccessControlContext context;
        Subject subject;
        Principal principal;
    }

}
