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
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:30 $
 */
public class ContentDispositionTest extends TestCase {

    public ContentDispositionTest(String name) {
        super(name);
    }
    
    public void testContentDisposition() throws ParseException {
        ContentDisposition c;
        c = new ContentDisposition();
        assertNotNull(c.getParameterList());
        assertNull(c.getParameterList().get("nothing"));
        assertNull(c.getDisposition());
        assertNull(c.toString());
        c.setDisposition("inline");
        assertEquals("inline",c.getDisposition());
        c.setParameter("file","file.txt");
        assertEquals("file.txt",c.getParameterList().get("file"));
        assertEquals("inline;file=file.txt",c.toString());
        c = new ContentDisposition("inline");
        assertEquals(0,c.getParameterList().size());
        assertEquals("inline",c.getDisposition());
        c = new ContentDisposition("inline",new ParameterList("charset=us-ascii;content-type=text/plain"));
        assertEquals("inline",c.getDisposition());
        assertEquals("us-ascii",c.getParameter("charset"));
        assertEquals("text/plain",c.getParameter("content-type"));
        c = new ContentDisposition("attachment;content-type=text/html;charset=UTF-8");
        assertEquals("attachment",c.getDisposition());
        assertEquals("UTF-8",c.getParameter("charset"));
        assertEquals("text/html",c.getParameter("content-type"));
    }

}
