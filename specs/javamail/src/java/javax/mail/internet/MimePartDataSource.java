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
    private MimePart _part;

    public MimePartDataSource(MimePart part) {
        _part = part;
    }

    public String getContentType() {
        try {
            return _part.getContentType();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream getInputStream() throws IOException {
        InputStream content;
        try {
            String encoding = _part.getEncoding();
            if (_part instanceof MimeMessage) {
                content = ((MimeMessage) _part).getContentStream();
            } else if (_part instanceof MimeBodyPart) {
                content = ((MimeBodyPart) _part).getContentStream();
            } else {
                throw new MessagingException("Unknown part");
            }
            return MimeUtility.decode(content, encoding);
        } catch (MessagingException e) {
            throw new IOException(e.toString());
        }
    }

    public synchronized MessageContext getMessageContext() {
        return new MessageContext(_part);
    }

    public String getName() {
        return "";
    }

    public OutputStream getOutputStream() throws IOException {
        throw new UnknownServiceException();
    }
}
