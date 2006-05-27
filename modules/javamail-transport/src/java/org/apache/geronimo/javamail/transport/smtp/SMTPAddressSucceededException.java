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

package org.apache.geronimo.javamail.transport.smtp;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

public class SMTPAddressSucceededException extends MessagingException {
    // the succeeding address
    InternetAddress addr;

    // the failing command
    protected String cmd;

    // the error code for the failure
    protected int rc;

    /**
     * Constructor for an SMTPAddressSucceededException.
     * 
     * @param addr
     *            The succeeding address.
     * @param cmd
     *            The succeeding command string.
     * @param rc
     *            The error code for the command.
     * @param err
     *            An error message for the exception.
     */
    SMTPAddressSucceededException(InternetAddress addr, java.lang.String cmd, int rc, java.lang.String err) {
        super(err);
        this.cmd = cmd;
        this.rc = rc;
        this.addr = addr;
    }

    /**
     * Get the failing command string for the exception.
     * 
     * @return The string value of the failing command.
     */
    public String getCommand() {
        return cmd;
    }

    /**
     * The failing command return code.
     * 
     * @return The failure return code.
     */
    public int getReturnCode() {
        return rc;
    }

    /**
     * Retrieve the internet address associated with this exception.
     * 
     * @return The provided InternetAddress object.
     */
    public InternetAddress getAddress() {
        return addr;
    }
}
