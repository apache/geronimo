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
package javax.mail.internet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.mail.Header;
import javax.mail.MessagingException;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.1 $ $Date: 2003/09/04 01:31:41 $
 */
public class InternetHeadersTest extends TestCase {

    private static final String EOL = "\r\n";
    private static final String users =
        "Geronimo Users <geronimo-user@apache.org>";
    private static final String developers =
        "Geronimo Developers <geronimo-dev@apache.org>";
    private static final String announce =
        "Geronimo Team <geronimo-announce@apache.org>";
    public void testAddHeaders() {
        InternetHeaders headers = new InternetHeaders();
        headers.addHeader("To", users);
        headers.addHeader("To", developers);
        headers.addHeader("Subject", "New release");
        headers.addHeader("From", announce);
        assertEquals("New release", headers.getHeader("subject", null));
        assertEquals(announce, headers.getHeader("FROM", null));
        assertEquals(users + "," + developers,headers.getHeader("To",","));
        
        String[] toto = headers.getHeader("tO");
        assertEquals(2, toto.length);
        assertTrue(
            (toto[0].equals(users) && toto[1].equals(developers))
                || (toto[1].equals(users) && toto[0].equals(developers)));
    }
    public void testAddHeaderLines() {
        InternetHeaders headers = new InternetHeaders();
        headers.addHeaderLine("To: " + users);
        headers.addHeaderLine("To: " + developers);
        headers.addHeaderLine("Subject: New release");
        headers.addHeaderLine("From: " + announce);
        assertEquals("New release", headers.getHeader("subject", null));
        assertEquals(announce, headers.getHeader("FROM", null));
        String[] toto = headers.getHeader("tO");
        assertEquals(2, toto.length);
        assertTrue(
            (toto[0].equals(users) && toto[1].equals(developers))
                || (toto[1].equals(users) && toto[0].equals(developers)));
    }
    public void testReadHeaders() throws MessagingException, IOException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("To: " + users);
        buffer.append(EOL);
        buffer.append("To: " + developers);
        buffer.append(EOL);
        buffer.append("Subject: New   ");
        buffer.append(EOL);
        buffer.append("   release");
        buffer.append(EOL);
        buffer.append("From: " + announce);
        buffer.append(EOL);
        buffer.append(EOL);
        buffer.append("Hello World");
        byte[] data = buffer.toString().getBytes();
        InputStream in = new ByteArrayInputStream(data);
        InternetHeaders headers = new InternetHeaders(in);
        assertEquals('H', in.read());
        assertEquals('e', in.read());
        assertEquals('l', in.read());
        assertEquals('l', in.read());
        assertEquals('o', in.read());
        assertEquals("New release", headers.getHeader("subject", null));
        assertEquals(announce, headers.getHeader("FROM", null));
        String[] toto = headers.getHeader("tO");
        assertEquals(2, toto.length);
        assertTrue(
            (toto[0].equals(users) && toto[1].equals(developers))
                || (toto[1].equals(users) && toto[0].equals(developers)));

        Enumeration enum;
        boolean to1, to2, from, subject;

        enum = headers.getAllHeaders();
        to1 = to2 = from = subject = false;
        while (enum.hasMoreElements()) {
            Header header = (Header) enum.nextElement();
            to1 =
                to1
                    || (header.getName().equals("To")
                        && header.getValue().equals(users));
            to2 =
                to2
                    || (header.getName().equals("To")
                        && header.getValue().equals(developers));
            from =
                from
                    || (header.getName().equals("From")
                        && header.getValue().equals(announce));
            subject =
                subject
                    || (header.getName().equals("Subject")
                        && header.getValue().equals("New release"));
        }
        assertTrue(to1 && to2 && from && subject);

        enum = headers.getAllHeaderLines();
        to1 = to2 = from = subject = false;
        while (enum.hasMoreElements()) {
            String line = (String) enum.nextElement();
            to1 = to1 || line.equals("To: " + users);
            to2 = to2 || line.equals("To: " + developers);
            from = from || line.equals("From: " + announce);
            subject = subject || line.equals("Subject: New release");
        }
        assertTrue(to1 && to2 && from && subject);

        String[] fromSubject = new String[] { "From", "Subject" };

        enum = headers.getMatchingHeaders(fromSubject);
        to1 = to2 = from = subject = false;
        while (enum.hasMoreElements()) {
            Header header = (Header) enum.nextElement();
            to1 =
                to1
                    || (header.getName().equals("To")
                        && header.getValue().equals(users));
            to2 =
                to2
                    || (header.getName().equals("To")
                        && header.getValue().equals(developers));
            from =
                from
                    || (header.getName().equals("From")
                        && header.getValue().equals(announce));
            subject =
                subject
                    || (header.getName().equals("Subject")
                        && header.getValue().equals("New release"));
        }
        assertTrue(!to1 && !to2 && from && subject);

        enum = headers.getMatchingHeaderLines(fromSubject);
        to1 = to2 = from = subject = false;
        while (enum.hasMoreElements()) {
            String line = (String) enum.nextElement();
            to1 = to1 || line.equals("To: " + users);
            to2 = to2 || line.equals("To: " + developers);
            from = from || line.equals("From: " + announce);
            subject = subject || line.equals("Subject: New release");
        }
        assertTrue(!to1 && !to2 && from && subject);

        enum = headers.getNonMatchingHeaders(fromSubject);
        to1 = to2 = from = subject = false;
        while (enum.hasMoreElements()) {
            Header header = (Header) enum.nextElement();
            to1 =
                to1
                    || (header.getName().equals("To")
                        && header.getValue().equals(users));
            to2 =
                to2
                    || (header.getName().equals("To")
                        && header.getValue().equals(developers));
            from =
                from
                    || (header.getName().equals("From")
                        && header.getValue().equals(announce));
            subject =
                subject
                    || (header.getName().equals("Subject")
                        && header.getValue().equals("New release"));
        }
        assertTrue(to1 && to2 && !from && !subject);

        enum = headers.getNonMatchingHeaderLines(fromSubject);
        to1 = to2 = from = subject = false;
        while (enum.hasMoreElements()) {
            String line = (String) enum.nextElement();
            to1 = to1 || line.equals("To: " + users);
            to2 = to2 || line.equals("To: " + developers);
            from = from || line.equals("From: " + announce);
            subject = subject || line.equals("Subject: New release");
        }
        assertTrue(to1 && to2 && !from && !subject);

    }

}
