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
 * @version $Revision: 1.1 $ $Date: 2003/08/28 13:06:49 $
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
