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

package javax.mail;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.mail.Flags.Flag;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.FolderEvent;
import javax.mail.event.FolderListener;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.event.TransportListener;
import javax.mail.search.SearchTerm;
/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:27 $
 */
public abstract class Folder {
    // Constants from J2SE 1.4 doc (Constant Values)
    public static final int HOLDS_FOLDERS = 2;
    public static final int HOLDS_MESSAGES = 1;
    private static final Message[] MESSAGE_ARRAY = new Message[0];
    public static final int READ_ONLY = 1;
    public static final int READ_WRITE = 2;
    private List _connectionListeners = new LinkedList();
    private List _folderListeners = new LinkedList();
    private List _messageChangedListeners = new LinkedList();
    private List _messageCountListeners = new LinkedList();
    private boolean _subscribed;
    protected int mode;
    protected Store store;
    protected Folder(Store store) {
        this.store = store;
    }
    public void addConnectionListener(ConnectionListener listener) {
        _connectionListeners.add(listener);
    }
    public void addFolderListener(FolderListener listener) {
        _folderListeners.add(listener);
    }
    public void addMessageChangedListener(MessageChangedListener listener) {
        _messageChangedListeners.add(listener);
    }
    public void addMessageCountListener(MessageCountListener listener) {
        _messageCountListeners.add(listener);
    }
    public abstract void appendMessages(Message[] messages)
        throws MessagingException;
    public abstract void close(boolean expunge) throws MessagingException;
    public void copyMessages(Message[] messages, Folder folder)
        throws MessagingException {
        folder.appendMessages(messages);
    }
    public abstract boolean create(int type) throws MessagingException;
    public abstract boolean delete(boolean recurse) throws MessagingException;
    public abstract boolean exists() throws MessagingException;
    public abstract Message[] expunge() throws MessagingException;
    public void fetch(Message[] messages, FetchProfile profile)
        throws MessagingException {
        // default does not do anything
        return;
    }
    protected void finalize() throws Throwable {
        try {
            super.finalize();
        } finally {
        }
    }
    private int getCount(Flag flag) throws MessagingException {
        return getCount(flag, true);
    }
    private int getCount(Flag flag, boolean value) throws MessagingException {
        if (isOpen()) {
            Message[] messages = getMessages();
            int total = 0;
            for (int i = 0; i < messages.length; i++) {
                if (messages[i].getFlags().contains(flag) == value) {
                    total++;
                }
            }
            return total;
        } else {
            return -1;
        }
    }
    public int getDeletedMessageCount() throws MessagingException {
        return getCount(Flags.Flag.DELETED);
    }
    public abstract Folder getFolder(String name) throws MessagingException;
    public abstract String getFullName();
    public abstract Message getMessage(int id) throws MessagingException;
    public abstract int getMessageCount() throws MessagingException;
    public Message[] getMessages() throws MessagingException {
        return getMessages(1, getMessageCount());
    }
    public Message[] getMessages(int from, int to) throws MessagingException {
        if (to == -1 || to < from) {
            throw new IndexOutOfBoundsException(
                "Invalid message range: " + from + " to " + to);
        }
        Message[] result = new Message[to - from + 1];
        for (int i = from; i <= to; i++) {
            result[i] = getMessage(i);
        }
        return result;
    }
    public Message[] getMessages(int ids[]) throws MessagingException {
        Message[] result = new Message[ids.length];
        for (int i = 0; i < ids.length; i++) {
            result[i] = getMessage(ids[i]);
        }
        return result;
    }
    public int getMode() {
        return mode;
    }
    public abstract String getName();
    public int getNewMessageCount() throws MessagingException {
        return getCount(Flags.Flag.RECENT);
    }
    public abstract Folder getParent() throws MessagingException;
    public abstract Flags getPermanentFlags();
    public abstract char getSeparator() throws MessagingException;
    public Store getStore() {
        return store;
    }
    public abstract int getType() throws MessagingException;
    public int getUnreadMessageCount() throws MessagingException {
        return getCount(Flags.Flag.SEEN, false);
    }
    public URLName getURLName() throws MessagingException {
        return store.getURLName();
    }
    public abstract boolean hasNewMessages() throws MessagingException;
    public abstract boolean isOpen();
    public boolean isSubscribed() {
        return _subscribed;
    }
    public Folder[] list() throws MessagingException {
        return list("%");
    }
    public abstract Folder[] list(String pattern) throws MessagingException;
    public Folder[] listSubscribed() throws MessagingException {
        return listSubscribed("%");
    }
    public Folder[] listSubscribed(String pattern) throws MessagingException {
        return list(pattern);
    }
    protected void notifyConnectionListeners(int type) {
        ConnectionEvent event = new ConnectionEvent(this, type);
        Iterator it = _connectionListeners.iterator();
        while (it.hasNext()) {
            TransportListener listener = (TransportListener) it.next();
            event.dispatch(listener);
        }
    }
    protected void notifyFolderListeners(int type) {
        Iterator it = _folderListeners.iterator();
        FolderEvent event = new FolderEvent(this,this,type);
        while (it.hasNext()) {
            FolderListener listener = (FolderListener) it.next();
            event.dispatch(listener);
        }
    }
    protected void notifyFolderRenamedListeners(
        Folder newFolder) {
        Iterator it = _folderListeners.iterator();
        FolderEvent event =
            new FolderEvent(this, this, newFolder, FolderEvent.RENAMED);
        while (it.hasNext()) {
            FolderListener listener = (FolderListener) it.next();
            event.dispatch(listener);
        }
    }
    protected void notifyMessageAddedListeners(Message[] messages) {
        Iterator it = _messageChangedListeners.iterator();
        MessageCountEvent event =
            new MessageCountEvent(
                this,
                MessageCountEvent.ADDED,
                false,
                messages);
        while (it.hasNext()) {
            MessageCountEvent listener = (MessageCountEvent) it.next();
            event.dispatch(listener);
        }
    }
    protected void notifyMessageChangedListeners(int type, Message message) {
        Iterator it = _messageChangedListeners.iterator();
        MessageChangedEvent event =
            new MessageChangedEvent(this, type, message);
        while (it.hasNext()) {
            MessageCountEvent listener = (MessageCountEvent) it.next();
            event.dispatch(listener);
        }
    }
    protected void notifyMessageRemovedListeners(
        boolean removed,
        Message[] messages) {
        Iterator it = _messageChangedListeners.iterator();
        MessageCountEvent event =
            new MessageCountEvent(
                this,
                MessageCountEvent.REMOVED,
                removed,
                messages);
        while (it.hasNext()) {
            MessageCountEvent listener = (MessageCountEvent) it.next();
            event.dispatch(listener);
        }
    }
    public abstract void open(int mode) throws MessagingException;
    public void removeConnectionListener(ConnectionListener listener) {
        _connectionListeners.remove(listener);
    }
    public void removeFolderListener(FolderListener listener) {
        _folderListeners.remove(listener);
    }
    public void removeMessageChangedListener(MessageChangedListener listener) {
        _messageChangedListeners.remove(listener);
    }
    public void removeMessageCountListener(MessageCountListener listener) {
        _messageCountListeners.remove(listener);
    }
    public abstract boolean renameTo(Folder newName) throws MessagingException;
    public Message[] search(SearchTerm term) throws MessagingException {
        return search(term, getMessages());
    }
    public Message[] search(SearchTerm term, Message[] messages)
        throws MessagingException {
        List result = new LinkedList();
        for (int i = 0; i < messages.length; i++) {
            Message message = messages[i];
            if (term.match(message)) {
                result.add(message);
            }
        }
        return (Message[]) result.toArray(MESSAGE_ARRAY);
    }
    public void setFlags(int from, int to, Flags flags, boolean value)
        throws MessagingException {
        setFlags(getMessages(from, to), flags, value);
    }
    public void setFlags(int ids[], Flags flags, boolean value)
        throws MessagingException {
        setFlags(getMessages(ids), flags, value);
    }
    public void setFlags(Message[] messages, Flags flags, boolean value)
        throws MessagingException {
        for (int i = 0; i < messages.length; i++) {
            Message message = messages[i];
            message.setFlags(flags, value);
        }
    }
    public void setSubscribed(boolean subscribed) throws MessagingException {
        _subscribed = subscribed;
    }
    public String toString() {
        String name = getFullName();
        if (name == null) {
            return super.toString();
        } else {
            return name;
        }
    }
}
