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
import java.net.UnknownServiceException;
import javax.activation.DataSource;
import javax.mail.MessageAware;
import javax.mail.MessageContext;
import javax.mail.MessagingException;

/**
 * @version $Rev$ $Date$
 */
public class MimePartDataSource implements DataSource, MessageAware {
    private final MimePart part;

    public MimePartDataSource(MimePart part) {
        this.part = part;
    }

    public InputStream getInputStream() throws IOException {
        try {
            InputStream stream;
            if (part instanceof MimeMessage) {
                stream = ((MimeMessage) part).getContentStream();
            } else if (part instanceof MimeBodyPart) {
                stream = ((MimeBodyPart) part).getContentStream();
            } else {
                throw new MessagingException("Unknown part");
            }
            String encoding = part.getEncoding();
            return encoding == null ? stream : MimeUtility.decode(stream, encoding);
        } catch (MessagingException e) {
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }
    }

    public OutputStream getOutputStream() throws IOException {
        throw new UnknownServiceException();
    }

    public String getContentType() {
        try {
            return part.getContentType();
        } catch (MessagingException e) {
            return null;
        }
    }

    public String getName() {
        return "";
    }

    public synchronized MessageContext getMessageContext() {
        return new MessageContext(part);
    }
}
