/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.mail;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.2 $ $Date: 2003/08/18 20:53:05 $
 */
public class URLNameTest extends TestCase {
    public URLNameTest(String name) {
        super(name);
    }

    public void testURLNameString() {
        String s;
        URLName name;

        s = "http://www.apache.org";
        name = new URLName(s);
        assertEquals(s, name.toString());
        assertEquals("http", name.getProtocol());
        assertEquals("www.apache.org", name.getHost());
        assertEquals(-1, name.getPort());
        assertNull(name.getFile());
        assertNull(name.getRef());
        assertNull(name.getUsername());
        assertNull(name.getPassword());
        try {
            assertEquals(new URL(s), name.getURL());
        } catch (MalformedURLException e) {
            fail();
        }

        s = "http://www.apache.org/file/file1#ref";
        name = new URLName(s);
        assertEquals(s, name.toString());
        assertEquals("http", name.getProtocol());
        assertEquals("www.apache.org", name.getHost());
        assertEquals(-1, name.getPort());
        assertEquals("/file/file1", name.getFile());
        assertEquals("ref", name.getRef());
        assertNull(name.getUsername());
        assertNull(name.getPassword());
        try {
            assertEquals(new URL(s), name.getURL());
        } catch (MalformedURLException e) {
            fail();
        }

        s = "http://www.apache.org/file/";
        name = new URLName(s);
        assertEquals(s, name.toString());
        assertEquals("http", name.getProtocol());
        assertEquals("www.apache.org", name.getHost());
        assertEquals(-1, name.getPort());
        assertEquals("/file/", name.getFile());
        assertNull(name.getRef());
        assertNull(name.getUsername());
        assertNull(name.getPassword());
        try {
            assertEquals(new URL(s), name.getURL());
        } catch (MalformedURLException e) {
            fail();
        }

        s = "http://john@www.apache.org/file/";
        name = new URLName(s);
        assertEquals(s, name.toString());
        assertEquals("http", name.getProtocol());
        assertEquals("www.apache.org", name.getHost());
        assertEquals(-1, name.getPort());
        assertEquals("/file/", name.getFile());
        assertNull(name.getRef());
        assertEquals("john", name.getUsername());
        assertNull(name.getPassword());
        try {
            assertEquals(new URL(s), name.getURL());
        } catch (MalformedURLException e) {
            fail();
        }

        s = "http://john:doe@www.apache.org/file/";
        name = new URLName(s);
        assertEquals(s, name.toString());
        assertEquals("http", name.getProtocol());
        assertEquals("www.apache.org", name.getHost());
        assertEquals(-1, name.getPort());
        assertEquals("/file/", name.getFile());
        assertNull(name.getRef());
        assertEquals("john", name.getUsername());
        assertEquals("doe", name.getPassword());
        try {
            assertEquals(new URL(s), name.getURL());
        } catch (MalformedURLException e) {
            fail();
        }

        s = "file/file2";
        name = new URLName(s);
        assertNull(name.getProtocol());
        assertNull(name.getHost());
        assertEquals(-1, name.getPort());
        assertEquals("file/file2", name.getFile());
        assertNull(name.getRef());
        assertNull(name.getUsername());
        assertNull(name.getPassword());
        try {
            name.getURL();
            fail();
        } catch (MalformedURLException e) {
            // OK
        }

        name = new URLName((String) null);
        assertNull( name.getProtocol());
        assertNull(name.getHost());
        assertEquals(-1, name.getPort());
        assertNull(name.getFile());
        assertNull(name.getRef());
        assertNull(name.getUsername());
        assertNull(name.getPassword());
        try {
            name.getURL();
            fail();
        } catch (MalformedURLException e) {
            // OK
        }

        name = new URLName("");
        assertNull( name.getProtocol());
        assertNull(name.getHost());
        assertEquals(-1, name.getPort());
        assertNull(name.getFile());
        assertNull(name.getRef());
        assertNull(name.getUsername());
        assertNull(name.getPassword());
        try {
            name.getURL();
            fail();
        } catch (MalformedURLException e) {
            // OK
        }
    }

