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

import java.security.PermissionCollection;
import java.security.Permission;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:53 $
 */
public final class WebUserDataPermissionCollection extends PermissionCollection {
    private Hashtable permissions = new Hashtable();

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

        if (!(permission instanceof WebUserDataPermission)) throw new IllegalArgumentException("Wrong permission type");

        WebUserDataPermission p  = (WebUserDataPermission)permission;

        permissions.put(p, p);
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
        if (!(permission instanceof WebUserDataPermission)) return false;

        WebUserDataPermission p  = (WebUserDataPermission)permission;
        Enumeration enum = permissions.elements();

        while (enum.hasMoreElements()) {
            if (((WebUserDataPermission)enum.nextElement()).implies(p)) return true;
        }

        return false;

    }

    /**
     * Returns an enumeration of all the Permission objects in the collection.
     *
     * @return an enumeration of all the Permissions.
     */
    public Enumeration elements() {
        return permissions.elements();
    }
}
