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
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
/**
 * @version $Revision: 1.1 $ $Date: 2004/01/29 04:20:04 $
 */
public class MimeMultipart extends Multipart {
    protected DataSource ds;
    protected boolean parsed;
    public MimeMultipart() {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public MimeMultipart(DataSource dataSource) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public MimeMultipart(String subtype) {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    protected InternetHeaders createInternetHeaders(InputStream in)
        throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    protected MimeBodyPart createMimeBodyPart(InputStream in)
        throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    protected MimeBodyPart createMimeBodyPart(
        InternetHeaders headers,
        byte[] data)
        throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public synchronized BodyPart getBodyPart(int part)
        throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public BodyPart getBodyPart(String cid) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public int getCount() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    protected void parse() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void setSubType(String subtype) throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    protected void updateHeaders() throws MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
    public void writeTo(OutputStream out)
        throws IOException, MessagingException {
        // TODO Implement method
        throw new UnsupportedOperationException("Method not yet implemented");
    }
}
