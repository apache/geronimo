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
 * @version $Revision: 1.1 $ $Date: 2003/08/16 01:55:49 $
 */
public class URLNameTest extends TestCase {
    private String testURL =
        "http://alex@www.thing.com:1234/jobby/jobby/jobby#splat";
    ;
    public URLNameTest(String name) {
        super(name);
    }
    public void testHashCode() {
    }
    public void testURLNameString() {
        URLName name = new URLName(testURL);
        assertEquals("http", name.getProtocol());
        assertEquals("www.thing.com", name.getHost());
        assertEquals(1234, name.getPort());
        assertEquals("/jobby/jobby/jobby", name.getFile());
        assertEquals("splat", name.getRef());
        assertEquals("alex", name.getUsername());
        name = new URLName("http://www.thing.com");
        assertEquals("http", name.getProtocol());
        assertEquals("www.thing.com", name.getHost());
        assertEquals(-1, name.getPort());
        assertEquals(null, name.getFile());
        assertEquals(null, name.getRef());
    }
    public void testURLNameAll() {
        URLName name =
            new URLName(
                "http",
                "www.jobby.com",
                99,
                "/bottles/of/beer/on/the#wall",
                "me",
                "me2");
        assertEquals("http", name.getProtocol());
        assertEquals("www.jobby.com", name.getHost());
        assertEquals(99, name.getPort());
        assertEquals("/bottles/of/beer/on/the", name.getFile());
        assertEquals("wall", name.getRef());
        assertEquals("me", name.getUsername());
        assertEquals("me2", name.getPassword());
    }
    public void testURLNameURL() throws MalformedURLException {
        URL url = new URL(testURL);
        URLName name = new URLName(url);
        assertEquals("http", name.getProtocol());
        assertEquals("www.thing.com", name.getHost());
        assertEquals(1234, name.getPort());
        assertEquals("/jobby/jobby/jobby", name.getFile());
        assertEquals("splat", name.getRef());
    }
    public void testEqualsObject() {
        URLName name = new URLName(testURL);
        URLName name2 = new URLName(testURL);
        assertEquals(name, name2);
    }
    public void testGetURL() throws MalformedURLException {
        URLName name = new URLName(testURL);
        URL url = new URL(testURL);
        assertEquals(url, name.getURL());
    }
    public void testParseString() {
        URLName name = new URLName("ftp://not.com");
        URLName name2 = new URLName(testURL);
        name.parseString(testURL);
        assertEquals(name, name2);
    }
    public void testUser() {
        URLName name = new URLName("ftp://alex@thing.com");
        assertEquals("alex", name.getUsername());
    }
}
