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
import java.io.PrintStream;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.MultipartDataSource;

/**
 * @version $Rev$ $Date$
 */
public class MimeMultipart extends Multipart {
    /**
     * DataSource that provides our InputStream.
     */
    protected DataSource ds;
    /**
     * Indicates if the data has been parsed.
     */
    protected boolean parsed;

    private transient ContentType type;

    /**
     * Create an empty MimeMultipart with content type "multipart/mixed"
     */
    public MimeMultipart() {
        this("mixed");
    }

    /**
     * Create an empty MimeMultipart with the subtype supplied.
     *
     * @param subtype the subtype
     */
    public MimeMultipart(String subtype) {
        type = new ContentType("multipart", subtype, null);
        type.setParameter("boundary", getBoundary());
        contentType = type.toString();
    }

    /**
     * Create a MimeMultipart from the supplied DataSource.
     *
     * @param dataSource the DataSource to use
     * @throws MessagingException
     */
    public MimeMultipart(DataSource dataSource) throws MessagingException {
        ds = dataSource;
        if (dataSource instanceof MultipartDataSource) {
            super.setMultipartDataSource((MultipartDataSource) dataSource);
            parsed = true;
        } else {
            type = new ContentType(ds.getContentType());
            contentType = type.toString();
            parsed = false;
        }
    }

    public void setSubType(String subtype) throws MessagingException {
        type.setSubType(subtype);
        contentType = type.toString();
    }

    public int getCount() throws MessagingException {
        parse();
        return super.getCount();
    }

    public synchronized BodyPart getBodyPart(int part) throws MessagingException {
        parse();
        return super.getBodyPart(part);
    }

    public BodyPart getBodyPart(String cid) throws MessagingException {
        parse();
        for (int i = 0; i < parts.size(); i++) {
            MimeBodyPart bodyPart = (MimeBodyPart) parts.get(i);
            if (cid.equals(bodyPart.getContentID())) {
                return bodyPart;
            }
        }
        return null;
    }

    protected void updateHeaders() throws MessagingException {
        parse();
        for (int i = 0; i < parts.size(); i++) {
            MimeBodyPart bodyPart = (MimeBodyPart) parts.get(i);
            bodyPart.updateHeaders();
        }
    }

    private static byte[] dash = { '-', '-' };

    public void writeTo(OutputStream out) throws IOException, MessagingException {
        parse();
        String boundary = type.getParameter("boundary");
        byte[] bytes = boundary.getBytes();
        PrintStream pos = new PrintStream(out, false);
        for (int i = 0; i < parts.size(); i++) {
            BodyPart bodyPart = (BodyPart) parts.get(i);
            pos.print(dash);
            pos.println(bytes);
            bodyPart.writeTo(pos);
            pos.println();
        }
        pos.print(dash);
        pos.print(bytes);
        pos.println(dash);
        pos.flush();
    }

    protected void parse() throws MessagingException {
        if (parsed) {
            return;
        }
        parsed = true;
    }

    protected InternetHeaders createInternetHeaders(InputStream in) throws MessagingException {
        return new InternetHeaders(in);
    }

    protected MimeBodyPart createMimeBodyPart(InternetHeaders headers, byte[] data) throws MessagingException {
        return new MimeBodyPart(headers, data);
    }

    protected MimeBodyPart createMimeBodyPart(InputStream in) throws MessagingException {
        return new MimeBodyPart(in);
    }

    private static int part;

    private synchronized static String getBoundary() {
        int i;
        synchronized(MimeMultipart.class) {
            i = part++;
        }
        StringBuffer buf = new StringBuffer(64);
        buf.append("----=_Part_").append(i).append('.').append(System.currentTimeMillis());
        return buf.toString();
    }
}
