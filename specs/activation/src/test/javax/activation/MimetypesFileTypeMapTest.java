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
package javax.activation;

import java.util.Map;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class MimetypesFileTypeMapTest extends TestCase {
    private MimetypesFileTypeMap typeMap;

    public void testDefault() {
        // unmapped
        assertEquals("application/octet-stream", typeMap.getContentType("x.foo"));
        // from META-INF/mimetypes.default
        assertEquals("text/html", typeMap.getContentType("x.html"));
    }

    public void testCommentRemoval() {
        typeMap.addMimeTypes(" text/foo foo #txt");
        assertEquals("text/foo", typeMap.getContentType("x.foo"));
        typeMap.addMimeTypes("#text/foo bar");
        assertEquals("text/foo", typeMap.getContentType("x.foo"));
        typeMap.addMimeTypes("text/foo #bar");
        assertEquals("text/foo", typeMap.getContentType("x.foo"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        typeMap = new MimetypesFileTypeMap();
    }
}
