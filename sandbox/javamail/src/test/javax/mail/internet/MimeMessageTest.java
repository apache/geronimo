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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:11 $
 */
public class MimeMessageTest extends TestCase {
    public void testNothing() {
    }
    /*
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
    */
}
