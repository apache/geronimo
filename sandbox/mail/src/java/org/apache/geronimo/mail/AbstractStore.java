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

package org.apache.geronimo.mail;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

/**
 * Apache implementation of a generic store. May be subclassed for optimisation for specific protocols.
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:32 $
 */
public abstract class AbstractStore extends Store {

    /**
     * @param session the {@link Session} this store is associated with
     * @param name the @{link URLName} of this store
     */
    public AbstractStore(Session session, URLName name) {
        super(session, name);
    }

    /**
     * Returns the folder called <code>INBOX</code> unless a subclass overrides it
     */
    public Folder getDefaultFolder() throws MessagingException {
        // look for folder INBOX, which is usually the default
        return getFolder("INBOX");
    }

    /**
     * Delegates the method to the new URLName class
     */
    public Folder getFolder(String name) throws MessagingException {
        return getFolder(new URLName(name));
    }

}
