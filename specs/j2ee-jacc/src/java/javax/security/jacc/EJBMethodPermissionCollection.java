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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.security.jacc;

import java.security.PermissionCollection;
import java.security.Permission;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Collections;


/**
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:38 $
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
