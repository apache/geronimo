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

package org.apache.geronimo.javamail.store.pop3.response;

import javax.mail.MessagingException;

import org.apache.geronimo.javamail.store.pop3.POP3Response;

/**
 * This class adds functionality to the basic response by parsing the status
 * line and obtaining specific information about num of msgs and the size
 * 
 * @see org.apache.geronimo.javamail.store.pop3.POP3Response
 * @see org.apache.geronimo.javamail.store.pop3.response.DefaultPOP3Response
 * 
 * @version $Rev$ $Date$
 */

public class POP3StatusResponse extends DefaultPOP3Response {

    private int numMessages = 0;

    private int size = 0;

    POP3StatusResponse(POP3Response baseRes) throws MessagingException {
        super(baseRes.getStatus(), baseRes.getFirstLine(), baseRes.getData());

        // if ERR not worth proceeding any further
        if (OK == getStatus()) {
            String[] args = getFirstLine().split(SPACE);
            try {
                numMessages = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                throw new MessagingException("Invalid response for STAT command", e);
            }
            try {
                size = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                throw new MessagingException("Invalid response for STAT command", e);
            }
        }
    }

    public int getNumMessages() {
        return numMessages;
    }

    public int getSize() {
        return size;
    }

}
