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

package javax.enterprise.deploy.shared;

import junit.framework.TestCase;

public class ModuleTypeTest extends TestCase {
      public void testValues() {
        assertEquals(0, ModuleType.EAR.getValue());
        assertEquals(1, ModuleType.EJB.getValue());
        assertEquals(2, ModuleType.CAR.getValue());
        assertEquals(3, ModuleType.RAR.getValue());
        assertEquals(4, ModuleType.WAR.getValue());
    }

    public void testToString() {
        assertEquals("ear", ModuleType.EAR.toString());
        assertEquals("ejb", ModuleType.EJB.toString());
        assertEquals("car", ModuleType.CAR.toString());
        assertEquals("rar", ModuleType.RAR.toString());
        assertEquals("war", ModuleType.WAR.toString());
        // only possible due to package local access
        assertEquals("5", new ModuleType(5).toString());
    }

    public void testModuleExtension() {
        assertEquals(".ear", ModuleType.EAR.getModuleExtension());
        assertEquals(".jar", ModuleType.EJB.getModuleExtension());
        assertEquals(".jar", ModuleType.CAR.getModuleExtension());
        assertEquals(".rar", ModuleType.RAR.getModuleExtension());
        assertEquals(".war", ModuleType.WAR.getModuleExtension());
    }

    public void testValueToSmall() {
        try {
            ModuleType.getModuleType(-1);
            fail("Expected AIOOBE");
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        }
    }

    public void testValueToLarge() {
        try {
            ModuleType.getModuleType(5);
            fail("Expected AIOOBE");
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        }
    }
}
