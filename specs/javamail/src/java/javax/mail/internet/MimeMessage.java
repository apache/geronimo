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

/**
 * @version $Rev$ $Date$
 */
public class MimeMessage extends Message implements MimePart {
    /**
     * Extends {@link javax.mail.Message.RecipientType} to support addition recipient types.
     */
    public static class RecipientType extends Message.RecipientType {
        /**
         * Recipient type for Usenet news.
         */
        public static final RecipientType NEWSGROUPS = new RecipientType("Newsgroups");

        protected RecipientType(String type) {
            super(type);
        }

        /**
         * Ensure the singleton is returned.
         *
         * @return resolved object
         */
        protected Object readResolve() throws ObjectStreamException {
            if (this.type.equals("Newsgroups")) {
                return NEWSGROUPS;
            } else {
                return super.readResolve();
            }
        }
    }

    /**
     * The {@link DataHandler} for this Message's content.
     */
    protected DataHandler dh;
    /**
     * This message's content (unless sourced from a SharedInputStream).
     */
    protected byte content[];
    /**
     * If the data for this message was supplied by a {@link SharedInputStream}
     * then this is another such stream representing the content of this message;
     * if this field is non-null, then {@link #content} will be null.
     */
    protected InputStream contentStream;
    /**
     * This message's headers.
     */
    protected InternetHeaders headers;
    /**
     * This message's flags.
     */
    protected Flags flags;
    /**
     * Flag indicating that the message has been modified; set to true when
     * an empty message is created or when {@link #saveChanges()} is called.
     */
    protected boolean modified;
    /**
     * Flag indicating that the message has been saved.
     */
    protected boolean saved;

    /**
     * Create a new MimeMessage.
     * An empty message is created, with empty {@link #headers} and empty {@link #flags}.
     * The {@link #modified} flag is set.
     *
     * @param session the session for this message
     */
    public MimeMessage(Session session) {
        super(session);
        headers = new InternetHeaders();
        flags = new Flags();
        modified = true;
    }

    /**
     * Create a MimeMessage by reading an parsing the data from the supplied stream.
     * @param session the session for this message
     * @param in the stream to load from
     * @throws MessagingException if there is a problem reading or parsing the stream
     */
    public MimeMessage(Session session, InputStream in) throws MessagingException {
        super(session);
        parse(in);
    }

    /**
     * Copy a MimeMessage.
     * @param message the message to copy
     * @throws MessagingException is there was a problem copying the message
     */
    public MimeMessage(MimeMessage message) throws MessagingException {
        super(message.session);
        // todo copy the message - how?
        throw new UnsupportedOperationException();
    }

    /**
     * Create an new MimeMessage in the supplied {@link Folder} and message number.
     * @param folder the Folder that contains the new message
     * @param number the message number of the new message
     */
    protected MimeMessage(Folder folder, int number) {
        super(folder, number);
        headers = new InternetHeaders();
        flags = new Flags();
        modified = true;
    }

    /**
     * Create a MimeMessage by reading an parsing the data from the supplied stream.
     * @param folder the folder for this message
     * @param in the stream to load from
     * @param number the message number of the new message
     * @throws MessagingException if there is a problem reading or parsing the stream
     */
    protected MimeMessage(Folder folder, InputStream in, int number) throws MessagingException {
        super(folder, number);
        parse(in);
    }

    /**
     * Create a MimeMessage with the supplied headers and content.
     * @param folder the folder for this message
     * @param headers the headers for the new message
     * @param content the content of the new message
     * @param number the message number of the new message
     * @throws MessagingException if there is a problem reading or parsing the stream
     */
    protected MimeMessage(Folder folder, InternetHeaders headers, byte[] content, int number) throws MessagingException {
        super(folder, number);
        this.headers = headers;
        this.content = content;
        modified = true;
    }

    /**
     * Parse the supplied stream and initialize {@link #headers} and {@link #content} appropriately.
     * @param in the stream to read
     * @throws MessagingException if there was a problem parsing the stream
     */
    protected void parse(InputStream in) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public Address[] getFrom() throws MessagingException {
        boolean strict = isStrictAddressing();
        Address[] result = getHeaderAsAddresses("From", strict);
        if (result == null) {
            result = getHeaderAsAddresses("Sender", strict);
        }
        return result;
    }

    public void setFrom(Address address) throws MessagingException {
        setHeader("From", address);
    }

    /**
     * Set the "From" header using the value returned by {@link InternetAddress#getLocalAddress(javax.mail.Session)}.
     * @throws MessagingException if there was a problem setting the header
     */
    public void setFrom() throws MessagingException {
        setFrom(InternetAddress.getLocalAddress(session));
    }

    public void addFrom(Address[] addresses) throws MessagingException {
        addHeader("From", addresses);
    }

    /**
     * Return the "Sender" header as an address.
     * @return the "Sender" header as an address, or null if not present
     * @throws MessagingException if there was a problem parsing the header
     */
    public Address getSender() throws MessagingException {
        InternetAddress[] addrs = getHeaderAsAddresses("Sender", isStrictAddressing());
        return addrs.length > 0 ? addrs[0] : null;
    }

    /**
     * Set the "Sender" header.
     * @param address the new Sender address
     * @throws MessagingException if there was a problem setting the header
     */
    public void setSender(Address address) throws MessagingException {
        setHeader("Sender", address);
    }

    public Address[] getRecipients(Message.RecipientType type) throws MessagingException {
        return getHeaderAsAddresses(getHeaderForRecipientType(type), isStrictAddressing());
    }

