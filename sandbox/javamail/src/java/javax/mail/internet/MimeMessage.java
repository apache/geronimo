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

package javax.mail.internet;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
// TODO there's a bunch of methods that overlap between MimeMessaeg and MimeBodyPart.
// If we can implement one in terms of the other it would be more efficient, and
// it's fairly likely that we can ...
/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:09 $
 */
public class MimeMessage extends Message implements MimePart {
    public static class RecipientType extends Message.RecipientType {
        public static final RecipientType NEWSGROUPS =
            new RecipientType("Newsgroups");
        private RecipientType(String type) {
            super(type);
        }
        protected Object readResolve() throws ObjectStreamException {
            if (this.type.equals("Newsgroups")) {
                return NEWSGROUPS;
            } else {
                return super.readResolve();
            }
        }
    }
    protected byte content[];
    protected InputStream contentStream;
    protected DataHandler dh;
    protected Flags flags;
    protected InternetHeaders headers;
    protected boolean modified;
    protected boolean saved;
    protected MimeMessage(Folder folder, InputStream in, int number)
        throws MessagingException {
    }
    protected MimeMessage(Folder folder, int number) {
    }
    protected MimeMessage(
        Folder folder,
        InternetHeaders headers,
        byte[] content,
        int number)
        throws MessagingException {
    }
    public MimeMessage(MimeMessage message) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public MimeMessage(Session session) {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public MimeMessage(Session session, InputStream in)
        throws MessagingException {
        this.session = session;
        contentStream = in;
    }
    public void addFrom(Address[] address) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void addHeader(String name, String value)
        throws MessagingException {
        headers.addHeader(name, value);
    }
    public void addHeaderLine(String line) throws MessagingException {
        headers.addHeaderLine(line);
    }
    public void addRecipients(Message.RecipientType type, Address[] address)
        throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void addRecipients(Message.RecipientType type, String address)
        throws MessagingException {
        addRecipients(type, InternetAddress.parse(address));
    }
    protected InternetHeaders createInternetHeaders(InputStream in)
        throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public Enumeration getAllHeaderLines() throws MessagingException {
        return headers.getAllHeaderLines();
    }
    public Enumeration getAllHeaders() throws MessagingException {
        return headers.getAllHeaders();
    }
    public Address[] getAllRecipients() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public Object getContent() throws IOException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getContentID() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String[] getContentLanguage() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getContentMD5() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    protected InputStream getContentStream() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getContentType() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public synchronized DataHandler getDataHandler()
        throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getDescription() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getDisposition() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getEncoding() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getFileName() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public synchronized Flags getFlags() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public Address[] getFrom() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String[] getHeader(String name) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getHeader(String name, String delimiter)
        throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public InputStream getInputStream() throws IOException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public int getLineCount() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public Enumeration getMatchingHeaderLines(String[] names)
        throws MessagingException {
        return headers.getMatchingHeaderLines(names);
    }
    public Enumeration getMatchingHeaders(String[] names)
        throws MessagingException {
        return getMatchingHeaders(names);
    }
    public String getMessageID() throws MessagingException {
        return headers.getHeader("Message-ID", null);
    }
    public Enumeration getNonMatchingHeaderLines(String[] names)
        throws MessagingException {
        return headers.getNonMatchingHeaderLines(names);
    }
    public Enumeration getNonMatchingHeaders(String[] names)
        throws MessagingException {
        return headers.getNonMatchingHeaders(names);
    }
    public InputStream getRawInputStream() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public Date getReceivedDate() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public Address[] getRecipients(Message.RecipientType type)
        throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public Address[] getReplyTo() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public Address getSender() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public Date getSentDate() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public int getSize() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getSubject() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public boolean isMimeType(String type) throws MessagingException {
        ContentType c1 = new ContentType(type);
        ContentType c2 = new ContentType(dh.getContentType());
        return c1.match(c2);
    }
    public synchronized boolean isSet(Flags.Flag flag)
        throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    protected void parse(InputStream in) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void removeHeader(String name) throws MessagingException {
        headers.removeHeader(name);
    }
    public Message reply(boolean replyToAll) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void saveChanges() throws MessagingException {
        updateHeaders();
    }
    public void setContent(Multipart part) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setContent(Object content, String type)
        throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setContentID(String cid) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setContentLanguage(String[] languages)
        throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setContentMD5(String md5) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setDataHandler(DataHandler handler) throws MessagingException {
        dh = handler;
    }
    public void setDescription(String description) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setDescription(String description, String charset)
        throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setDisposition(String disposition) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setFileName(String name) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public synchronized void setFlags(Flags flags, boolean set)
        throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setFrom() throws MessagingException {
        setFrom(InternetAddress.getLocalAddress(session));
    }
    public void setFrom(Address address) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setHeader(String name, String value)
        throws MessagingException {
        headers.setHeader(name, value);
    }
    public void setRecipients(Message.RecipientType type, Address[] address)
        throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setRecipients(Message.RecipientType type, String address)
        throws MessagingException {
        setRecipients(type, InternetAddress.parse(address));
    }
    public void setReplyTo(Address[] address) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setSentDate(Date sent) throws MessagingException {
    }
    public void setSubject(String subject) throws MessagingException {
    }
    public void setSubject(String subject, String charset)
        throws MessagingException {
    }
    public void setText(String text) throws MessagingException {
    }
    public void setText(String text, String charset)
        throws MessagingException {
    }
    protected void updateHeaders() throws MessagingException {
    }
    public void writeTo(OutputStream out) throws IOException {
    }
    public void writeTo(OutputStream out, String[] ignoreHeaders)
        throws IOException {
    }
    public void setSender(Address address) {
    }
}
