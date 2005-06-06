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

import java.io.IOException;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class MimeMessageTest extends TestCase {
    private CommandMap defaultMap;
    private Session session;

    public void testWriteTo() throws MessagingException, IOException {
        MimeMessage msg = new MimeMessage(session);
        msg.setSender(new InternetAddress("foo"));
        MimeMultipart mp = new MimeMultipart();
        MimeBodyPart part1 = new MimeBodyPart();
        part1.setHeader("foo", "bar");
        part1.setContent("Hello World", "text/plain");
        mp.addBodyPart(part1);
        MimeBodyPart part2 = new MimeBodyPart();
        part2.setContent("Hello Again", "text/plain");
        mp.addBodyPart(part2);
        msg.setContent(mp);
        msg.writeTo(System.out);
    }

    protected void setUp() throws Exception {
        defaultMap = CommandMap.getDefaultCommandMap();
        MailcapCommandMap myMap = new MailcapCommandMap();
        myMap.addMailcap("text/plain;;    x-java-content-handler=" + MimeMultipartTest.DummyTextHandler.class.getName());
        myMap.addMailcap("multipart/*;;    x-java-content-handler=" + MimeMultipartTest.DummyMultipartHandler.class.getName());
        CommandMap.setDefaultCommandMap(myMap);
        session = Session.getDefaultInstance(new Properties());
    }

    protected void tearDown() throws Exception {
        CommandMap.setDefaultCommandMap(defaultMap);
    }
}
