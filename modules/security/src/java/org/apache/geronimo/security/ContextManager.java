/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * @version $Revision: 1.5 $ $Date: 2004/02/18 03:54:21 $
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
