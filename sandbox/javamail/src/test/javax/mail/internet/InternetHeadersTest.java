/**
 *
 * Copyright 2004 The Apache Software Foundation
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.mail.Header;
import javax.mail.MessagingException;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:11 $
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
