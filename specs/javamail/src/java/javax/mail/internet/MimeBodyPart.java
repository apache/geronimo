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
 * @version $Revision: 1.1 $ $Date: 2003/08/16 01:55:48 $
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
