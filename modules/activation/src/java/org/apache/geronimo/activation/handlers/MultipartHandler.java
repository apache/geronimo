/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.activation.handlers;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.OutputStream;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

/**
 * @version $Rev$ $Date$
 */
public class MultipartHandler implements DataContentHandler {
    private final DataFlavor flavour;

    public MultipartHandler() {
        flavour = new ActivationDataFlavor(MimeMultipart.class, "multipart/mixed", "Multipart MIME");
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{flavour};
    }

    public Object getTransferData(DataFlavor df, DataSource ds) throws UnsupportedFlavorException, IOException {
        return flavour.equals(df) ? getContent(ds) : null;
    }

    public Object getContent(DataSource ds) throws IOException {
        try {
            return new MimeMultipart(ds);
        } catch (MessagingException e) {
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }
    }

    public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException {
        if (obj instanceof MimeMultipart) {
            MimeMultipart mp = (MimeMultipart) obj;
            try {
                mp.writeTo(os);
            } catch (MessagingException e) {
                throw (IOException) new IOException(e.getMessage()).initCause(e);
            }
        }
    }
}
