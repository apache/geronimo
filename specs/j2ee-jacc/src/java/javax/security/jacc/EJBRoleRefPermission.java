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
 * This source code implements specifications defined by the Java
 * Community Process. In order to remain compliant with the specification
 * DO NOT add / change / or delete method signatures!
 *
 * ====================================================================
 */

package javax.security.jacc;

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
 * @version $Revision: 1.2 $ $Date: 2003/11/18 05:30:16 $
 */
public final class EJBRoleRefPermission extends Permission {

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

