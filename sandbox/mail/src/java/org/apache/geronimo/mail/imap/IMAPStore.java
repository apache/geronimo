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

package org.apache.geronimo.mail.imap;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;

import org.apache.geronimo.mail.AbstractStore;

/**
 * Represents the Apache implementation of the IMAP Store.
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:14 $
 */
public class IMAPStore extends AbstractStore {

    /**
     * Create a new IMAP store
     * @param session the session to be associated with
     * @param name the name of this store
     */
    public IMAPStore(Session session, URLName name) {
        super(session, name);
    }

    /* (non-Javadoc)
     * @see javax.mail.Store#getFolder(javax.mail.URLName)
     */
    public Folder getFolder(URLName name) throws MessagingException {
        return new IMAPFolder(this,name);
    }

}
