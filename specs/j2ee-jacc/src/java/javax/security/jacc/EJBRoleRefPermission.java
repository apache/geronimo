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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.security.jacc;

import java.io.Serializable;
import java.security.Permission;

/**
 * Class for EJB <code>isCallerInRole(String reference)</code> permissions. An
 * EJBRoleRefPermission is a named permission and has actions.<p>
 *
 * The name of an EJBRoleRefPermission contains the value of the ejb-name
 * element in the application's deployment descriptor that identifies the EJB
 * in whose context the permission is being evalutated.<p>
 *
 * The actions of an EJBRoleRefPermission identifies the role reference to
 * which the permission applies. An EJBRoleRefPermission is checked to
 * determine if the subject is a member of the role identified by the reference.
 * @version $Rev$ $Date$
 */
public final class EJBRoleRefPermission extends Permission implements Serializable {

    private transient int cachedHashCode = 0;
    private String actions;

    /**
     * Creates a new EJBRoleRefPermission with the specified name and actions.
     * @param name the ejb-name that identifies the EJB in whose context the
     * role references are to be evaluated.
     * @param role identifies the role reference to which the permission
     * pertains. The role reference is scoped to the EJB identified in the
     * name parameter. The value of the role reference must not be null or
     * the empty string.
     */
    public EJBRoleRefPermission(String name, String role) {
        super(name);

        if (role == null || role.length() == 0)
            throw new IllegalArgumentException("Role reference must not be null or the empty string");

        actions = role;
    }

    /**
     * Checks two EJBRoleRefPermission objects for equality. EJBRoleRefPermission
     * objects are equivalent if they have case equivalent name and actions values.<p>
     *
     * Two Permission objects, P1 and P2, are equivalent if and only if P1.implies(P2) && P2.implies(P1).
     * @param o the EJBRoleRefPermission object being tested for equality with this EJBRoleRefPermission.
     * @return true if the argument EJBRoleRefPermission object is equivalent to this EJBRoleRefPermission.
     */
    public boolean equals(Object o) {
        if (o == null || !(o instanceof EJBRoleRefPermission)) return false;

        EJBRoleRefPermission other = (EJBRoleRefPermission)o;
        return getName().equals(other.getName()) && actions.equals(other.actions);
    }

    /**
     * Returns a canonical String representation of the actions of this EJBRoleRefPermission.
     * @return a String containing the canonicalized actions of this EJBRoleRefPermission.
     */
    public String getActions() {
        return actions;
    }

    /**
     * Returns the hash code value for this EJBRoleRefPermission. The properties
     * of the returned hash code must be as follows:
     * <ul>
     * <li>During the lifetime of a Java application, the hashCode method must
     * return the same integer value, every time it is called on a EJBRoleRefPermission
     * object. The value returned by hashCode for a particular EJBRoleRefPermission
     * need not remain consistent from one execution of an application to another.</li>
     * <li>If two EJBRoleRefPermission objects are equal according to the equals
     * method, then calling the hashCode method on each of the two Permission
     * objects must produce the same integer result (within an application).</li>
     * </ul>
     * @return the integer hash code value for this object.
     */
    public int hashCode() {
        if (cachedHashCode == 0) {
            cachedHashCode = getName().hashCode() ^ actions.hashCode();
        }
        return cachedHashCode;
    }

    /**
     * Determines if the argument Permission is "implied by" this
     * EJBRoleRefPermission. For this to be the case,
     *
     * <ul>
     * <li>The argument must be an instanceof EJBRoleRefPermission</li>
     * <li>with name equivalent to that of this EJBRoleRefPermission, and</li>
     * <li>with the role reference equivalent to that of this EJBRoleRefPermission applies.</li>
     * <ul>
     * The name and actions comparisons described above are case sensitive.
     * @param permission "this" EJBRoleRefPermission is checked to see if it implies the argument permission.
     * @return true if the specified permission is implied by this object, false if not.
     */
    public boolean implies(Permission permission) {
        return equals(permission);
    }
}

