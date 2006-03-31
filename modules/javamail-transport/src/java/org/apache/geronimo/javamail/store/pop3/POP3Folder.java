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

package org.apache.geronimo.javamail.store.pop3;

import java.util.Vector;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.event.ConnectionEvent;

import org.apache.geronimo.javamail.store.pop3.message.POP3Message;
import org.apache.geronimo.javamail.store.pop3.message.POP3MessageFactory;
import org.apache.geronimo.javamail.store.pop3.response.POP3ResponseFactory;
import org.apache.geronimo.javamail.store.pop3.response.POP3StatusResponse;

/**
 * The POP3 implementation of the javax.mail.Folder Note that only INBOX is
 * supported in POP3
 * <p>
 * <url>http://www.faqs.org/rfcs/rfc1939.html</url>
 * </p>
 * 
 * @see javax.mail.Folder
 * 
 * @version $Rev$ $Date$
 */
public class POP3Folder extends Folder {

    private boolean isFolderOpen = false;

    private int mode;

    private POP3Connection pop3Con;

    private int msgCount;

    private Session session;

    /**
     * Vector is synchronized so choose over the other Collection impls This is
     * initialized on open A chache will save the expensive operation of
     * retrieving the message again from the server.
     */
    private Vector msgCache;

    protected POP3Folder(Store store, URLName url) {
        super(store);
    }

    protected POP3Folder(Store store, Session session, POP3Connection pop3Con) {
        super(store);
        this.pop3Con = pop3Con;
        this.session = session;
    }

    public String getName() {
        return "INBOX";
    }

    public String getFullName() {
        return "INBOX";
    }

    /**
     * Never return "this" as the parent folder. Somebody not familliar with
     * POP3 may do something like while(getParent() != null) or something
     * simmilar which will result in an infinte loop
     */
    public Folder getParent() throws MessagingException {
        throw new MethodNotSupportedException("INBOX is the root folder");
    }

    public boolean exists() throws MessagingException {
        // INBOX always exists at the backend
        return true;
    }

    public Folder[] list(String pattern) throws MessagingException {
        throw new MethodNotSupportedException("Only INBOX is supported in POP3, no sub folders");
    }

    /**
     * No sub folders, hence there is no notion of a seperator
     */
    public char getSeparator() throws MessagingException {
        throw new MethodNotSupportedException("Only INBOX is supported in POP3, no sub folders");
    }

    public int getType() throws MessagingException {
        return HOLDS_MESSAGES;
    }

    public boolean create(int type) throws MessagingException {
        throw new MethodNotSupportedException("Only INBOX is supported in POP3, no sub folders");
    }

    public boolean hasNewMessages() throws MessagingException {
        throw new MethodNotSupportedException("POP3 doesn't support this operation");
    }

    public Folder getFolder(String name) throws MessagingException {
        throw new MethodNotSupportedException("Only INBOX is supported in POP3, no sub folders");
    }

    public boolean delete(boolean recurse) throws MessagingException {
        throw new MethodNotSupportedException("Only INBOX is supported in POP3 and INBOX cannot be deleted");
    }

    public boolean renameTo(Folder f) throws MessagingException {
        throw new MethodNotSupportedException("Only INBOX is supported in POP3 and INBOX cannot be renamed");
    }

    /**
     * @see javax.mail.Folder#open(int)
     */
    public void open(int mode) throws MessagingException {
        // Can only be performed on a closed folder
        checkClosed();

        try {

            POP3StatusResponse res = (POP3StatusResponse) POP3ResponseFactory.getStatusResponse(pop3Con
                    .sendCommand(POP3CommandFactory.getCOMMAND_STAT()));

            // I am not checking for the res == null condition as the
            // try catch block will handle it.

            this.mode = mode;
            this.isFolderOpen = true;
            this.msgCount = res.getNumMessages();
            // JavaMail API has no method in Folder to expose the total
            // size (no of bytes) of the mail drop;

            msgCache = new Vector(msgCount);
            msgCache.setSize(msgCount);

        } catch (Exception e) {
            throw new MessagingException("Unable to execute STAT command", e);
        }

        notifyConnectionListeners(ConnectionEvent.OPENED);
    }

