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
package javax.security.jacc;

import java.security.PermissionCollection;
import java.security.Permission;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Collections;


/**
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/27 16:00:23 $
 */
public final class EJBMethodPermissionCollection extends PermissionCollection {

    private LinkedList collection = new LinkedList();
    private HashMap permissions = new HashMap();
    private static final Object WILDCARD = new Object();

    /**
     * Adds a permission object to the current collection of permission objects.
     *
     * @param permission the Permission object to add.
     *
     * @exception SecurityException -  if this PermissionCollection object
     *                                 has been marked readonly
     */
    public void add(Permission permission) {
        if (isReadOnly()) throw new IllegalArgumentException("Read only collection");

        if (!(permission instanceof EJBMethodPermission)) throw new IllegalArgumentException("Wrong permission type");

        if (collection.contains(permission)) return;
        else collection.add(permission);

        EJBMethodPermission p = (EJBMethodPermission)permission;
        EJBMethodPermission.MethodSpec spec = p.methodSpec;
        Object test =  permissions.get(p.getName());

        if (test instanceof Boolean) return;

        if (spec.methodName == null && spec.methodInterface == null && spec.methodParams == null) {
            permissions.put(p.getName(), new Boolean(true));
            return;
        }

        HashMap methods = (HashMap)test;
        if (methods == null) {
            methods = new HashMap();
            permissions.put(p.getName(), methods);
        }

        Object methodKey = (spec.methodName == null || spec.methodName.length() == 0? WILDCARD:spec.methodName);
        HashMap interfaces = (HashMap)methods.get(methodKey);
        if (interfaces == null) {
            interfaces = new HashMap();
            methods.put(methodKey, interfaces);
        }

        Object interfaceKey = (spec.methodInterface == null || spec.methodInterface.length() == 0? WILDCARD:spec.methodInterface);
        HashMap parameters = (HashMap)interfaces.get(interfaceKey);
        if (parameters == null) {
            parameters = new HashMap();
            interfaces.put(interfaceKey, parameters);
        }

        // an empty string for a parameter spec indicates a method w/ no parameters
        Object parametersKey = (spec.methodParams == null? WILDCARD:spec.methodParams);
        Object parameter = parameters.get(parametersKey);
        if (parameter == null) {
            parameter = new Boolean(true);
            parameters.put(parametersKey, parameter);
        }
    }

    /**
     * Checks to see if the specified permission is implied by
     * the collection of Permission objects held in this PermissionCollection.
     *
     * @param permission the Permission object to compare.
     *
     * @return true if "permission" is implied by the  permissions in
     * the collection, false if not.
     */
    public boolean implies(Permission permission) {
        if (!(permission instanceof EJBMethodPermission)) return false;

        EJBMethodPermission p = (EJBMethodPermission)permission;

        EJBMethodPermission.MethodSpec spec = p.methodSpec;
        Object test = permissions.get(p.getName());

        if (test == null) return false;
        if (test instanceof Boolean) return true;

        HashMap methods = (HashMap)test;

        Object methodKey = (spec.methodName == null || spec.methodName.length() == 0? WILDCARD:spec.methodName);
        HashMap interfaces = (HashMap)methods.get(methodKey);

        if (methodImplies(interfaces, spec)) return true;
        if (methodKey != WILDCARD) {
            return methodImplies((HashMap)methods.get(WILDCARD), spec);
        }

        return false;
    }

    protected boolean methodImplies(HashMap interfaces, EJBMethodPermission.MethodSpec spec) {
        if (interfaces == null) return false;

        Object interfaceKey = (spec.methodInterface == null || spec.methodInterface.length() == 0? WILDCARD:spec.methodInterface);
        HashMap parameters = (HashMap)interfaces.get(interfaceKey);

        if (interfaceImplies(parameters, spec)) return true;
        if (interfaceKey != WILDCARD) {
            return interfaceImplies((HashMap)interfaces.get(WILDCARD), spec);
        }

        return false;
    }

    protected boolean interfaceImplies(HashMap parameters, EJBMethodPermission.MethodSpec spec) {
        if (parameters == null) return false;

        // An empty string for a parameter spec indicates a method w/ no parameters
        // so we won't convert an empty string to a wildcard.
        Object parametersKey = (spec.methodParams == null? WILDCARD:spec.methodParams);
        Object parameter = parameters.get(parametersKey);

        if (parameter != null) return true;
        if (parametersKey != WILDCARD) {
            return parameters.containsKey(WILDCARD);
        }

        return false;
    }

    /**
     * Returns an enumeration of all the Permission objects in the collection.
     *
     * @return an enumeration of all the Permissions.
     */
    public Enumeration elements() {
        return Collections.enumeration(collection);
    }
}
