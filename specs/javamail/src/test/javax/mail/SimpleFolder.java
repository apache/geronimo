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
package javax.mail;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
/**
 * @version $Revision: 1.1 $ $Date: 2003/09/04 01:31:41 $
 */
public class SimpleFolder extends Folder {
    private static final Message[] MESSAGE_ARRAY = new Message[0];
    private List _messages = new LinkedList();
    private String _name;
    public SimpleFolder(Store store) {
        this(store, "SimpleFolder");
    }
    SimpleFolder(Store store, String name) {
        super(store);
        _name = name;
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#appendMessages(javax.mail.Message[])
     */
    public void appendMessages(Message[] messages) throws MessagingException {
        for (int i = 0; i < messages.length; i++) {
            Message message = messages[i];
            _messages.add(message);
        }
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#close(boolean)
     */
    public void close(boolean expunge) throws MessagingException {
        if (expunge) {
            expunge();
        }
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#create(int)
     */
    public boolean create(int type) throws MessagingException {
        if (type == HOLDS_MESSAGES) {
            return true;
        } else {
            throw new MessagingException("Cannot create folders that hold folders");
        }
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#delete(boolean)
     */
    public boolean delete(boolean recurse) throws MessagingException {
        _messages = new LinkedList();
        return true;
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#exists()
     */
    public boolean exists() throws MessagingException {
        return true;
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#expunge()
     */
    public Message[] expunge() throws MessagingException {
        Iterator it = _messages.iterator();
        List result = new LinkedList();
        while (it.hasNext()) {
            Message message = (Message) it.next();
            if (message.isSet(Flags.Flag.DELETED)) {
                it.remove();
                result.add(message);
            }
        }
        // run through and renumber the messages
        for (int i = 0; i < _messages.size(); i++) {
            Message message = (Message) _messages.get(i);
            message.setMessageNumber(i);
        }
        return (Message[]) result.toArray(MESSAGE_ARRAY);
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#getFolder(java.lang.String)
     */
    public Folder getFolder(String name) throws MessagingException {
        return null;
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#getFullName()
     */
    public String getFullName() {
        return getName();
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#getMessage(int)
     */
    public Message getMessage(int id) throws MessagingException {
        return (Message) _messages.get(id);
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#getMessageCount()
     */
    public int getMessageCount() throws MessagingException {
        return _messages.size();
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#getName()
     */
    public String getName() {
        return _name;
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#getParent()
     */
    public Folder getParent() throws MessagingException {
        return null;
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#getPermanentFlags()
     */
    public Flags getPermanentFlags() {
        return null;
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#getSeparator()
     */
    public char getSeparator() throws MessagingException {
        return '/';
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#getType()
     */
    public int getType() throws MessagingException {
        return HOLDS_MESSAGES;
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#hasNewMessages()
     */
    public boolean hasNewMessages() throws MessagingException {
        return false;
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#isOpen()
     */
    public boolean isOpen() {
        return true;
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#list(java.lang.String)
     */
    public Folder[] list(String pattern) throws MessagingException {
        return null;
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#open(int)
     */
    public void open(int mode) throws MessagingException {
        if (mode != HOLDS_MESSAGES) {
            throw new MessagingException("SimpleFolder can only be opened with HOLDS_MESSAGES");
        }
    }
    /* (non-Javadoc)
     * @see javax.mail.Folder#renameTo(javax.mail.Folder)
     */
    public boolean renameTo(Folder newName) throws MessagingException {
        _name = newName.getName();
        return true;
    }
}
