/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.kernel.classloader;

import java.net.URL;
import java.util.Enumeration;

import junit.framework.TestCase;

/**
 * @version $Rev:$ $Date:$
 */
public class JarFileResourceFinderTest extends TestCase {

    /**
     * There are 2 "jars" with a "resource" inside.  Make sure the enumeration has exactly 2 elements and
     * that hasMoreElements() doesn't advance the iterator.
     *
     * @throws Exception
     */
    public void testResourceEnumeration() throws Exception {
        ClassLoader cl = this.getClass().getClassLoader();
        URL jar1 = cl.getResource("resourceFinderTest/jar1/");
        URL jar2 = cl.getResource("resourceFinderTest/jar2/");
        JarFileResourceFinder resourceFinder = new JarFileResourceFinder(new URL[] {jar1, jar2});
        Enumeration enumeration = resourceFinder.findResources("resource");
        assertTrue(enumeration.hasMoreElements());
        assertTrue(enumeration.hasMoreElements());
        URL resource1 = (URL) enumeration.nextElement();
        assertNotNull(resource1);
        assertTrue(enumeration.hasMoreElements());
        assertTrue(enumeration.hasMoreElements());
        URL resource2 = (URL) enumeration.nextElement();
        assertNotNull(resource2);
        assertFalse(enumeration.hasMoreElements());
    }
}
