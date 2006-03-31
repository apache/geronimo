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

package org.apache.geronimo.javamail.store.nntp;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.IllegalWriteException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.Session;
import javax.mail.event.ConnectionEvent;

import org.apache.geronimo.javamail.transport.nntp.NNTPConnection;

/**
 * The base NNTP implementation of the javax.mail.Folder This is a base class
 * for both the Root NNTP server and each NNTP group folder.
 * 
 * @see javax.mail.Folder
 * 
 * @version $Rev$
 */
public class NNTPFolder extends Folder {

    // our active connection.
    protected NNTPConnection connection;

    // our attached session
    protected Session session;

    // the name of this folder (either the name of the server for the root or
    // the news group name).
    protected String name;

    // the "full" name of the folder. For the root folder, this is the name
    // returned by the connection
    // welcome string. Otherwise, this is the same as the name.
    protected String fullName;

    // the parent folder. For the root folder, this is null. For a group folder,
    // this is the root.
    protected Folder parent;

    // the folder open state
    protected boolean folderOpen = false;

    // the folder message count. For the root folder, this is always 0.
    protected int messageCount = 0;

    // the persistent flags we save in the store (basically just the SEEN flag).
    protected Flags permanentFlags;

    /**
     * Super class constructor the base NNTPFolder class.
     * 
     * @param store
     *            The javamail store this folder is attached to.
     */
    protected NNTPFolder(NNTPStore store) {
        super(store);
        // get the active connection from the store...all commands are sent
        // there
        this.connection = store.getConnection();
        this.session = store.getSession();

        // set up our permanent flags bit.
        permanentFlags = new Flags();
        permanentFlags.add(Flags.Flag.SEEN);
    }

    /**
     * Retrieve the folder name.
     * 
     * @return The folder's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieve the folder's full name (including hierarchy information). NNTP
     * folders are flat, so the full name is generally the same as the name.
     * 
     * @return The full name value.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the parent folder for this folder. Returns null if this is the
     * root folder.
     */
    public Folder getParent() throws MessagingException {
        return parent;
    }

    /**
     * Indicated whether the folder "exists" or not. Existance in this context
     * indicates that the group still exists on the server.
     * 
     * @return
     * @exception MessagingException
     */
    public boolean exists() throws MessagingException {
        // by default, return true. This is really only the case for the root.
        // The group folder will
        // need to override this.
        return true;
    }

    /**
     * List the subfolders. For group folders, this is a meaningless so we throw
     * a MethodNotSupportedException.
     * 
     * @param pattern
     *            The folder pattern string.
     * 
     * @return Never returns.
     * @exception MessagingException
     */
    public Folder[] list(String pattern) throws MessagingException {
        throw new MethodNotSupportedException("NNTP group folders cannot contain sub folders");
    }

    /**
     * Retrieve the list of subscribed folders that match the given pattern
     * string.
     * 
     * @param pattern
     *            The pattern string used for the matching
     * 
     * @return An array of matching folders from the subscribed list.
     */
    public Folder[] listSubscribed(String pattern) throws MessagingException {
        throw new MethodNotSupportedException("NNTP group folders cannot contain sub folders");
    }

    /**
     * No sub folders, hence there is no notion of a seperator. We return a null
     * character (consistent with what Sun returns for POP3 folders).
     */
    public char getSeparator() throws MessagingException {
        return '\0';
    }

    /**
     * Return whether this folder can hold just messages or also subfolders.
     * Only the root folder can hold other folders, so it will need to override.
     * 
     * @return Either Folder.HOLDS_MESSAGES or Folder.HOLDS_FOLDERS.
     * @exception MessagingException
     */
    public int getType() throws MessagingException {
        return HOLDS_MESSAGES;
    }

    /**
     * Create a new folder. NNTP folders are read only, so this is a nop.
     * 
     * @param type
     *            The type of folder.
     * 
     * @return Not support, throws an exception.
     * @exception MessagingException
     */
    public boolean create(int type) throws MessagingException {
        throw new MethodNotSupportedException("Sub folders cannot be created in NNTP");
    }

    /**
     * Check for new messages. We always return false for the root folder. The
     * group folders will need to override.
     * 
     * @return Always returns false.
     * @exception MessagingException
     */
    public boolean hasNewMessages() throws MessagingException {
        return false;
    }

    /**
     * Get a named subfolder from this folder. This only has meaning from the
     * root NNTP folder.
     * 
     * @param name
     *            The requested name.
     * 
     * @return If the folder exists, returns a Folder object representing the
     *         named folder.
     * @exception MessagingException
     */
    public Folder getFolder(String name) throws MessagingException {
        throw new MethodNotSupportedException("NNTP Group folders do not support sub folders");
    }

    /**
     * Delete a folder. This is not supported for NNTP.
     * 
     * @param recurse
     *            The recusion flag.
     * 
     * @return Never returns.
     * @exception MessagingException
     */
    public boolean delete(boolean recurse) throws MessagingException {
        throw new MethodNotSupportedException("Deleting of NNTP folders is not supported");
    }

    /**
     * Rename a folder. Not supported for NNTP folders.
     * 
     * @param f
     *            The new folder specifying the rename location.
     * 
     * @return
     * @exception MessagingException
     */
    public boolean renameTo(Folder f) throws MessagingException {
        throw new MethodNotSupportedException("Renaming of NNTP folders is not supported.");
    }

