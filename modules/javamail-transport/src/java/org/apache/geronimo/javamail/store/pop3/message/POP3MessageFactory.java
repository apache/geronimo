/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

package org.apache.geronimo.javamail.store.pop3.message;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.geronimo.javamail.store.pop3.POP3Connection;
import org.apache.geronimo.javamail.store.pop3.POP3Folder;

/**
 * Fctory class to create POP3Messages based on the fetch profile
 * 
 * @version $Rev$ $Date$
 */
public final class POP3MessageFactory {

    /**
     * Creates a basic method with no items, the items will be loaded on demand
     * 
     * @param folder
     * @param session
     * @param pop3Con
     * @param msgNum
     * @return
     */
    public static Message createMessage(POP3Folder folder, Session session, POP3Connection pop3Con, int msgNum) {
        return new POP3Message(folder, msgNum, session, pop3Con);
    }

    /**
     * Created in response to <cpde>FetchProfile.ENVELOPE</code>
     */
    public static Message createMessageWithEvelope(POP3Message msg) throws MessagingException {
        msg.getAllHeaders();
        msg.getSender();
        msg.getSentDate();
        msg.getSubject();
        msg.getReplyTo();
        msg.getReceivedDate();
        msg.getRecipients(RecipientType.TO);

        return msg;
    }

    /**
     * Created in response to <code>FetchProfile.CONTENT_INFO</code>
     */
    public static Message createMessageWithContentInfo(POP3Message msg) throws MessagingException {
        msg.getContentType();
        msg.getDisposition();
        msg.getDescription();
        msg.getSize();
        msg.getLineCount();

        return msg;
    }

    /**
     * Created in response to <code>FetchProfile.FLAGS</code>
     */
    public static Message createMessageWithFlags(POP3Message msg) throws MessagingException {
        msg.getFlags();
        return msg;
    }

}
