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
import java.util.HashSet;
import java.util.Set;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;

import org.apache.geronimo.ejb.EJBInvocationUtil;
import org.apache.geronimo.common.Invocation;
import org.apache.geronimo.common.InvocationType;

/**
 *
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/10 20:51:54 $
 */
public final class Entrancy implements Serializable {
    private static final String ENTRANCY_KEY = "ENTRANCY_KEY";

    public static final Entrancy ENTRANT = new Entrancy("ENTRANT", true);
    public static final Entrancy NON_ENTRANT = new Entrancy("NON_ENTRANT", false);

    private static final Set nonEntrantMethods = new HashSet();

    static {
        try {
            Class[] noArg = new Class[0];
            nonEntrantMethods.add(EJBObject.class.getMethod("getEJBHome", noArg));
            nonEntrantMethods.add(EJBObject.class.getMethod("getHandle", noArg));
            nonEntrantMethods.add(EJBObject.class.getMethod("getPrimaryKey", noArg));
            nonEntrantMethods.add(EJBObject.class.getMethod("isIdentical", new Class[]{EJBObject.class}));
            nonEntrantMethods.add(EJBObject.class.getMethod("remove", noArg));

            nonEntrantMethods.add(EJBLocalObject.class.getMethod("getEJBLocalHome", noArg));
            nonEntrantMethods.add(EJBLocalObject.class.getMethod("getPrimaryKey", noArg));
            nonEntrantMethods.add(EJBLocalObject.class.getMethod("isIdentical", new Class[]{EJBLocalObject.class}));
            nonEntrantMethods.add(EJBLocalObject.class.getMethod("remove", noArg));
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static boolean isNonEntrant(Invocation invocation) {
        // Home invocations are not entrant
        if (InvocationType.getType(invocation).isHomeInvocation()) {
            return true;
        }
        // The methods above are not entrant
        if (nonEntrantMethods.contains(EJBInvocationUtil.getMethod(invocation))) {
            return true;
        }
        // If the invocation contains an entrancy value and it is non-entrant then we are non-entrant
        return getEntrancy(invocation) == NON_ENTRANT;
    }

    public static Entrancy getEntrancy(Invocation invocation) {
        return (Entrancy) invocation.getTransient(ENTRANCY_KEY);
    }

    public static void putEntrancy(Invocation invocation, Entrancy type) {
        invocation.putTransient(ENTRANCY_KEY, type);
    }

    private final transient String name;
    private final transient boolean entrant;

    private Entrancy(String name, boolean entrant) {
        this.name = name;
        this.entrant = entrant;
    }

    public String toString() {
        return name;
    }

    Object readResolve() throws ObjectStreamException {
        if (entrant) {
            return ENTRANT;
        } else {
            return NON_ENTRANT;
        }
    }
}
