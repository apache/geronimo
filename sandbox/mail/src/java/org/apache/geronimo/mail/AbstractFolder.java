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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.URLName;

/**
 * Apache implementation of a generic {@link Folder}. May be subclassed for optimisation for specific protocols.
 * This stores messages using an internal {@link List}, and allows messages to be cached inside the folder.
 * Note that the messages do not have to be present or fully loaded; it is possible that a message
 * will only contain the message ID and be loaded dynamically on request.
 * <p>
 * Whilst it is possible for other implementations to override {@link Folder} directly, this provides common
 * methods that allow for easier implementation. Subclasses should implement the abstract methods
 * from {@link Folder} and the <code>doXxxx()</code> that this requires in order to facilitate creation
 * of future folder types.
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:32 $
 */
public abstract class AbstractFolder extends Folder {
    /**
     * Used for converting a list into a folder
     */
    private static final Message[] MESSAGE_ARRAY = new Message[0];
    /**
     * The list that holds the messages for this folder.
     * Note that due to a list being implemented, it is not possible to have (say) message 1 cached and message 3 cached, without
     * also caching message 2. If this is changed to provide a SortedSet based on Integer keys, this may be more efficient.
     */
    // TODO Investigate migration to a SortedSet. Implementations should not assume that this is a list.
    private List _messages = new LinkedList();
    /**
     * The name of this folder.
     */
    private URLName _name;
    /**
     * Create a new folder associated with the given {@link Store}
     * @param store the {@link Store} this folder is associated with
     * @param name the name of this folder
     */
    public AbstractFolder(Store store, URLName name) {
        super(store);
        _name = name;
    }

    /**
     * Runs through the array of messages and appends them onto the folder using {@link #add(Message)}.
     * @param messages the array of messages to add.
     */
    public void appendMessages(Message[] messages) throws MessagingException {
        for (int i = 0; i < messages.length; i++) {
            Message message = messages[i];
            _messages.add(message);
        }
    }

    /**
     * Closes the folder, and if <code>expunge</code>, then automatically expunges prior to closure.
     * Afterwards calls doClose() as a hook to the underlying implementation to do the real close operation.
     */
    public void close(boolean expunge) throws MessagingException {
        if (expunge) {
            expunge();
        }
        doClose();
    }

    /* (non-Javadoc)
     * @see javax.mail.Folder#delete(boolean)
     */
    public boolean delete(boolean recurse) throws MessagingException {
        _messages = new LinkedList();
        // TODO This is implemented badly; it doesn't delete the messages from the folder!
        for (int i = 0; i < _messages.size(); i++) {
            doDelete(i);
        }
        return true;
    }

    /**
     * Implemented by the provider, to close the folder. An expunge will already have
     * been performed if required.
     * @throws MessagingException if an error occurs during closure.
     */
    protected abstract void doClose() throws MessagingException;
    /**
     * Implemented by subclasses to actually delete a message from the store. This is then removed from the 
     * folder's message cache.
     * @param id the message number to delete
     * @return the newly-created message from the store
     * @throws MessagingException in case of any error
     */
    protected abstract void doDelete(int id) throws MessagingException;

    /**
     * Implemented by subclasses to actually load a message from the store. This is then cached in
     * the itnernal folder.
     * Note that the message returned does not need to be fully loaded; it can be a placeholder with
     * the message id only and dynamically access the rest of the message when required.
     * @param id the message number to use
     * @return the newly-created message from the store
     * @throws MessagingException in case of any error
     */
    protected abstract Message doGetMessage(int id) throws MessagingException;

    /**
     * Implemented by subclasses to perform a rename operation. Called by renameTo(),
     * and if successful, the name is update.
     * @param newName the new name to use
     */
    protected abstract void doOpen(int newMode) throws MessagingException;
    /**
     * Implemented by subclasses to perform a rename operation. Called by renameTo(),
     * and if successful, the name is update.
     * @param newName the new name to use
     * @return true if the folder is renamed; false otherwise.
     */
    protected abstract boolean doRenameTo(Folder newName);

    /**
     * Renumber message with new ID after an expunge occurs.
     * Since the Message#setMessageNumber is protected, there cannot be an
     * implementation that provides it. However, subclasses that use
     * their own implementation of Message (e.g. POP3Message) can set up
     * the setMessageNumber to be protected, and thus available in the
     * local package. This method implementation is likely to look like:
     * 
     * <pre>
     *   protected void doRenumberMessageTo(Message message, int id) throws MessagingException {
     *     ((POP3Message)message).setMessageNumber(id);
     *   }
     * </pre>
     * 
     * Note that this cannot be done generically in the superclass since there is no way
     * of making the <code>setMessageNumber()</code> method visible to this class without
     * violating the JavaMail spec.
     * @param message the message to renumber
     * @param id the message id
     */
    protected abstract void doRenumberMessageTo(Message message, int id)
        throws MessagingException;

    /**
     * Runs through a list of messages, and removes them from the internal list
     * of messages. Calls 
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
            doRenumberMessageTo(message, i);
            //            message.setMessageNumber(i);
        }
        return (Message[]) result.toArray(MESSAGE_ARRAY);
    }

    /** 
     * Returns the full URLName of this folder
     * @return the full URLName of this folder
     */
    public String getFullName() {
        return _name.toString();
    }

    /* (non-Javadoc)
     * @see javax.mail.Folder#getMessage(int)
     */
    public Message getMessage(int id) throws MessagingException {
        // TODO Put in faulting to dynamically load message if not present
        return (Message) _messages.get(id);
    }

    /**
     * Returns just the name portion of the URLName
     */
    public String getName() {
        return _name.getFile();
    }

    /**
     * Uses '<code>/</code>' as the separator. This may be overriden by subclases, but is a sensible default.
     */
    public char getSeparator() throws MessagingException {
        return '/';
    }

    /**
     * Returns {@link HOLDS_MESSAGES}. Override if the server supports folders {@link HOLDS_FOLDERS}
     * instead/as well.
     */
    public int getType() throws MessagingException {
        return HOLDS_MESSAGES;
    }

    /* (non-Javadoc)
     * @see javax.mail.Folder#hasNewMessages()
     */
    public boolean hasNewMessages() throws MessagingException {
        // TODO Implement
        return false;
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
    public void open(int newMode) throws MessagingException {
        doOpen(newMode);
        mode = newMode;
        // if the open works, then do the assignment; otherwise, leave as-is
    }

    /* (non-Javadoc)
     * @see javax.mail.Folder#renameTo(javax.mail.Folder)
     */
    public boolean renameTo(Folder newName) throws MessagingException {
        if (doRenameTo(newName)) {
            _name = ((AbstractFolder) newName)._name;
            return true;
        } else {
            return false;
        }
    }

}
