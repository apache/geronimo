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

package org.apache.geronimo.mail.smtp;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;

import org.apache.geronimo.mail.AbstractTransport;

/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:14 $
 */
public class SMTPTransport extends AbstractTransport {

    /**
     * @param session
     * @param name
     */
    public SMTPTransport(Session session, URLName name) {
        super(session, name);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see javax.mail.Transport#sendMessage(javax.mail.Message, javax.mail.Address[])
     */
    public void sendMessage(Message message, Address[] addresses)
        throws MessagingException {
        // TODO Auto-generated method stub

    }

}