    /**
     * @see javax.mail.Folder#open(int)
     */
    public void open(int mode) throws MessagingException {

        // we don't support READ_WRITE mode, so don't allow opening in that
        // mode.
        if (mode == READ_WRITE) {
            throw new IllegalWriteException("Newsgroup folders cannot be opened read/write");
        }

        // an only be performed on a closed folder
        checkClosed();

        this.mode = mode;

        // perform folder type-specific open actions.
        openFolder();

        folderOpen = true;

        notifyConnectionListeners(ConnectionEvent.OPENED);
    }

    /**
     * Perform folder type-specific open actions. The default action is to do
     * nothing.
     * 
     * @exception MessagingException
     */
    protected void openFolder() throws MessagingException {
    }

    /**
     * Peform folder type-specific close actions. The default action is to do
     * nothing.
     * 
     * @exception MessagingException
     */
    protected void closeFolder() throws MessagingException {
    }

    /**
     * Close the folder. Cleans up resources, potentially expunges messages
     * marked for deletion, and sends an event notification.
     * 
     * @param expunge
     *            The expunge flag, which is ignored for NNTP folders.
     * 
     * @exception MessagingException
     */
    public void close(boolean expunge) throws MessagingException {
        // Can only be performed on an open folder
        checkOpen();

        // give the subclasses an opportunity to do some cleanup
        closeFolder();

        folderOpen = false;
        notifyConnectionListeners(ConnectionEvent.CLOSED);
    }

    /**
     * Tests the open status of the folder.
     * 
     * @return true if the folder is open, false otherwise.
     */
    public boolean isOpen() {
        return folderOpen;
    }

    /**
     * Get the permanentFlags
     * 
     * @return The set of permanent flags we support (only SEEN).
     */
    public Flags getPermanentFlags() {
        // we need a copy of our master set.
        return new Flags(permanentFlags);
    }

    /**
     * Get the count of messages in this folder.
     * 
     * @return The message count.
     * @exception MessagingException
     */
    public int getMessageCount() throws MessagingException {
        return messageCount;
    }

    /**
     * Checks wether the message is in cache, if not will create a new message
     * object and return it.
     * 
     * @see javax.mail.Folder#getMessage(int)
     */
    public Message getMessage(int msgNum) throws MessagingException {
        // for the base, we just throw an exception.
        throw new MethodNotSupportedException("Root NNTP folder does not contain messages");
    }

    /**
     * Append messages to a folder. NNTP folders are read only, so this is not
     * supported.
     * 
     * @param msgs
     *            The list of messages to append.
     * 
     * @exception MessagingException
     */
    public void appendMessages(Message[] msgs) throws MessagingException {
        throw new MethodNotSupportedException("Root NNTP folder does not contain messages");

    }

    /**
     * Expunge messages marked for deletion and return a list of the Messages.
     * Not supported for NNTP.
     * 
     * @return Never returns.
     * @exception MessagingException
     */
    public Message[] expunge() throws MessagingException {
        throw new MethodNotSupportedException("Root NNTP folder does not contain messages");
    }

    /**
     * Below is a list of convenience methods that avoid repeated checking for a
     * value and throwing an exception
     */

    /** Ensure the folder is open */
    protected void checkOpen() throws IllegalStateException {
        if (!folderOpen) {
            throw new IllegalStateException("Folder is not Open");
        }
    }

    /** Ensure the folder is not open */
    protected void checkClosed() throws IllegalStateException {
        if (folderOpen) {
            throw new IllegalStateException("Folder is Open");
        }
    }

    /**
     * @see javax.mail.Folder#notifyMessageChangedListeners(int,
     *      javax.mail.Message)
     * 
     * this method is protected and cannot be used outside of Folder, therefore
     * had to explicitly expose it via a method in NNTPFolder, so that
     * NNTPMessage has access to it
     * 
     * Bad design on the part of the Java Mail API.
     */
    public void notifyMessageChangedListeners(int type, Message m) {
        super.notifyMessageChangedListeners(type, m);
    }

    /**
     * Retrieve the subscribed status for a folder. This default implementation
     * just returns false (which is true for the root folder).
     * 
     * @return Always returns true.
     */
    public boolean isSubscribed() {
        return false;
    }

    /**
     * Set the subscribed status for a folder.
     * 
     * @param flag
     *            The new subscribed status.
     * 
     * @exception MessagingException
     */
    public void setSubscribed(boolean flag) throws MessagingException {
        throw new MessagingException("Root NNTP folder cannot be subscribed to");
    }

    /**
     * Test if a given article number is marked as SEEN.
     * 
     * @param article
     *            The target article number.
     * 
     * @return The articles current seen status.
     */
    public boolean isSeen(int article) {
        return false;
    }

    /**
     * Set the SEEN status for an article.
     * 
     * @param article
     *            The target article.
     * @param flag
     *            The new seen setting.
     * 
     * @exception MessagingException
     */
    public void setSeen(int article, boolean flag) throws MessagingException {
        throw new MessagingException("Root NNTP folder does not contain articles");
    }

}
