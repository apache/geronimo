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

package org.apache.geronimo.mail.pop3;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.URLName;

import org.apache.geronimo.mail.AbstractFolder;

/**
 * Represents an Apache implementation of a POP3 folder.
 * @version $Rev$ $Date$
 */
public class POP3Folder extends AbstractFolder {

    /**
     * Create a new POP3 folder associated with the given store
     * @param store the {@link Store} that this folder is a part of
     * @param name the name of this folder
     */
    public POP3Folder(Store store, URLName name) {
        super(store,name);
    }

    /* (non-Javadoc)
     * @see javax.mail.Folder#create(int)
     */
    public boolean create(int type) throws MessagingException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.mail.AbstractFolder#doClose()
     */
    protected void doClose() throws MessagingException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.mail.AbstractFolder#doDelete(int)
     */
    protected void doDelete(int id) throws MessagingException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.mail.AbstractFolder#doGetMessage(int)
     */
    protected Message doGetMessage(int id) throws MessagingException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.mail.AbstractFolder#doRenameTo(javax.mail.Folder)
     */
    protected boolean doRenameTo(Folder newName) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.mail.AbstractFolder#doRenumberMessageTo(javax.mail.Message, int)
     */
    protected void doRenumberMessageTo(Message message, int id) throws MessagingException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.mail.Folder#exists()
     */
    public boolean exists() throws MessagingException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.mail.Folder#getFolder(java.lang.String)
     */
    public Folder getFolder(String name) throws MessagingException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.mail.Folder#getMessageCount()
     */
    public int getMessageCount() throws MessagingException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.mail.Folder#getParent()
     */
    public Folder getParent() throws MessagingException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.mail.Folder#getPermanentFlags()
     */
    public Flags getPermanentFlags() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.mail.Folder#isOpen()
     */
    public boolean isOpen() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.mail.Folder#open(int)
     */
    protected void doOpen(int newMode) throws MessagingException {
        // TODO Auto-generated method stub
        
    }

}
