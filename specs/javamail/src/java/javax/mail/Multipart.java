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
import java.io.OutputStream;
import java.util.Vector;
public abstract class Multipart {
    protected String contentType;
    protected Part parent;
    protected Vector parts = new Vector();
    protected Multipart() {
    }
    public void addBodyPart(BodyPart part) throws MessagingException {
        parts.add(part);
    }
    public void addBodyPart(BodyPart part, int pos) throws MessagingException {
        parts.add(pos, part);
    }
    public BodyPart getBodyPart(int index) throws MessagingException {
        return (BodyPart) parts.get(index);
    }
    public String getContentType() {
        return contentType;
    }
    public int getCount() throws MessagingException {
        return parts.size();
    }
    public Part getParent() {
        return parent;
    }
    public boolean removeBodyPart(BodyPart part) throws MessagingException {
        return parts.remove(part);
    }
    public void removeBodyPart(int index) throws MessagingException {
        parts.remove(index);
    }
    protected void setMultipartDataSource(MultipartDataSource mds)
        throws MessagingException {
        // TODO review implementation
        contentType = mds.getContentType();
        int size = mds.getCount();
        for (int i = 0; i < size; i++) {
            addBodyPart(mds.getBodyPart(i));
        }
    }
    public void setParent(Part part) {
        parent = part;
    }
    public abstract void writeTo(OutputStream out)
        throws IOException, MessagingException;
}
