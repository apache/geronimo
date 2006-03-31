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

package org.apache.geronimo.javamail.transport.smtp;

/**
 * Util class to represent a reply from a SMTP server
 * 
 * @version $Rev$ $Date$
 */
class SMTPReply {
    // The original reply string
    private final String reply;

    // returned message code
    private final int code;

    // the returned message text
    private final String message;

    // indicates that this is a continuation response
    private boolean continued;

    SMTPReply(String s) throws MalformedSMTPReplyException {
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
            continued = false;
            code = Integer.parseInt(s.substring(0, 3));

            // message should be separated by a space OR a continuation
            // character if this is a
            // multi-line response.
            if (s.length() > 4) {
                //
                if (s.charAt(3) == '-') {
                    continued = true;
                }
                message = s.substring(4);
            } else {
                message = "";
            }
        } catch (NumberFormatException e) {
            throw new MalformedSMTPReplyException("error in parsing code", e);
        }
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

    /**
     * Indicates whether this response is flagged as part of a multiple line
     * response.
     * 
     * @return true if the response has multiple lines, false if this is the
     *         last line of the response.
     */
    public boolean isContinued() {
        return continued;
    }

    public String toString() {
        return "CODE = " + getCode() + " : MSG = " + getMessage();
    }
}
