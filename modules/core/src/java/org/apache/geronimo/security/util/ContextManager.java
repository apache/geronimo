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
 *        Apache Software Foundation (http:www.apache.org/)."
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
 * <http:www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.security.util;

import org.apache.geronimo.security.GeronimoSecurityPermission;
import org.apache.geronimo.security.RealmPrincipal;

import javax.security.jacc.EJBRoleRefPermission;
import javax.security.auth.Subject;
import java.util.Stack;
import java.util.Hashtable;
import java.util.Map;
import java.util.Iterator;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.Principal;


/**
 *
 * @version $Revision: 1.2 $ $Date: 2003/11/12 04:29:04 $
 */

public class ContextManager {
    private static ContextThreadLocalStack contexts = new ContextThreadLocalStack();
    private static Map subjectContexts = new Hashtable();
    private static ThreadLocal methodIndexes = new ThreadLocal();

    public static final GeronimoSecurityPermission GET_CONTEXT = new GeronimoSecurityPermission("getContext");
    public static final GeronimoSecurityPermission SET_CONTEXT = new GeronimoSecurityPermission("setContext");

    public static AccessControlContext peekContext() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_CONTEXT);

        return contexts.peek().context;
    }

    public static Subject popSubject() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);

        return contexts.pop().subject;
    }

    public static void pushSubject(Subject subject) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);

        Context context = new Context();
        context.subject = subject;
        context.context = (AccessControlContext)subjectContexts.get(subject);

        assert context.context != null;

        contexts.push(context);
    }

    public static void registerContext(Subject subject, AccessControlContext context) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);

        subjectContexts.put(subject, context);
    }

    public static void unregisterContext(Subject subject) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);

        subjectContexts.remove(subject);
    }

    public static void setMethodIndex(int index) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_CONTEXT);

        methodIndexes.set(new Integer(index));
    }

    public static int getMethodIndex() {
        return ((Integer)methodIndexes.get()).intValue();
    }

    public static Principal getCallerPrincipal() {
        Iterator iter = contexts.peek().subject.getPrincipals(RealmPrincipal.class).iterator();
        
        assert iter.hasNext();

        return (RealmPrincipal)iter.next();
    }

    public static boolean isCallerInRole(String EJBName, String role) {
        try {
            contexts.peek().context.checkPermission(new EJBRoleRefPermission(EJBName, role));
        } catch (AccessControlException e) {
            return false;
        }
        return true;
    }

    public static class Context {
        AccessControlContext context;
        Subject subject;
    }

    private static class ContextThreadLocalStack extends ThreadLocal {
        protected Object initialValue() {
            return new Stack();
        }

        void push(Context context) {
            Stack stack = (Stack) super.get();
            stack.push(context);
        }

        Context pop() {
            Stack stack = (Stack) super.get();
            return (Context) stack.pop();
        }

        Context peek() {
            Stack stack = (Stack) super.get();
            return (Context) stack.peek();
        }
    }
}
