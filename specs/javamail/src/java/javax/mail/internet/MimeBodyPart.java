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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;

/**
 * @version $Rev$ $Date$
 */
public class MimeBodyPart extends BodyPart implements MimePart {
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

    public MimeBodyPart() {
        headers = new InternetHeaders();
    }

    public MimeBodyPart(InputStream in) throws MessagingException {
        this.contentStream = in;
    }

    public MimeBodyPart(InternetHeaders headers, byte[] content) throws MessagingException {
        this.headers = headers;
        this.content = content;
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

    public void writeTo(OutputStream out) throws IOException, MessagingException {
        headers.writeTo(out, null);
        out.write(13);
        out.write(10);
        out.write(content);
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

    public Enumeration getMatchingHeaders(String[] name) throws MessagingException {
        return headers.getMatchingHeaders(name);
    }

    public Enumeration getNonMatchingHeaders(String[] name) throws MessagingException {
        return headers.getNonMatchingHeaders(name);
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

    protected void updateHeaders() throws MessagingException {
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
