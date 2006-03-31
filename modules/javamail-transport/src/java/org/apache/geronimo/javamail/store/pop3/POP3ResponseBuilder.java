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

package org.apache.geronimo.javamail.store.pop3;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.Session;

import org.apache.geronimo.javamail.store.pop3.response.POP3ResponseFactory;

;

/**
 * Builds a basic response out of the input stream received by the connection.
 * Performs only two basic functions
 * <ul>
 * <li>Extrats the status code</li>
 * <li>If multi-line response then extract the data as an input stream</li>
 * </ul>
 * 
 * @version $Rev$ $Date$
 */

public final class POP3ResponseBuilder implements POP3Constants {

    public static POP3Response buildResponse(Session session, BufferedReader reader, boolean isMultiLineResponse)
            throws MessagingException {

        int status = ERR;
        InputStream data = null;

        String line;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            throw new MessagingException("Error in receving response");
        }
        if (line == null || line.trim().equals("")) {
            if (session.getDebug()) {
                session.getDebugOut().println("Empty Response");
            }
            throw new MessagingException("Empty Response");
        }
        if (session.getDebug()) {
            session.getDebugOut().println("Response From Server " + line);
        }

        if (line.startsWith("+OK")) {
            status = OK;
            line = removeStatusField(line);
            if (isMultiLineResponse) {
                data = getMultiLineResponse(session, reader);
            }
        } else if (line.startsWith("-ERR")) {
            status = ERR;
            line = removeStatusField(line);
        } else {
            throw new MessagingException("Unexpected response: " + line);
        }

        return POP3ResponseFactory.getDefaultResponse(status, line, data);
    }

    private static String removeStatusField(String line) {
        return line.substring(line.indexOf(SPACE) + 1);
    }

    /**
     * This could be a multiline response
     */
    private static InputStream getMultiLineResponse(Session session, BufferedReader reader) throws MessagingException {

        int byteRead = -1;
        int lastByteRead = LF;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            while ((byteRead = reader.read()) >= 0) {
                // We are checking for the end of a multiline response
                // the format is .CRLF

                // checking for the DOT and CR
                if (lastByteRead == DOT && byteRead == CR) {
                    byteRead = reader.read();
                    // now checking for the LF of the second CRLF
                    if (byteRead == LF) {
                        // end of response
                        break;
                    }
                }

                out.write(byteRead);
                lastByteRead = byteRead;
            }

            if (session.getDebug()) {
                session.getDebugOut().println("\n============================ Response Content==================\n");
                session.getDebugOut().write(out.toByteArray());
                session.getDebugOut().println("\n==============================================================\n");
            }

        } catch (IOException e) {
            throw new MessagingException("Error processing a multi-line response", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

}
