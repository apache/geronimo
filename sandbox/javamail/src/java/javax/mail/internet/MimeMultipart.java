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
import java.io.OutputStream;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:28 $
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
