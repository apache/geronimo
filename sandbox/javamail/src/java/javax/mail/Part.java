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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.activation.DataHandler;
/**
 * @version $Revision: 1.1 $ $Date: 2004/01/29 04:20:02 $
 */
public interface Part {
    public static final String ATTACHMENT = "attachment";
    public static final String INLINE = "inline";
    public abstract void addHeader(String name, String value)
        throws MessagingException;
    public abstract Enumeration getAllHeaders() throws MessagingException;
    public abstract Object getContent() throws IOException, MessagingException;
    public abstract String getContentType() throws MessagingException;
    public abstract DataHandler getDataHandler() throws MessagingException;
    public abstract String getDescription() throws MessagingException;
    public abstract String getDisposition() throws MessagingException;
    public abstract String getFileName() throws MessagingException;
    public abstract String[] getHeader(String name) throws MessagingException;
    public abstract InputStream getInputStream()
        throws IOException, MessagingException;
    public abstract int getLineCount() throws MessagingException;
    public abstract Enumeration getMatchingHeaders(String[] names)
        throws MessagingException;
    public abstract Enumeration getNonMatchingHeaders(String[] names)
        throws MessagingException;
    public abstract int getSize() throws MessagingException;
    public abstract boolean isMimeType(String mimeType)
        throws MessagingException;
    public abstract void removeHeader(String name) throws MessagingException;
    public abstract void setContent(Multipart content)
        throws MessagingException;
    public abstract void setContent(Object content, String type)
        throws MessagingException;
    public abstract void setDataHandler(DataHandler handler)
        throws MessagingException;
    public abstract void setDescription(String description)
        throws MessagingException;
    public abstract void setDisposition(String disposition)
        throws MessagingException;
    public abstract void setFileName(String name) throws MessagingException;
    public abstract void setHeader(String name, String value)
        throws MessagingException;
    public abstract void setText(String content) throws MessagingException;
    public abstract void writeTo(OutputStream out)
        throws IOException, MessagingException;
}
