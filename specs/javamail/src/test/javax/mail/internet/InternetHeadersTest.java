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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.mail.internet;

import java.io.ByteArrayInputStream;

import javax.mail.MessagingException;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class InternetHeadersTest extends TestCase {
    private InternetHeaders headers;

    public void testLoadSingleHeader() throws MessagingException {
        String stream = "content-type: text/plain\r\n\r\n";
        headers.load(new ByteArrayInputStream(stream.getBytes()));
        String[] header = headers.getHeader("content-type");
        assertNotNull(header);
        assertEquals("text/plain", header[0]);
    }

    protected void setUp() throws Exception {
        headers = new InternetHeaders();
    }
}