    public Address[] getAllRecipients() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void setRecipients(Message.RecipientType type, Address[] addresses) throws MessagingException {
        setHeader(getHeaderForRecipientType(type), addresses);
    }

    public void setRecipients(Message.RecipientType type, String address) throws MessagingException {
        setHeader(getHeaderForRecipientType(type), address);
    }

    public void addRecipients(Message.RecipientType type, Address[] address) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void addRecipients(Message.RecipientType type, String address) throws MessagingException {
        addHeader(getHeaderForRecipientType(type), address);
    }

    public Address[] getReplyTo() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void setReplyTo(Address[] address) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public String getSubject() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void setSubject(String subject) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void setSubject(String subject, String charset) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public Date getSentDate() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void setSentDate(Date sent) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public Date getReceivedDate() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public int getSize() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public int getLineCount() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public String getContentType() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public boolean isMimeType(String type) throws MessagingException {
        ContentType c1 = new ContentType(type);
        ContentType c2 = new ContentType(dh.getContentType());
        return c1.match(c2);
    }

    public String getDisposition() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void setDisposition(String disposition) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public String getEncoding() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public String getContentID() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void setContentID(String cid) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public String getContentMD5() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void setContentMD5(String md5) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public String getDescription() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void setDescription(String description) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void setDescription(String description, String charset) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public String[] getContentLanguage() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void setContentLanguage(String[] languages) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public String getMessageID() throws MessagingException {
        return headers.getHeader("Message-ID", null);
    }

    public String getFileName() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void setFileName(String name) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public InputStream getInputStream() throws MessagingException, IOException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    protected InputStream getContentStream() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public InputStream getRawInputStream() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public synchronized DataHandler getDataHandler() throws MessagingException {
        return dh;
    }

    public Object getContent() throws MessagingException, IOException {
        return getDataHandler().getContent();
    }

    public void setDataHandler(DataHandler handler) throws MessagingException {
        dh = handler;
    }

    public void setContent(Object content, String type) throws MessagingException {
        setDataHandler(new DataHandler(content, type));
    }

    public void setText(String text) throws MessagingException {
        setText(text, MimeUtility.getDefaultJavaCharset());
    }

    public void setText(String text, String charset) throws MessagingException {
        setContent(text, "text/plain; charset=" + charset);
    }

    public void setContent(Multipart part) throws MessagingException {
        setDataHandler(new DataHandler(part, part.getContentType()));
        part.setParent(this);
    }

    public Message reply(boolean replyToAll) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void writeTo(OutputStream out) throws MessagingException, IOException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void writeTo(OutputStream out, String[] ignoreHeaders) throws MessagingException, IOException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public String[] getHeader(String name) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public String getHeader(String name, String delimiter) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void setHeader(String name, String value) throws MessagingException {
        headers.setHeader(name, value);
    }

    public void addHeader(String name, String value) throws MessagingException {
        headers.addHeader(name, value);
    }

    public void removeHeader(String name) throws MessagingException {
        headers.removeHeader(name);
    }

    public Enumeration getAllHeaders() throws MessagingException {
        return headers.getAllHeaders();
    }

    public Enumeration getMatchingHeaders(String[] names) throws MessagingException {
        return getMatchingHeaders(names);
    }

    public Enumeration getNonMatchingHeaders(String[] names) throws MessagingException {
        return headers.getNonMatchingHeaders(names);
    }

    public void addHeaderLine(String line) throws MessagingException {
        headers.addHeaderLine(line);
    }

    public Enumeration getAllHeaderLines() throws MessagingException {
        return headers.getAllHeaderLines();
    }

    public Enumeration getMatchingHeaderLines(String[] names) throws MessagingException {
        return headers.getMatchingHeaderLines(names);
    }

    public Enumeration getNonMatchingHeaderLines(String[] names) throws MessagingException {
        return headers.getNonMatchingHeaderLines(names);
    }

    public synchronized Flags getFlags() throws MessagingException {
        return (Flags) flags.clone();
    }

    public synchronized boolean isSet(Flags.Flag flag) throws MessagingException {
        return flags.contains(flag);
    }

    public synchronized void setFlags(Flags flags, boolean set) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void saveChanges() throws MessagingException {
        updateHeaders();
    }

    protected void updateHeaders() throws MessagingException {
    }

    protected InternetHeaders createInternetHeaders(InputStream in) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    private InternetAddress[] getHeaderAsAddresses(String header, boolean strict) throws MessagingException {
        return headers.getHeaderAsAddresses(header, strict);
    }

    private boolean isStrictAddressing() {
        String property = session.getProperty("mail.mime.address.strict");
        return property == null ? true: Boolean.valueOf(property).booleanValue();
    }

    private void setHeader(String header, Address address) {
        if (address == null) {
            headers.removeHeader(header);
        } else {
            headers.setHeader(header, address.toString());
        }
    }

    private void setHeader(String header, Address[] addresses) {
        if (addresses == null) {
            headers.removeHeader(header);
        } else {
            headers.setHeader(header, addresses);
        }
    }

    private void addHeader(String header, Address[] addresses) {
        headers.addHeader(header, InternetAddress.toString(addresses));
    }

    private String getHeaderForRecipientType(Message.RecipientType type) throws MessagingException {
        if (RecipientType.TO == type) {
            return "To";
        } else if (RecipientType.CC == type) {
            return "Cc";
        } else if (RecipientType.BCC == type) {
            return "Bcc";
        } else if (RecipientType.NEWSGROUPS == type) {
            return "Newsgroups";
        } else {
            throw new MessagingException("Unsupported recipient type: " + type.toString());
        }
    }
}
