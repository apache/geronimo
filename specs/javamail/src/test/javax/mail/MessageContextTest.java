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
package javax.mail;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.activation.DataHandler;
import javax.mail.internet.MimeMessage;
import junit.framework.TestCase;
/**
 * @version $Revision: 1.1 $ $Date: 2003/09/04 01:31:41 $
 */
public class MessageContextTest extends TestCase {
    public void testMessageContext() {
        Part p;
        MessageContext mc;
        p = new TestPart();
        mc = new MessageContext(p);
        assertSame(p, mc.getPart());
        assertNull(mc.getMessage());
        assertNull(mc.getSession());
        
        Session s = Session.getDefaultInstance(null);
        MimeMessage m = new MimeMessage(s);
        p = new TestMultipart(m);
        mc = new MessageContext(p);
        assertSame(p, mc.getPart());
        assertSame(m,mc.getMessage());
        assertSame(s,mc.getSession());
        
    }
    private static class TestMultipart extends Multipart implements Part {
        public TestMultipart(Part p) {
            parent = p;
        }
        public void writeTo(OutputStream out) throws IOException, MessagingException {
        }
        public void addHeader(String name, String value) throws MessagingException {
        }
        public Enumeration getAllHeaders() throws MessagingException {
            return null;
        }
        public Object getContent() throws IOException, MessagingException {
            return null;
        }
        public DataHandler getDataHandler() throws MessagingException {
            return null;
        }
        public String getDescription() throws MessagingException {
            return null;
        }
        public String getDisposition() throws MessagingException {
            return null;
        }
        public String getFileName() throws MessagingException {
            return null;
        }
        public String[] getHeader(String name) throws MessagingException {
            return null;
        }
        public InputStream getInputStream() throws IOException, MessagingException {
            return null;
        }
        public int getLineCount() throws MessagingException {
            return 0;
        }
        public Enumeration getMatchingHeaders(String[] names) throws MessagingException {
            return null;
        }
        public Enumeration getNonMatchingHeaders(String[] names) throws MessagingException {
            return null;
        }
        public int getSize() throws MessagingException {
            return 0;
        }
        public boolean isMimeType(String mimeType) throws MessagingException {
            return false;
        }
        public void removeHeader(String name) throws MessagingException {
        }
        public void setContent(Multipart content) throws MessagingException {
        }
        public void setContent(Object content, String type) throws MessagingException {
        }
        public void setDataHandler(DataHandler handler) throws MessagingException {
        }
        public void setDescription(String description) throws MessagingException {
        }
        public void setDisposition(String disposition) throws MessagingException {
        }
        public void setFileName(String name) throws MessagingException {
        }
        public void setHeader(String name, String value) throws MessagingException {
        }
        public void setText(String content) throws MessagingException {
        }        
    }
    private static class TestBodyPart extends BodyPart {
        public TestBodyPart(Multipart p) {
            super();
            parent = p;
        }
        public void addHeader(String name, String value)
            throws MessagingException {
        }
        public Enumeration getAllHeaders() throws MessagingException {
            return null;
        }
        public Object getContent() throws IOException, MessagingException {
            return null;
        }
        public String getContentType() throws MessagingException {
            return null;
        }
        public DataHandler getDataHandler() throws MessagingException {
            return null;
        }
        public String getDescription() throws MessagingException {
            return null;
        }
        public String getDisposition() throws MessagingException {
            return null;
        }
        public String getFileName() throws MessagingException {
            return null;
        }
        public String[] getHeader(String name) throws MessagingException {
            return null;
        }
        public InputStream getInputStream()
            throws IOException, MessagingException {
            return null;
        }
        public int getLineCount() throws MessagingException {
            return 0;
        }
        public Enumeration getMatchingHeaders(String[] names)
            throws MessagingException {
            return null;
        }
        public Enumeration getNonMatchingHeaders(String[] names)
            throws MessagingException {
            return null;
        }
        public int getSize() throws MessagingException {
            return 0;
        }
        public boolean isMimeType(String mimeType) throws MessagingException {
            return false;
        }
        public void removeHeader(String name) throws MessagingException {
        }
        public void setContent(Multipart content) throws MessagingException {
        }
        public void setContent(Object content, String type)
            throws MessagingException {
        }
        public void setDataHandler(DataHandler handler)
            throws MessagingException {
        }
        public void setDescription(String description)
            throws MessagingException {
        }
        public void setDisposition(String disposition)
            throws MessagingException {
        }
        public void setFileName(String name) throws MessagingException {
        }
        public void setHeader(String name, String value)
            throws MessagingException {
        }
        public void setText(String content) throws MessagingException {
        }
        public void writeTo(OutputStream out)
            throws IOException, MessagingException {
        }
    }
    private static class TestPart implements Part {
        public void addHeader(String name, String value)
            throws MessagingException {
        }
        public Enumeration getAllHeaders() throws MessagingException {
            return null;
        }
        public Object getContent() throws IOException, MessagingException {
            return null;
        }
        public String getContentType() throws MessagingException {
            return null;
        }
        public DataHandler getDataHandler() throws MessagingException {
            return null;
        }
        public String getDescription() throws MessagingException {
            return null;
        }
        public String getDisposition() throws MessagingException {
            return null;
        }
        public String getFileName() throws MessagingException {
            return null;
        }
        public String[] getHeader(String name) throws MessagingException {
            return null;
        }
        public InputStream getInputStream()
            throws IOException, MessagingException {
            return null;
        }
        public int getLineCount() throws MessagingException {
            return 0;
        }
        public Enumeration getMatchingHeaders(String[] names)
            throws MessagingException {
            return null;
        }
        public Enumeration getNonMatchingHeaders(String[] names)
            throws MessagingException {
            return null;
        }
        public int getSize() throws MessagingException {
            return 0;
        }
        public boolean isMimeType(String mimeType) throws MessagingException {
            return false;
        }
        public void removeHeader(String name) throws MessagingException {
        }
        public void setContent(Multipart content) throws MessagingException {
        }
        public void setContent(Object content, String type)
            throws MessagingException {
        }
        public void setDataHandler(DataHandler handler)
            throws MessagingException {
        }
        public void setDescription(String description)
            throws MessagingException {
        }
        public void setDisposition(String disposition)
            throws MessagingException {
        }
        public void setFileName(String name) throws MessagingException {
        }
        public void setHeader(String name, String value)
            throws MessagingException {
        }
        public void setText(String content) throws MessagingException {
        }
        public void writeTo(OutputStream out)
            throws IOException, MessagingException {
        }
    }
}
