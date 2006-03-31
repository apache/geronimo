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

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;

import org.apache.geronimo.javamail.store.pop3.POP3Connection;

/**
 * light-weight Message object will be created in response to
 * FetchProfile.CONTENT_INFO other details will be filled on demand *
 * 
 * @version $Rev$ $Date$
 * 
 */

public class POP3MessageWithContentInfo extends POP3Message {

    public POP3MessageWithContentInfo(Folder folder, int msgnum, Session session, POP3Connection pop3Con)
            throws MessagingException {
        super(folder, msgnum, null, pop3Con);
        this.getContentType();
        this.getDisposition();
        this.getDescription();
        this.getSize();
        this.getLineCount();
    }

}
