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

import junit.framework.TestCase;

/**
 *
 * @version $Rev$ $Date$
 */
public class WebRoleRefPermissionTest extends TestCase {

     public void test() {
         WebRoleRefPermission permission1 = new WebRoleRefPermission("foo", "bar");
         WebRoleRefPermission permission2 = new WebRoleRefPermission("foo", "bar");
         WebRoleRefPermission permission3 = new WebRoleRefPermission("foo", "cat");

         assertTrue(permission1.implies(permission1));
         assertTrue(permission1.implies(permission2));
         assertTrue(permission2.implies(permission1));

         assertTrue(permission1.equals(permission1));
         assertTrue(permission1.equals(permission2));
         assertTrue(permission2.equals(permission1));

         assertFalse(permission1.implies(permission3));
         assertFalse(permission3.implies(permission1));

         assertFalse(permission1.equals(permission3));
         assertFalse(permission3.equals(permission1));

         assertEquals(permission1.getName(), "foo");
         assertEquals(permission1.getActions(), "bar");
         assertEquals(permission1.hashCode(), permission2.hashCode());
    }

}

