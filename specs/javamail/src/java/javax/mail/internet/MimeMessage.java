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
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
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

    private final MailDateFormat dateFormat = new MailDateFormat();

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
     *
     * @param session the session for this message
     * @param in      the stream to load from
     * @throws MessagingException if there is a problem reading or parsing the stream
     */
    public MimeMessage(Session session, InputStream in) throws MessagingException {
        super(session);
        parse(in);
    }

    /**
     * Copy a MimeMessage.
     *
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
     *
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
     *
     * @param folder the folder for this message
     * @param in     the stream to load from
     * @param number the message number of the new message
     * @throws MessagingException if there is a problem reading or parsing the stream
     */
    protected MimeMessage(Folder folder, InputStream in, int number) throws MessagingException {
        super(folder, number);
        parse(in);
    }

    /**
     * Create a MimeMessage with the supplied headers and content.
     *
     * @param folder  the folder for this message
     * @param headers the headers for the new message
     * @param content the content of the new message
     * @param number  the message number of the new message
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
     *
     * @param in the stream to read
     * @throws MessagingException if there was a problem parsing the stream
     */
    protected void parse(InputStream in) throws MessagingException {
        in = new BufferedInputStream(in);
        headers = new InternetHeaders(in);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte buffer[] = new byte[1024];
            int count;
            while ((count = in.read(buffer, 0, 1024)) != -1) {
                baos.write(buffer, 0, count);
            }
        } catch (Exception e) {
            throw new MessagingException(e.toString(), e);
        }
        content = baos.toByteArray();
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
     *
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
     *
     * @return the "Sender" header as an address, or null if not present
     * @throws MessagingException if there was a problem parsing the header
     */
    public Address getSender() throws MessagingException {
        InternetAddress[] addrs = getHeaderAsAddresses("Sender", isStrictAddressing());
        return addrs.length > 0 ? addrs[0] : null;
    }

    /**
     * Set the "Sender" header.
     *
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
        List recipients = new ArrayList();
        addRecipientsToList(recipients, RecipientType.TO);
        addRecipientsToList(recipients, RecipientType.CC);
        addRecipientsToList(recipients, RecipientType.BCC);
        addRecipientsToList(recipients, RecipientType.NEWSGROUPS);
        return (Address[]) recipients.toArray(new Address[recipients.size()]);
    }

    private void addRecipientsToList(List list, Message.RecipientType type) throws MessagingException {
        Address[] recipients = getHeaderAsAddresses(getHeaderForRecipientType(type), isStrictAddressing());
        if (recipients != null) {
            list.addAll(Arrays.asList(recipients));
        }
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
        return getHeaderAsAddresses("Reply-To", isStrictAddressing());
    }

    public void setReplyTo(Address[] address) throws MessagingException {
        setHeader("Reply-To", address);
    }

    public String getSubject() throws MessagingException {
        String subject = getSingleHeader("Subject");
        if (subject == null) {
            return null;
        } else {
            try {
                return MimeUtility.decodeText(subject);
            } catch (UnsupportedEncodingException e) {
                return subject;
            }
        }
    }

    public void setSubject(String subject) throws MessagingException {
        setHeader("Subject", subject);
    }

    public void setSubject(String subject, String charset) throws MessagingException {
        try {
            setHeader("Subject", MimeUtility.encodeText(subject, charset, null));
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException(e.getMessage(), e);
        }
    }

    public Date getSentDate() throws MessagingException {
        String value = getSingleHeader("Date");
        if (value == null) {
            return null;
        }
        try {
            return dateFormat.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }

    public void setSentDate(Date sent) throws MessagingException {
        if (sent == null) {
            removeHeader("Date");
        } else {
            setHeader("Date", dateFormat.format(sent));
        }
    }

    public Date getReceivedDate() throws MessagingException {
        return null;
    }

    public int getSize() throws MessagingException {
        if (content != null) {
            return content.length;
        }
        return -1;
    }

    public int getLineCount() throws MessagingException {
        return -1;
    }

    public String getContentType() throws MessagingException {
        String value = getSingleHeader("Content-Type");
        if (value == null) {
            value = "text/plain";
        }
        return value;
    }

    public boolean isMimeType(String type) throws MessagingException {
        return new ContentType(getContentType()).match(type);
    }

    public String getDisposition() throws MessagingException {
        return getSingleHeader("Content-Disposition");
    }

    public void setDisposition(String disposition) throws MessagingException {
        setHeader("Content-Disposition", disposition);
    }

    public String getEncoding() throws MessagingException {
        return getSingleHeader("Content-Transfer-Encoding");
    }

    public String getContentID() throws MessagingException {
        return getSingleHeader("Content-ID");
    }

    public void setContentID(String cid) throws MessagingException {
        setHeader("Content-ID", cid);
    }

    public String getContentMD5() throws MessagingException {
        return getSingleHeader("Content-MD5");
    }

    public void setContentMD5(String md5) throws MessagingException {
        setHeader("Content-MD5", md5);
    }

    public String getDescription() throws MessagingException {
        return getSingleHeader("Content-Description");
    }

    public void setDescription(String description) throws MessagingException {
        setHeader("Content-Description", description);
    }

    public void setDescription(String description, String charset) throws MessagingException {
        // todo encoding
        setHeader("Content-Description", description);
    }

    public String[] getContentLanguage() throws MessagingException {
        return getHeader("Content-Language");
    }

    public void setContentLanguage(String[] languages) throws MessagingException {
        if (languages == null || languages.length == 0) {
            removeHeader("Content-Language");
        } else if (languages.length == 1) {
            setHeader("Content-Language", languages[0]);
        } else {
            StringBuffer buf = new StringBuffer(languages.length * 20);
            buf.append(languages[0]);
            for (int i = 1; i < languages.length; i++) {
                buf.append(',').append(languages[i]);
            }
            setHeader("Content-Language", buf.toString());
        }
    }

    public String getMessageID() throws MessagingException {
        return getSingleHeader("Message-ID");
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
        return getDataHandler().getInputStream();
    }

    protected InputStream getContentStream() throws MessagingException {
        if (content != null) {
            return new ByteArrayInputStream(content);
        } else {
            throw new MessagingException("No content");
        }
    }

    public InputStream getRawInputStream() throws MessagingException {
        return getContentStream();
    }

    public synchronized DataHandler getDataHandler() throws MessagingException {
        if (dh == null) {
            dh = new DataHandler(new MimePartDataSource(this));
        }
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
        writeTo(out, null);
    }

    public void writeTo(OutputStream out, String[] ignoreHeaders) throws MessagingException, IOException {
        if (!saved) {
            saveChanges();
        }
        headers.writeTo(out, ignoreHeaders);
        out.write(13);
        out.write(10);
        if (modified) {
            dh.writeTo(MimeUtility.encode(out, getEncoding()));
        } else {
            out.write(content);
        }
    }

    public String[] getHeader(String name) throws MessagingException {
        return headers.getHeader(name);
    }

    public String getHeader(String name, String delimiter) throws MessagingException {
        return headers.getHeader(name, delimiter);
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
        return headers.getMatchingHeaders(names);
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
        return property == null ? true : Boolean.valueOf(property).booleanValue();
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

    private String getSingleHeader(String name) throws MessagingException {
        String[] values = getHeader(name);
        if (values == null || values.length == 0) {
            return null;
        } else {
            return values[0];
        }
    }
}
