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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.1 $ $Date: 2003/09/04 01:31:41 $
 */
public class MimeMessageTest extends TestCase {
    private static final String EOL = "\r\n";
    private static final String users =
        "Geronimo Users <geronimo-user@apache.org>";
    private static final String developers =
        "Geronimo Developers <geronimo-dev@apache.org>";
    private static final String announce =
        "Geronimo Team <geronimo-announce@apache.org>";

    public void testMimeMessage() throws MessagingException {
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
        buffer.append("Bcc: " + announce);
        buffer.append(EOL);
        buffer.append(EOL);
        buffer.append("Hello World");
        byte[] data = buffer.toString().getBytes();
        InputStream in = new ByteArrayInputStream(data);
        MimeMessage message = new MimeMessage(null, in);
        List to =
            Arrays.asList(message.getRecipients(Message.RecipientType.TO));
        assertEquals(2, to.size());
        assertTrue(to.contains(new InternetAddress(users)));
        assertTrue(to.contains(new InternetAddress(developers)));
        List cc =
            Arrays.asList(message.getRecipients(Message.RecipientType.CC));
        assertEquals(0, cc.size());
        List bcc =
            Arrays.asList(message.getRecipients(Message.RecipientType.BCC));
        assertEquals(1, bcc.size());
        assertTrue(bcc.contains(new InternetAddress(announce)));
        List all = Arrays.asList(message.getAllRecipients());
        assertEquals(3, all.size());
        assertTrue(all.contains(new InternetAddress(announce)));
        assertTrue(all.contains(new InternetAddress(users)));
        assertTrue(all.contains(new InternetAddress(developers)));
    }
    public void testSetters() throws MessagingException, IOException {
        MimeMessage message = new MimeMessage(null, 1);
        message.setContent("Hello world", "text/plain");
        message.setContentID("Test message @ test.com");
        //message.setContentLanguage(new String[] { "en" });
        message.setContentMD5("md5hash");
        message.setDescription("A test message");
        message.setDisposition(Part.INLINE);
        message.setFileName("file.txt");
        message.setFrom(new InternetAddress(users));
        message.setRecipient(
            Message.RecipientType.TO,
            new InternetAddress(developers));
        message.setSubject("What is the first program you write?");
        Date sent = new Date();
        message.setSentDate(sent);
        assertEquals("Hello world", message.getContent());
        assertEquals("Test message @ test.com", message.getContentID());
        assertEquals("md5hash", message.getContentMD5());
        assertEquals("A test message", message.getDescription());
        assertEquals("inline", message.getDisposition());
        assertEquals("file.txt", message.getFileName());
        //        assertEquals("en",message.getContentLanguage()[0]);
        assertEquals(
            "inline;filename=file.txt",
            message.getHeader("Content-Disposition", null));
        assertEquals(new InternetAddress(users), message.getFrom()[0]);
        assertEquals(
            new InternetAddress(developers),
            message.getRecipients(Message.RecipientType.TO)[0]);
            // Cannot use 'equals' testing becaues new Date() contains millisecond accuracy, 
            // whereas the one from the MessageContext has been encoded to a string and parsed
        assertEquals(sent.toString(),message.getSentDate().toString());
    }
}
