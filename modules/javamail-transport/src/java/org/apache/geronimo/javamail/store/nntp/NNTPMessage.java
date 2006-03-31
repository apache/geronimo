/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.javamail.store.nntp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.mail.Flags;
import javax.mail.IllegalWriteException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;

import org.apache.geronimo.javamail.transport.nntp.NNTPConnection;
import org.apache.geronimo.javamail.transport.nntp.NNTPReply;
import org.apache.geronimo.javamail.transport.nntp.StringListInputStream;

/**
 * NNTP implementation of javax.mail.internet.MimeMessage
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
public class NNTPMessage extends MimeMessage {
    // the server message identifer
    String messageID = null;

    // our attached session
    protected Session session;

    // the Store we're stored in (which manages the connection and other stuff).
    protected NNTPStore store;

    // our active connection.
    protected NNTPConnection connection;

    // used to force loading of headers
    protected boolean headersLoaded = false;

    // use to force content loading
    protected boolean contentLoaded = false;

    /**
     * Contruct an NNTPMessage instance.
     * 
     * @param folder
     *            The hosting folder for the message.
     * @param store
     *            The Store owning the article (and folder).
     * @param msgnum
     *            The article message number.
     * @param messageID
     *            The article messageID (as assigned by the server).
     * 
     * @exception MessagingException
     */
    NNTPMessage(NNTPFolder folder, NNTPStore store, int msgnum, String messageID) throws MessagingException {
        super(folder, msgnum);
        this.messageID = messageID;
        this.store = store;
        this.session = ((NNTPStore) store).getSession();
        // get the active connection from the store...all commands are sent
        // there
        this.connection = ((NNTPStore) store).getConnection();

        // get our flag set from the folder.
        flags = folder.getPermanentFlags();
        // now check our initial SEEN state and set the flags appropriately
        if (folder.isSeen(msgnum)) {
            flags.add(Flags.Flag.SEEN);
        } else {
            flags.remove(Flags.Flag.SEEN);
        }
    }

    /**
     * Retrieve the size of the message content. The content will be retrieved
     * from the server, if necessary.
     * 
     * @return The size of the content.
     * @exception MessagingException
     */
    public int getSize() throws MessagingException {
        // make sure we've retrieved the message content and continue with the
        // superclass version.
        loadContent();
        return super.getSize();
    }

    /**
     * Get a line count for the NNTP message. This is potentially stored in the
     * Lines article header. If not there, we return a default of -1.
     * 
     * @return The header line count estimate, or -1 if not retrieveable.
     * @exception MessagingException
     */
    public int getLineCount() throws MessagingException {
        String[] headers = getHeader("Lines");

        // hopefully, there's only a single one of these. No sensible way of
        // interpreting
        // multiples.
        if (headers.length == 1) {
            try {
                return Integer.parseInt(headers[0].trim());

            } catch (NumberFormatException e) {
                // ignore
            }
        }
        // dunno...and let them know I don't know.
        return -1;
    }

    /**
     * @see javax.mail.internet.MimeMessage#getContentStream()
     */
    protected InputStream getContentStream() throws MessagingException {
        // get the article information.
        loadArticle();
        return super.getContentStream();
    }

    /***************************************************************************
     * Following is a set of methods that deal with headers These methods are
     * just overrides on the superclass methods to allow lazy loading of the
     * header information.
     **************************************************************************/

    public String[] getHeader(String name) throws MessagingException {
        loadHeaders();
        return headers.getHeader(name);
    }

    public String getHeader(String name, String delimiter) throws MessagingException {
        loadHeaders();
        return headers.getHeader(name, delimiter);
    }

    public Enumeration getAllHeaders() throws MessagingException {
        loadHeaders();
        return headers.getAllHeaders();
    }

    public Enumeration getMatchingHeaders(String[] names) throws MessagingException {
        loadHeaders();
        return headers.getMatchingHeaders(names);
    }

    public Enumeration getNonMatchingHeaders(String[] names) throws MessagingException {
        loadHeaders();
        return headers.getNonMatchingHeaders(names);
    }

    public Enumeration getAllHeaderLines() throws MessagingException {
        loadHeaders();
        return headers.getAllHeaderLines();
    }

    public Enumeration getMatchingHeaderLines(String[] names) throws MessagingException {
        loadHeaders();
        return headers.getMatchingHeaderLines(names);
    }

    public Enumeration getNonMatchingHeaderLines(String[] names) throws MessagingException {
        loadHeaders();
        return headers.getNonMatchingHeaderLines(names);
    }

    // the following are overrides for header modification methods. These
    // messages are read only,
    // so the headers cannot be modified.
    public void addHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("NNTP messages are read-only");
    }

    public void setHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("NNTP messages are read-only");
    }

    public void removeHeader(String name) throws MessagingException {
        throw new IllegalWriteException("NNTP messages are read-only");
    }

    public void addHeaderLine(String line) throws MessagingException {
        throw new IllegalWriteException("IMAP messages are read-only");
    }

    /**
     * We cannot modify these messages
     */
    public void saveChanges() throws MessagingException {
        throw new IllegalWriteException("NNTP messages are read-only");
    }

    /**
     * Retrieve the message headers from the NNTP server.
     * 
     * @exception MessagingException
     */
    public void loadHeaders() throws MessagingException {
        // don't retrieve if already loaded.
        if (headersLoaded) {
            return;
        }

        NNTPReply reply = connection.sendCommand("HEAD " + messageID, NNTPReply.HEAD_FOLLOWS);

        if (reply.getCode() == NNTPReply.HEAD_FOLLOWS) {
            try {
                // wrap a stream around the reply data and read as headers.
                updateHeaders(new StringListInputStream(reply.getData()));
            } catch (IOException e) {
                throw new MessagingException("Error retrieving article headers from server", e);
            }
        } else {
            throw new MessagingException("Error retrieving article headers from server: " + reply);
        }
    }

    /**
     * Update the message headers from an input stream.
     * 
     * @param in
     *            The InputStream source for the header information.
     * 
     * @exception MessagingException
     */
    public void updateHeaders(InputStream in) throws MessagingException {
        // wrap a stream around the reply data and read as headers.
        headers = new InternetHeaders(in);
        headersLoaded = true;
    }

    /**
     * Load just the message content from the NNTP server.
     * 
     * @exception MessagingException
     */
    public void loadContent() throws MessagingException {
        if (contentLoaded) {
            return;
        }

        NNTPReply reply = connection.sendCommand("BODY " + messageID, NNTPReply.BODY_FOLLOWS);

        if (reply.getCode() == NNTPReply.BODY_FOLLOWS) {
            try {
                InputStream in = new StringListInputStream(reply.getData());
                updateContent(in);
            } catch (IOException e) {
                throw new MessagingException("Error retrieving article body from server", e);
            }
        } else {
            throw new MessagingException("Error retrieving article body from server: " + reply);
        }
    }

    /**
     * Load the entire article from the NNTP server. This updates both the
     * headers and the content.
     * 
     * @exception MessagingException
     */
    public void loadArticle() throws MessagingException {
        // if the headers are already loaded, retrieve the content portion.
        if (headersLoaded) {
            loadContent();
            return;
        }

        // we need to retrieve everything.
        NNTPReply reply = connection.sendCommand("ARTICLE " + messageID, NNTPReply.ARTICLE_FOLLOWS);

        if (reply.getCode() == NNTPReply.ARTICLE_FOLLOWS) {
            try {
                InputStream in = new StringListInputStream(reply.getData());
                // update both the headers and the content.
                updateHeaders(in);
                updateContent(in);
            } catch (IOException e) {
                throw new MessagingException("Error retrieving article from server", e);
            }
        } else {
            throw new MessagingException("Error retrieving article from server: " + reply);
        }
    }

    /**
     * Update the article content from an input stream.
     * 
     * @param in
     *            The content data source.
     * 
     * @exception MessagingException
     */
    public void updateContent(InputStream in) throws MessagingException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];

            // copy the content data from the stream into a byte buffer for the
            // content.
            while (true) {
                int read = in.read(buffer);
                if (read == -1) {
                    break;
                }
                out.write(buffer, 0, read);
            }

            content = out.toByteArray();
            contentLoaded = true;
        } catch (IOException e) {
            throw new MessagingException("Error retrieving message body from server", e);
        }
    }

    /**
     * Get the server assigned messageid for the article.
     * 
     * @return The server assigned message id.
     */
    public String getMessageId() {
        return messageID;
    }

    /**
     * Override of setFlags(). We need to ensure that if the SEEN flag is set or
     * cleared, that the newsrc file correctly reflects the current state.
     * 
     * @param flag
     *            The flag being set.
     * @param newvalue
     *            The new flag value.
     * 
     * @exception MessagingException
     */
    public void setFlags(Flags flag, boolean newvalue) throws MessagingException {
        // if this is the SEEN flag, make sure we shadow this in the newsrc
        // file.
        if (flag.contains(Flags.Flag.SEEN)) {
            ((NNTPFolder) folder).setSeen(msgnum, newvalue);
        }
        // have the superclass do the real flag setting.
        super.setFlags(flag, newvalue);
    }
}
