/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.naming.enc;

import junit.framework.TestCase;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class EnterpriseNamingContextTest extends TestCase {
    private static final String STRING_VAL = "some string";

    public void testLookup() throws Exception {
        Map map = new HashMap();
        map.put("string", STRING_VAL);
        map.put("nested/context/string", STRING_VAL);

        Context context = EnterpriseNamingContext.createEnterpriseNamingContext(map);

        Name stringName = context.getNameParser("").parse("string");
        assertEquals(STRING_VAL, context.lookup("string"));
        assertEquals(STRING_VAL, context.lookup(stringName));
        assertEquals(STRING_VAL, context.lookupLink("string"));
        assertEquals(STRING_VAL, context.lookupLink(stringName));

        Name nestedContextStringName = context.getNameParser("").parse("nested/context/string");
        assertEquals(STRING_VAL, context.lookup("nested/context/string"));
        assertEquals(STRING_VAL, context.lookup(nestedContextStringName));
        assertEquals(STRING_VAL, context.lookupLink("nested/context/string"));
        assertEquals(STRING_VAL, context.lookupLink(nestedContextStringName));
    }

    public void testList() throws Exception {
        Map map = new HashMap();
        map.put("one", new Integer(1));
        map.put("two", new Integer(2));
        map.put("three", new Integer(3));

        Context context = EnterpriseNamingContext.createEnterpriseNamingContext(map);

        Map result = toListResults(context.list(""));
        assertEquals(4, result.size());
        assertEquals(Integer.class.getName(), result.get("one"));
        assertEquals(Integer.class.getName(), result.get("two"));
        assertEquals(Integer.class.getName(), result.get("three"));
        assertNotNull(result.get("env"));

        result = toListBindingResults(context.listBindings(""));
        assertEquals(4, result.size());
        assertEquals(new Integer(1), result.get("one"));
        assertEquals(new Integer(2), result.get("two"));
        assertEquals(new Integer(3), result.get("three"));
        assertNotNull(result.get("env"));
    }

    private Map toListResults(NamingEnumeration enumeration) {
        Map result = new HashMap();
        while (enumeration.hasMoreElements()) {
            NameClassPair nameClassPair = (NameClassPair) enumeration.nextElement();
            String name = nameClassPair.getName();
            assertFalse(result.containsKey(name));
            result.put(name, nameClassPair.getClassName());
        }
        return result;
    }

    private Map toListBindingResults(NamingEnumeration enumeration) {
        Map result = new HashMap();
        while (enumeration.hasMoreElements()) {
            Binding binding = (Binding) enumeration.nextElement();
            String name = binding.getName();
            assertFalse(result.containsKey(name));
            result.put(name, binding.getObject());
        }
        return result;
    }

    public void testNestedLookup() throws Exception {
        Map map = new HashMap();
        map.put("a/b/c/d/e/string", STRING_VAL);

        Context context = EnterpriseNamingContext.createEnterpriseNamingContext(map);

        assertEquals(STRING_VAL, context.lookup("a/b/c/d/e/string"));
        Context nestedContext = (Context) context.lookup("a/b/c");
        assertNotNull(nestedContext);
        assertEquals("a/b/c", nestedContext.getNameInNamespace());
        assertEquals(STRING_VAL, nestedContext.lookup("d/e/string"));
    }

    public void testNestedList() throws Exception {
        Map map = new HashMap();
        map.put("a/b/c/d/e/one", new Integer(1));
        map.put("a/b/c/d/e/two", new Integer(2));
        map.put("a/b/c/d/e/three", new Integer(3));

        Context context = EnterpriseNamingContext.createEnterpriseNamingContext(map);

        Map result = toListResults(context.list("a/b/c/d/e"));
        assertEquals(3, result.size());
        assertEquals(Integer.class.getName(), result.get("one"));
        assertEquals(Integer.class.getName(), result.get("two"));
        assertEquals(Integer.class.getName(), result.get("three"));

        result = toListBindingResults(context.listBindings("a/b/c/d/e"));
        assertEquals(3, result.size());
        assertEquals(new Integer(1), result.get("one"));
        assertEquals(new Integer(2), result.get("two"));
        assertEquals(new Integer(3), result.get("three"));

        Context nestedContext = (Context) context.lookup("a/b/c");
        assertNotNull(nestedContext);
        assertEquals("a/b/c", nestedContext.getNameInNamespace());

        result = toListResults(nestedContext.list("d/e"));
        assertEquals(3, result.size());
        assertEquals(Integer.class.getName(), result.get("one"));
        assertEquals(Integer.class.getName(), result.get("two"));
        assertEquals(Integer.class.getName(), result.get("three"));

        result = toListBindingResults(nestedContext.listBindings("d/e"));
        assertEquals(3, result.size());
        assertEquals(new Integer(1), result.get("one"));
        assertEquals(new Integer(2), result.get("two"));
        assertEquals(new Integer(3), result.get("three"));
    }
}
