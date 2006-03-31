/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

package org.apache.geronimo.javamail.store.pop3.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.IllegalWriteException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.event.MessageChangedEvent;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;

import org.apache.geronimo.javamail.store.pop3.POP3CommandFactory;
import org.apache.geronimo.javamail.store.pop3.POP3Connection;
import org.apache.geronimo.javamail.store.pop3.POP3Folder;
import org.apache.geronimo.javamail.store.pop3.POP3Response;
import org.apache.geronimo.javamail.store.pop3.response.POP3ListResponse;
import org.apache.geronimo.javamail.store.pop3.response.POP3ResponseFactory;

/**
 * POP3 implementation of javax.mail.internet.MimeMessage
 * 
 * Only the most basic information is given and Message objects created here is
 * a light-weight reference to the actual Message As per the JavaMail spec items
 * from the actual message will get filled up on demand
 * 
 * If some other items are obtained from the server as a result of one call,
 * then the other details are also processed and filled in. For ex if RETR is
 * called then header information will also be processed in addition to the
 * content
 * 
 * @version $Rev$ $Date$
 */
public class POP3Message extends MimeMessage {

    private POP3Connection pop3Con;

    private int msgSize = -1;

    private int headerSize = -1;

    // We can't use header bcos it's already initialize to
    // to an empty InternetHeader
    private InputStream rawHeaders;

    // used to force loading of headers again
    private boolean loadHeaders = true;

    // to get accessed to the debug setting and log
    private Session session;

    protected POP3Message(Folder folder, int msgnum, Session session, POP3Connection pop3Con) {
        super(folder, msgnum);
        this.pop3Con = pop3Con;
        this.session = session;
    }

    /**
     * @see javax.mail.internet.MimeMessage#getContentStream()
     */
    protected InputStream getContentStream() throws MessagingException {
        POP3Response msgResponse = null;
        try {
            msgResponse = pop3Con.sendCommand(POP3CommandFactory.getCOMMAND_RETR(msgnum));
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadHeaders = true;
        loadHeaders(msgResponse.getData());
        loadContent(msgResponse.getData());

        return contentStream;
    }

    public void setFlags(Flags newFlags, boolean set) throws MessagingException {
        Flags oldFlags = (Flags) flags.clone();
        super.setFlags(newFlags, set);

        if (!flags.equals(oldFlags)) {
            ((POP3Folder) folder).notifyMessageChangedListeners(MessageChangedEvent.FLAGS_CHANGED, this);
        }
    }

    protected void loadHeaders(InputStream in) throws MessagingException {
        if (loadHeaders || rawHeaders == null) {
            rawHeaders = in;
            headers = new InternetHeaders(rawHeaders);
            loadHeaders = false;
        }
    }

    protected void loadContent(InputStream stream) throws MessagingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int byteRead = stream.read();
            int lastByte = -1;
            for (; byteRead > 0;) {
                if (byteRead == ' ' && lastByte == '\n') {
                    break;
                }
                lastByte = byteRead;
                byteRead = stream.read();
            }

            for (; stream.available() > 0;) {
                out.write(stream.read());
            }

            contentStream = new ByteArrayInputStream(out.toByteArray());
            msgSize = contentStream.available();

        } catch (IOException e) {

            throw new MessagingException("Error loading content info", e);
        }
    }

    public int getSize() throws MessagingException {
        if (msgSize >= 0) {
            return msgSize;
        }
        try {

            if (msgSize < 0) {
                if (rawHeaders == null) {
                    loadHeaders();
                }
                POP3ListResponse res = (POP3ListResponse) POP3ResponseFactory.getListResponse(pop3Con
                        .sendCommand(POP3CommandFactory.getCOMMAND_LIST(msgnum)));
                msgSize = res.getSize() - headerSize;
            }
            return msgSize;
        } catch (MessagingException ex) {
            throw new MessagingException("error getting size", ex);
        }
    }

    /**
     * notice that we pass zero as the no of lines from the message,as it
     * doesn't serv any purpose to get only a certain number of lines.
     * 
     * However this maybe important if a mail client only shows 3 or 4 lines of
     * the message in the list and then when the user clicks they would load the
     * message on demand.
     * 
     */
    protected void loadHeaders() throws MessagingException {
        POP3Response msgResponse = null;
        try {

            msgResponse = pop3Con.sendCommand(POP3CommandFactory.getCOMMAND_TOP(msgnum, 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadHeaders(msgResponse.getData());
    }

    /***************************************************************************
     * Following is a set of methods that deal with headers I have tried to use
     * the bare minimum
     * 
     * Used sun's POP3 impl & JavaMail API as a guide in decided which methods
     * are important.
     **************************************************************************/

    public String[] getHeader(String name) throws MessagingException {
        if (rawHeaders == null)
            loadHeaders();
        return headers.getHeader(name);
    }

    public String getHeader(String name, String delimiter) throws MessagingException {
        if (headers == null)
            loadHeaders();
        return headers.getHeader(name, delimiter);
    }

    public Enumeration getAllHeaders() throws MessagingException {
        if (headers == null)
            loadHeaders();
        return headers.getAllHeaders();
    }

    public Enumeration getMatchingHeaders(String[] names) throws MessagingException {
        if (headers == null)
            loadHeaders();
        return headers.getMatchingHeaders(names);
    }

    public Enumeration getNonMatchingHeaders(String[] names) throws MessagingException {
        if (headers == null)
            loadHeaders();
        return headers.getNonMatchingHeaders(names);
    }

    public Enumeration getAllHeaderLines() throws MessagingException {
        if (headers == null)
            loadHeaders();
        return headers.getAllHeaderLines();
    }

    public Enumeration getMatchingHeaderLines(String[] names) throws MessagingException {
        if (headers == null)
            loadHeaders();
        return headers.getMatchingHeaderLines(names);
    }

    public Enumeration getNonMatchingHeaderLines(String[] names) throws MessagingException {
        if (headers == null)
            loadHeaders();
        return headers.getNonMatchingHeaderLines(names);
    }

    // the following are overrides for header modification methods. These
    // messages are read only,
    // so the headers cannot be modified.
    public void addHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    public void setHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    public void removeHeader(String name) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    public void addHeaderLine(String line) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    /**
     * We cannot modify these messages
     */
    public void saveChanges() throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }
}
