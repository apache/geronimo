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
//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.mail;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.mail.search.SearchTerm;
/**
 * @version $Revision: 1.2 $ $Date: 2003/08/16 04:29:52 $
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