    public void testURLNameAll() {
        URLName name;
        name = new URLName(null, null, -1, null, null, null);
        assertNull(name.getProtocol());
        assertNull(name.getHost());
        assertEquals(-1, name.getPort());
        assertNull(name.getFile());
        assertNull(name.getRef());
        assertNull(name.getUsername());
        assertNull(name.getPassword());
        try {
            name.getURL();
            fail();
        } catch (MalformedURLException e) {
            // OK
        }

        name = new URLName("", "", -1, "", "", "");
        assertNull(name.getProtocol());
        assertNull(name.getHost());
        assertEquals(-1, name.getPort());
        assertNull(name.getFile());
        assertNull(name.getRef());
        assertNull(name.getUsername());
        assertNull(name.getPassword());
        try {
            name.getURL();
            fail();
        } catch (MalformedURLException e) {
            // OK
        }

        name = new URLName("http", "www.apache.org", -1, null, null, null);
        assertEquals("http://www.apache.org", name.toString());
        assertEquals("http", name.getProtocol());
        assertEquals("www.apache.org", name.getHost());
        assertEquals(-1, name.getPort());
        assertNull(name.getFile());
        assertNull(name.getRef());
        assertNull(name.getUsername());
        assertNull(name.getPassword());
        try {
            assertEquals(new URL("http://www.apache.org"), name.getURL());
        } catch (MalformedURLException e) {
            fail();
        }

        name = new URLName("http", "www.apache.org", 8080, "", "", "");
        assertEquals("http://www.apache.org:8080", name.toString());
        assertEquals("http", name.getProtocol());
        assertEquals("www.apache.org", name.getHost());
        assertEquals(8080, name.getPort());
        assertNull(name.getFile());
        assertNull(name.getRef());
        assertNull(name.getUsername());
        assertNull(name.getPassword());
        try {
            assertEquals(new URL("http://www.apache.org:8080"), name.getURL());
        } catch (MalformedURLException e) {
            fail();
        }

        name = new URLName("http", "www.apache.org", -1, "/file/file2", "", "");
        assertEquals("http://www.apache.org/file/file2", name.toString());
        assertEquals("http", name.getProtocol());
        assertEquals("www.apache.org", name.getHost());
        assertEquals(-1, name.getPort());
        assertEquals("/file/file2", name.getFile());
        assertNull(name.getRef());
        assertNull(name.getUsername());
        assertNull(name.getPassword());
        try {
            assertEquals(new URL("http://www.apache.org/file/file2"), name.getURL());
        } catch (MalformedURLException e) {
            fail();
        }

        name = new URLName("http", "www.apache.org", -1, "/file/file2", "john", "");
        assertEquals("http://john@www.apache.org/file/file2", name.toString());
        assertEquals("http", name.getProtocol());
        assertEquals("www.apache.org", name.getHost());
        assertEquals(-1, name.getPort());
        assertEquals("/file/file2", name.getFile());
        assertNull(name.getRef());
        assertEquals("john", name.getUsername());
        assertNull(name.getPassword());
        try {
            assertEquals(new URL("http://john@www.apache.org/file/file2"), name.getURL());
        } catch (MalformedURLException e) {
            fail();
        }

        name = new URLName("http", "www.apache.org", -1, "/file/file2", "john", "doe");
        assertEquals("http://john:doe@www.apache.org/file/file2", name.toString());
        assertEquals("http", name.getProtocol());
        assertEquals("www.apache.org", name.getHost());
        assertEquals(-1, name.getPort());
        assertEquals("/file/file2", name.getFile());
        assertNull(name.getRef());
        assertEquals("john", name.getUsername());
        assertEquals("doe", name.getPassword());
        try {
            assertEquals(new URL("http://john:doe@www.apache.org/file/file2"), name.getURL());
        } catch (MalformedURLException e) {
            fail();
        }

        name = new URLName("http", "www.apache.org", -1, "/file/file2", "", "doe");
        assertEquals("http://www.apache.org/file/file2", name.toString());
        assertEquals("http", name.getProtocol());
        assertEquals("www.apache.org", name.getHost());
        assertEquals(-1, name.getPort());
        assertEquals("/file/file2", name.getFile());
        assertNull(name.getRef());
        assertNull(name.getUsername());
        assertNull(name.getPassword());
        try {
            assertEquals(new URL("http://www.apache.org/file/file2"), name.getURL());
        } catch (MalformedURLException e) {
            fail();
        }
    }

    public void testURLNameURL() throws MalformedURLException {
        URL url;
        URLName name;

        url = new URL("http://www.apache.org");
        name = new URLName(url);
        assertEquals("http", name.getProtocol());
        assertEquals("www.apache.org", name.getHost());
        assertEquals(-1, name.getPort());
        assertNull(name.getFile());
        assertNull(name.getRef());
        assertNull(name.getUsername());
        assertNull(name.getPassword());
        try {
            assertEquals(url, name.getURL());
        } catch (MalformedURLException e) {
            fail();
        }
    }

    public void testEquals() throws MalformedURLException {
        URLName name1 = new URLName("http://www.apache.org");
        assertEquals(name1, new URLName("http://www.apache.org"));
        assertEquals(name1, new URLName(new URL("http://www.apache.org")));
        assertEquals(name1, new URLName("http", "www.apache.org", -1, null, null, null));
        assertEquals(name1, new URLName("http://www.apache.org#foo")); // wierd but ref is not part of the equals contract
        assertTrue(!name1.equals(new URLName("http://www.apache.org:8080")));
        assertTrue(!name1.equals(new URLName("http://cvs.apache.org")));
        assertTrue(!name1.equals(new URLName("https://www.apache.org")));

        name1 = new URLName("http://john:doe@www.apache.org");
        assertEquals(name1, new URLName(new URL("http://john:doe@www.apache.org")));
        assertEquals(name1, new URLName("http", "www.apache.org", -1, null, "john", "doe"));
        assertTrue(!name1.equals(new URLName("http://john:xxx@www.apache.org")));
        assertTrue(!name1.equals(new URLName("http://xxx:doe@www.apache.org")));
        assertTrue(!name1.equals(new URLName("http://www.apache.org")));

        assertEquals(new URLName("http://john@www.apache.org"), new URLName("http", "www.apache.org", -1, null, "john", null));
        assertEquals(new URLName("http://www.apache.org"), new URLName("http", "www.apache.org", -1, null, null, "doe"));
    }

}
