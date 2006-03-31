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

import java.io.InputStream;

import javax.mail.MessagingException;

import org.apache.geronimo.javamail.store.pop3.POP3Constants;
import org.apache.geronimo.javamail.store.pop3.POP3Response;

/**
 * This factory provides a uniform way of handling the creation of response
 * objects.
 * 
 * @version $Rev$ $Date$
 */

public final class POP3ResponseFactory implements POP3Constants {

    public static POP3Response getDefaultResponse(int status, String line, InputStream data) {
        return new DefaultPOP3Response(status, line, data);
    }

    public static POP3Response getStatusResponse(POP3Response baseRes) throws MessagingException {
        return new POP3StatusResponse(baseRes);
    }

    public static POP3Response getListResponse(POP3Response baseRes) throws MessagingException {
        return new POP3StatusResponse(baseRes);
    }

}
