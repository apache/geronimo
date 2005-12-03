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
    private final int code;
    private final String message;
    private final char firstChar;

    SMTPReply(String s) throws MalformedSMTPReplyException {
        // first three must be the return code
        if (s == null || s.length() < 3) {
            throw new MalformedSMTPReplyException("Too Short! : " + s);
        }

        try {
            firstChar = s.charAt(0);
            code = Integer.parseInt(s.substring(0, 3));

            // message should be separated by a space
            if (s.length() > 4) {
                message = s.substring(4);
            } else {
                message = "";
            }
        } catch (NumberFormatException e) {
            throw new MalformedSMTPReplyException("error in parsing code", e);
        }
    }

    int getCode() {
        return this.code;
    }

    String getMessage() {
        return this.message;
    }

    /**
     * Indicates if reply is an error condition
     */
    boolean isError() {
        if (firstChar == '5' || firstChar == '4') {
            return true;
        }

        return false;
    }

    public String toString() {
        return "CODE = " + getCode() + " : MSG = " + getMessage();
    }
}
