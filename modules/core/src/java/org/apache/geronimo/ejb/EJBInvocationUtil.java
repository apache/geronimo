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
package org.apache.geronimo.ejb;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.Principal;

import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationKey;

/**
 *
 *
 *
 * @version $Revision: 1.7 $ $Date: 2003/09/08 04:28:26 $
 */
public final class EJBInvocationUtil implements Serializable, InvocationKey {
    
    // Be careful here.  If you change the ordinals, this class must be changed on evey client.
    private static int MAX_ORDINAL = 5;
    private static final EJBInvocationUtil[] values = new EJBInvocationUtil[MAX_ORDINAL + 1];
    private static final EJBInvocationUtil METHOD = new EJBInvocationUtil("METHOD", 0, false);
    private static final EJBInvocationUtil ID = new EJBInvocationUtil("ID", 1, false);
    private static final EJBInvocationUtil ARGUMENTS = new EJBInvocationUtil("ARGUMENTS", 2, false);
    private static final EJBInvocationUtil EJB_CONTEXT_KEY = new EJBInvocationUtil("EJB_CONTEXT_KEY", 3, true);
    private static final EJBInvocationUtil PRINCIPAL = new EJBInvocationUtil("PRINCIPAL", 4, false);
    private static final EJBInvocationUtil CREDENTIALS = new EJBInvocationUtil("CREDENTIALS", 5, false);

    public static Method getMethod(Invocation invocation) {
        return (Method) invocation.get(METHOD);
    }

    public static void putMethod(Invocation invocation, Method method) {
        invocation.put(METHOD, method);
    }

    public static Object getId(Invocation invocation) {
        return invocation.get(ID);
    }

    public static void putId(Invocation invocation, Object id) {
        invocation.put(ID, id);
    }

    public static Object[] getArguments(Invocation invocation) {
        return (Object[]) invocation.get(ARGUMENTS);
    }

    public static void putArguments(Invocation invocation, Object[] arguments) {
        invocation.put(ARGUMENTS, arguments);
    }

    public static EnterpriseContext getEnterpriseContext(Invocation invocation) {
        return (EnterpriseContext) invocation.get(EJB_CONTEXT_KEY);
    }

    public static void putEnterpriseContext(Invocation invocation, EnterpriseContext enterpriseContext) {
        invocation.put(EJB_CONTEXT_KEY, enterpriseContext);
    }

    public static Principal getPrincipal(Invocation invocation) {
        return (Principal) invocation.get(PRINCIPAL);
    }

    public static void putPrincipal(Invocation invocation, Principal principal) {
        invocation.put(PRINCIPAL, principal);
    }

    public static Object getCredentials(Invocation invocation) {
        return invocation.get(CREDENTIALS);
    }

    public static void putCredentials(Invocation invocation, Object credentials) {
        invocation.put(CREDENTIALS, credentials);
    }

    private final transient String name;
    private final int ordinal;
    private final transient boolean isTransient;

    private EJBInvocationUtil(String name, int ordinal, boolean isTransient) {
        assert ordinal < MAX_ORDINAL;
        assert values[ordinal] == null;
        this.name = name;
        this.ordinal = ordinal;
        values[ordinal] = this;
        this.isTransient = isTransient;
    }

    public String toString() {
        return name;
    }

    Object readResolve() throws ObjectStreamException {
        return values[ordinal];
    }

    /**
     * @see org.apache.geronimo.core.service.InvocationKey#isTransient()
     */
    public boolean isTransient() {
        return isTransient;
    }
}