    public void close(boolean expunge) throws MessagingException {
        // Can only be performed on an open folder
        checkOpen();

        try {
            if (mode == READ_WRITE) {
                // find all messages marked deleted and issue DELE commands
                POP3Message m;
                for (int i = 0; i < msgCache.size(); i++) {
                    if ((m = (POP3Message) msgCache.elementAt(i)) != null) {
                        if (m.isSet(Flags.Flag.DELETED)) {
                            try {
                                pop3Con.sendCommand(POP3CommandFactory.getCOMMAND_DELE(i + 1));
                            } catch (Exception e) {
                                throw new MessagingException("Exception deleting message no [" + (i + 1)
                                        + "] during close", e);
                            }
                        }
                    }
                }
            }

            try {
                pop3Con.sendCommand(POP3CommandFactory.getCOMMAND_QUIT());
            } catch (Exception e) {
                // doesn't really care about the response
            }
            // dosn't need a catch block here, but added incase something goes
            // wrong
            // so that the finnaly is garunteed to execute in such a case.
        } finally {
            try {
                pop3Con.close();
            } catch (Exception e) {
                // doesn't really care about the response
                // all we can do is to set the reference explicitly to null
                pop3Con = null;
            }

            /*
             * The message numbers depend on the mail drop if the connection is
             * closed, then purge the cache
             */
            msgCache = null;
            isFolderOpen = false;
            notifyConnectionListeners(ConnectionEvent.CLOSED);
        }
    }

    public boolean isOpen() {
        return isFolderOpen;
    }

    public Flags getPermanentFlags() {
        // unfortunately doesn't have a throws clause for this method
        // throw new MethodNotSupportedException("POP3 doesn't support permanent
        // flags");

        // Better than returning null, save the extra condition from a user to
        // check for null
        // and avoids a NullPointerException for the careless.
        return new Flags();
    }

    public int getMessageCount() throws MessagingException {
        return msgCount;
    }

    /**
     * Checks wether the message is in cache, if not will create a new message
     * object and return it.
     * 
     * @see javax.mail.Folder#getMessage(int)
     */
    public Message getMessage(int msgNum) throws MessagingException {
        // Can only be performed on an Open folder
        checkOpen();
        if (msgNum < 1 || msgNum > getMessageCount()) {
            throw new MessagingException("Invalid Message number");
        }

        Message msg = null;
        try {
            msg = (Message) msgCache.elementAt(msgNum);
        } catch (RuntimeException e) {
            session.getDebugOut().println("Message not in cache");
        }
        if (msg == null) {
            msg = POP3MessageFactory.createMessage(this, session, pop3Con, msgNum);
            msgCache.setElementAt(msg, msgNum);
        }

        return msg;
    }

    public void appendMessages(Message[] msgs) throws MessagingException {
        throw new MethodNotSupportedException("Message appending is not supported in POP3");

    }

    public Message[] expunge() throws MessagingException {
        throw new MethodNotSupportedException("Expunge is not supported in POP3");
    }

    public int getMode() throws IllegalStateException {
        // Can only be performed on an Open folder
        checkOpen();
        return mode;
    }

    /**
     * @see javax.mail.Folder#fetch(javax.mail.Message[],
     *      javax.mail.FetchProfile)
     * 
     * The JavaMail API recommends that this method be overrident to provide a
     * meaningfull implementation.
     */
    public void fetch(Message[] msgs, FetchProfile fp) throws MessagingException {
        // Can only be performed on an Open folder
        checkOpen();
        for (int i = 0; i < msgs.length; i++) {
            Message msg = msgs[i];
            if (msg == null) {
                msg = POP3MessageFactory.createMessage(this, session, pop3Con, i);
            }
            if (fp.contains(FetchProfile.Item.ENVELOPE)) {
                msg = POP3MessageFactory.createMessageWithEvelope((POP3Message) msg);
            }

            if (fp.contains(FetchProfile.Item.CONTENT_INFO)) {
                msg = POP3MessageFactory.createMessageWithContentInfo((POP3Message) msg);
            }

            if (fp.contains(FetchProfile.Item.FLAGS)) {
                msg = POP3MessageFactory.createMessageWithFlags((POP3Message) msg);
            }

            msgs[i] = msg;
        }
    }

    /**
     * Below is a list of covinience methods that avoid repeated checking for a
     * value and throwing an exception
     */

    /** Ensure the folder is open */
    private void checkOpen() throws IllegalStateException {
        if (!isFolderOpen) {
            throw new IllegalStateException("Folder is not Open");
        }
    }

    /** Ensure the folder is not open */
    private void checkClosed() throws IllegalStateException {
        if (isFolderOpen) {
            throw new IllegalStateException("Folder is Open");
        }
    }

    /**
     * @see javax.mail.Folder#notifyMessageChangedListeners(int,
     *      javax.mail.Message)
     * 
     * this method is protected and cannot be used outside of Folder, therefore
     * had to explicitly expose it via a method in POP3Folder, so that
     * POP3Message has access to it
     * 
     * Bad design on the part of the Java Mail API.
     */
    public void notifyMessageChangedListeners(int type, Message m) {
        super.notifyMessageChangedListeners(type, m);
    }

}
