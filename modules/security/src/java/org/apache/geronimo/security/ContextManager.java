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

package org.apache.geronimo.security;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.Subject;
import javax.security.jacc.EJBRoleRefPermission;

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


/**
 * @version $Revision: 1.6 $ $Date: 2004/02/25 09:58:08 $
 */
public class ContextManager {
    private static ThreadLocal currentCallerId = new ThreadLocal();
    private static ThreadLocal currentCaller = new ThreadLocal();
    private static ThreadLocal nextCaller = new ThreadLocal();
    private static Map subjectContexts = new IdentityHashMap();
    private static Map subjectIds = new Hashtable();
    private static long nextSubjectId = System.currentTimeMillis();
    private static Map principals = new Hashtable();

    private static long nextPrincipalId = System.currentTimeMillis();

    private static SecretKey key;
    private static String algorithm;
    private static String password;

    static {
        password = "secret";
        ContextManager.setAlgorithm("HmacSHA1");
    }


    public static final GeronimoSecurityPermission GET_CONTEXT = new GeronimoSecurityPermission("getContext");
    public static final GeronimoSecurityPermission SET_CONTEXT = new GeronimoSecurityPermission("setContext");


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

        Context context = (Context) subjectContexts.get(currentCaller.get());

        assert context != null : "No registered context";

        return context.context;
    }

    public static Principal getCurrentPrincipal() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        Object caller = currentCaller.get();
        if (caller == null) {
            return new Principal() {
                public String getName() {
                    return "";
                }
            };
        }
        Context context = (Context) subjectContexts.get(currentCaller.get());

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
        if (!subject.getPrincipals(PrimaryRealmPrincipal.class).isEmpty()) {
            context.principal = (PrimaryRealmPrincipal) subject.getPrincipals(PrimaryRealmPrincipal.class).iterator().next();
        } else if (!subject.getPrincipals(RealmPrincipal.class).isEmpty()) {
            context.principal = (RealmPrincipal) subject.getPrincipals(RealmPrincipal.class).iterator().next();
        } else if (!subject.getPrincipals().isEmpty()) {
            context.principal = (Principal) subject.getPrincipals().iterator().next();
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

    public static RealmPrincipal registerPrincipal(RealmPrincipal principal) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);


        if (principal == null) throw new IllegalArgumentException("Principal must not be null");

        RealmPrincipal result = (RealmPrincipal) principals.get(principal);

        if (result == null) {
            synchronized (principals) {
                result = (RealmPrincipal) principals.get(principal);
                if (result == null) {
                    principal.setId(nextPrincipalId++);
                    principals.put(principal, principal);
                    result = principal;
                }
            }
        }

        return result;
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
