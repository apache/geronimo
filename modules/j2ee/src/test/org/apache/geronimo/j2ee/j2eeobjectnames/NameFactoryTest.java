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
package org.apache.geronimo.j2ee.j2eeobjectnames;

import javax.management.ObjectName;

import junit.framework.TestCase;

/**
 * @version $Rev:  $ $Date:  $
 */
public class NameFactoryTest extends TestCase {
    String d = "d";
    String s = "s";
    String a = "a";
    String mt = "mt";
    String m = "m";
    String t = "t";
    String n = "n";
    J2eeContext context = new J2eeContextImpl("d1", "s1", "a1", "mt1", "m1", "n1", "t1");


    public void testquery1() throws Exception {
        ObjectName o1 = NameFactory.getComponentNameQuery(d, s, a, n, t, context);
        ObjectName o2 = NameFactory.getComponentNameQuery(d, s, a, null, "*", n, t, context);
        assertEquals(o1, o2);
        assertEquals(o1, ObjectName.getInstance("d:J2EEServer=s,J2EEApplication=a,j2eeType=t,name=n,*"));
    }

    public void testquery2() throws Exception {
        ObjectName o1 = NameFactory.getComponentInModuleQuery(d, s, a, mt, m, t, context);
        ObjectName o2 = NameFactory.getComponentNameQuery(d, s, a, mt, m, "*", t, context);
        assertEquals(o1, o2);
//        System.out.println(o1);
        assertEquals(o1, ObjectName.getInstance("d:j2eeType=t,J2EEServer=s,J2EEApplication=a,mt=m,*"));
    }
}
