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

package javax.mail.internet;
import junit.framework.TestCase;
/**
 * @version $Rev$ $Date$
 */
public class ContentTypeTest extends TestCase {
    public ContentTypeTest(String arg0) {
        super(arg0);
    }
    public void testContentType() throws ParseException {
        ContentType type;
        type = new ContentType("text/plain");
        assertEquals("text", type.getPrimaryType());
        assertEquals("plain", type.getSubType());
        assertEquals(null, type.getParameterList());
        type = new ContentType("image/audio;charset=us-ascii");
        ParameterList parameterList = type.getParameterList();
        assertEquals("image", type.getPrimaryType());
        assertEquals("audio", type.getSubType());
        assertEquals("us-ascii", parameterList.get("charset"));
    }
    public void testContentTypeStringStringParameterList() {
    }
    public void testContentTypeString() {
    }
    public void testGetPrimaryType() {
    }
    public void testGetSubType() {
    }
    public void testGetBaseType() {
    }
    public void testGetParameter() {
    }
    public void testGetParameterList() {
    }
    public void testSetPrimaryType() {
    }
    public void testSetSubType() {
    }
    public void testSetParameter() {
    }
    public void testSetParameterList() {
    }
    public void testToString() {
    }
    public void testMatchContentType() {
    }
    public void testMatchString() {
    }
}
