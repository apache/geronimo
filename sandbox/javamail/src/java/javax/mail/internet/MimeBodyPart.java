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
import java.io.OutputStream;
import java.util.Enumeration;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:09 $
 */
public class MimeBodyPart extends BodyPart implements MimePart {
    protected byte content[];
    protected InputStream contentStream;
    protected DataHandler dh;
    protected InternetHeaders headers;
    public MimeBodyPart() {
    }
    public MimeBodyPart(InputStream in) throws MessagingException {
        this.contentStream = in;
    }
    public MimeBodyPart(InternetHeaders headers, byte[] content)
        throws MessagingException {
        this.headers = headers;
        this.content = content;
    }
    public void addHeader(String name, String value)
        throws MessagingException {
        headers.addHeader(name, value);
    }
    public void addHeaderLine(String line) throws MessagingException {
        headers.addHeaderLine(line);
    }
    public Enumeration getAllHeaderLines() throws MessagingException {
        return headers.getAllHeaderLines();
    }
    public Enumeration getAllHeaders() throws MessagingException {
        return headers.getAllHeaders();
    }
    public Object getContent() throws IOException, MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getContentID() throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String[] getContentLanguage() throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getContentMD5() throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    protected InputStream getContentStream() throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getContentType() throws MessagingException {
        return dh.getContentType();
    }
    public DataHandler getDataHandler() throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getDescription() throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getDisposition() throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getEncoding() throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String getFileName() throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public String[] getHeader(String name) throws MessagingException {
        return headers.getHeader(name);
    }
    public String getHeader(String name, String delimiter)
        throws MessagingException {
        return headers.getHeader(name, delimiter);
    }
    public InputStream getInputStream()
        throws IOException, MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public int getLineCount() throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public Enumeration getMatchingHeaderLines(String[] names)
        throws MessagingException {
        return headers.getMatchingHeaderLines(names);
    }
    public Enumeration getMatchingHeaders(String[] name)
        throws MessagingException {
        return headers.getMatchingHeaders(name);
    }
    public Enumeration getNonMatchingHeaderLines(String[] names)
        throws MessagingException {
        return headers.getNonMatchingHeaderLines(names);
    }
    public Enumeration getNonMatchingHeaders(String[] name)
        throws MessagingException {
        return headers.getNonMatchingHeaders(name);
    }
    public InputStream getRawInputStream() throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public int getSize() throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public boolean isMimeType(String type) throws MessagingException {
        ContentType c1 = new ContentType(type);
        ContentType c2 = new ContentType(dh.getContentType());
        return c1.match(c2);
    }
    public void removeHeader(String name) throws MessagingException {
        headers.removeHeader(name);
    }
    public void setContent(Multipart content) throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setContent(Object content, String type)
        throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setContentID(String cid) throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setContentLanguage(String[] languages)
        throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setContentMD5(String md5) throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setDataHandler(DataHandler handler) throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setDescription(String description) throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setDescription(String description, String charset)
        throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setDisposition(String disposition) throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setFileName(String name) throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setHeader(String name, String value)
        throws MessagingException {
        headers.setHeader(name, value);
    }
    public void setText(String text) throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setText(String text, String charset)
        throws MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    protected void updateHeaders() throws MessagingException {
    }
    public void writeTo(OutputStream out)
        throws IOException, MessagingException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }
}
