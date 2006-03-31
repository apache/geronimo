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

package org.apache.geronimo.javamail.transport.nntp;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

/**
 * Util class to represent a reply from a NNTP server
 * 
 * @version $Rev$ $Date$
 */
public class NNTPReply {
    // general server responses
    public static final int POSTING_ALLOWED = 200;

    public static final int NO_POSTING_ALLOWED = 201;

    public static final int EXTENSIONS_SUPPORTED = 202;

    public static final int SERVICE_DISCONTINUED = 400;

    public static final int COMMAND_NOT_RECOGNIZED = 500;

    public static final int COMMAND_SYNTAX_ERROR = 501;

    public static final int PERMISSION_DENIED = 502;

    public static final int PROGRAM_FAULT = 503;

    // article responses
    public static final int ARTICLE_FOLLOWS = 220;

    public static final int HEAD_FOLLOWS = 221;

    public static final int BODY_FOLLOWS = 222;

    public static final int REQUEST_TEXT_SEPARATELY = 223;

    public static final int OVERVIEW_FOLLOWS = 224;

    public static final int NEW_ARTICLES_FOLLOWS = 230;

    public static final int NEW_GROUPS_FOLLOWS = 231;

    public static final int ARTICLE_TRANSFERRED = 235;

    public static final int NO_NEWSGROUP_SELECTED = 412;

    public static final int NO_ARTICLE_SELECTED = 420;

    public static final int NO_ARTICLE_NUMBER = 423;

    public static final int NO_ARTICLE_FOUND = 430;

    // group responses
    public static final int GROUP_SELECTED = 211;

    public static final int NO_SUCH_NEWSGROUP = 411;

    // post responses
    public static final int POSTED_OK = 240;

    public static final int SEND_ARTICLE = 340;

    public static final int POSTING_NOT_ALLOWED = 440;

    public static final int POSTING_FAILED = 441;

    // quit responses
    public static final int CLOSING_CONNECTION = 205;

    // authentication responses
    public static final int AUTHINFO_ACCEPTED = 250;

    public static final int AUTHINFO_ACCEPTED_FINAL = 251;

    public static final int AUTHINFO_CONTINUE = 350;

    public static final int AUTHINFO_CHALLENGE = 350;

    public static final int AUTHINFO_SIMPLE_REJECTED = 402;

    public static final int AUTHENTICATION_ACCEPTED = 281;

    public static final int MORE_AUTHENTICATION_REQUIRED = 381;

    public static final int AUTHINFO_REQUIRED = 480;

    public static final int AUTHINFO_SIMPLE_REQUIRED = 450;

    public static final int AUTHENTICATION_REJECTED = 482;

    // list active reponses
    public static final int LIST_FOLLOWS = 215;

    // The original reply string
    private final String reply;

    // returned message code
    private final int code;

    // the returned message text
    private final String message;

    // data associated with a long response command.
    private ArrayList data;

    NNTPReply(String s) throws MessagingException {
        // save the reply
        reply = s;

        // In a normal response, the first 3 must be the return code. However,
        // the response back from a QUIT command is frequently a null string.
        // Therefore, if the result is
        // too short, just default the code to -1 and use the entire text for
        // the message.
        if (s == null || s.length() < 3) {
            code = -1;
            message = s;
            return;
        }

        try {
            code = Integer.parseInt(s.substring(0, 3));

            // message should be separated by a space OR a continuation
            // character if this is a
            // multi-line response.
            if (s.length() > 4) {
                message = s.substring(4);
            } else {
                message = "";
            }
        } catch (NumberFormatException e) {
            throw new MessagingException("error in parsing reply code", e);
        }
    }

    /**
     * Retrieve data associated with a multi-line reponse from a server stream.
     * 
     * @param in
     *            The reader that's the source of the additional lines.
     * 
     * @exception IOException
     */
    public void retrieveData(BufferedReader in) throws MessagingException {
        try {
            data = new ArrayList();

            String line = in.readLine();
            // read until the end of file or until we see the end of data
            // marker.
            while (line != null && !line.equals(".")) {
                // this line is not the terminator, but it may have been byte
                // stuffed. If it starts with
                // '.', throw away the leading one.
                if (line.startsWith(".")) {
                    line = line.substring(1);
                }

                // just add the line to the list
                data.add(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            throw new MessagingException("Error reading message reply", e);
        }
    }

    /**
     * Retrieve the long-command data from this response.
     * 
     * @return The data list. Returns null if there is no associated data.
     */
    public List getData() {
        return data;
    }

    /**
     * Return the code value associated with the reply.
     * 
     * @return The integer code associated with the reply.
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Get the message text associated with the reply.
     * 
     * @return The string value of the message from the reply.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Retrieve the raw reply string for the reponse.
     * 
     * @return The original reply string from the server.
     */
    public String getReply() {
        return reply;
    }

    /**
     * Indicates if reply is an error condition
     */
    boolean isError() {
        // error codes are all above 400
        return code >= 400;
    }

    public String toString() {
        return "CODE = " + getCode() + " : MSG = " + getMessage();
    }
}
