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

package javax.mail;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.mail.search.SearchTerm;
/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:07 $
 */
public abstract class Message implements Part {
    public static class RecipientType implements Serializable {
        public static final RecipientType BCC = new RecipientType("Bcc");
        public static final RecipientType CC = new RecipientType("Cc");
        public static final RecipientType TO = new RecipientType("To");
        protected String type;
        protected RecipientType(String type) {
            this.type = type;
        }
        protected Object readResolve() throws ObjectStreamException {
            if (type.equals("To")) {
                return TO;
            } else if (type.equals("Cc")) {
                return CC;
            } else if (type.equals("Bcc")) {
                return BCC;
            } else {
                return this;
            }
        }
        public String toString() {
            return type;
        }
    }
    private static final Address[] ADDRESS_ARRAY = new Address[0];
    protected boolean expunged;
    protected Folder folder;
    protected int msgnum;
    protected Session session;
    protected Message() {
    }
    protected Message(Folder folder, int number) {
        this.folder = folder;
        this.msgnum = number;
    }
    protected Message(Session session) {
        this.session = session;
    }
    public abstract void addFrom(Address[] addresses)
        throws MessagingException;
    public void addRecipient(RecipientType type, Address address)
        throws MessagingException {
        addRecipients(type, new Address[] { address });
    }
    public abstract void addRecipients(RecipientType type, Address[] addresses)
        throws MessagingException;
    public Address[] getAllRecipients() throws MessagingException {
        Address[] to = getRecipients(RecipientType.TO);
        Address[] cc = getRecipients(RecipientType.CC);
        Address[] bcc = getRecipients(RecipientType.BCC);
        List result = new LinkedList();
        for (int id = 0; to != null && id < to.length; id++) {
            result.add(to[id]);
        }
        for (int id = 0; cc != null && id < cc.length; id++) {
            result.add(cc[id]);
        }
        for (int id = 0; bcc != null && id < bcc.length; id++) {
            result.add(bcc[id]);
        }
        return (Address[]) result.toArray(ADDRESS_ARRAY);
    }
    public abstract Flags getFlags() throws MessagingException;
    public Folder getFolder() {
        return folder;
    }
    public abstract Address[] getFrom() throws MessagingException;
    public int getMessageNumber() {
        return msgnum;
    }
    public abstract Date getReceivedDate() throws MessagingException;
    public abstract Address[] getRecipients(RecipientType type)
        throws MessagingException;
    public Address[] getReplyTo() throws MessagingException {
        return getFrom();
    }
    public abstract Date getSentDate() throws MessagingException;
    public abstract String getSubject() throws MessagingException;
    public boolean isExpunged() {
        return expunged;
    }
    public boolean isSet(Flags.Flag flag) throws MessagingException {
        return getFlags().contains(flag);
    }
    public boolean match(SearchTerm term) throws MessagingException {
        return term.match(this);
    }
    public abstract Message reply(boolean replyToAll)
        throws MessagingException;
    public abstract void saveChanges() throws MessagingException;
    protected void setExpunged(boolean expunged) {
        this.expunged = expunged;
    }
    public void setFlag(Flags.Flag flag, boolean set)
        throws MessagingException {
        Flags flags = getFlags();
        if (set) {
            flags.add(flag);
        } else {
            flags.remove(flag);
        }
    }
    public abstract void setFlags(Flags flags, boolean set)
        throws MessagingException;
    public abstract void setFrom() throws MessagingException;
    public abstract void setFrom(Address address) throws MessagingException;
    protected void setMessageNumber(int number) {
        msgnum = number;
    }
    public void setRecipient(RecipientType type, Address address)
        throws MessagingException {
        setRecipients(type, new Address[] { address });
    }
    public abstract void setRecipients(RecipientType type, Address[] addresses)
        throws MessagingException;
    public void setReplyTo(Address[] addresses) throws MessagingException {
        throw new MethodNotSupportedException("setReplyTo not implemented");
    }
    public abstract void setSentDate(Date sent) throws MessagingException;
    public abstract void setSubject(String subject) throws MessagingException;
}
