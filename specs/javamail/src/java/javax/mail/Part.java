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

package javax.mail;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.activation.DataHandler;
/**
 * @version $Rev$ $Date$
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
