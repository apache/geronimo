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

import org.apache.geronimo.javamail.store.pop3.POP3Constants;
import org.apache.geronimo.javamail.store.pop3.POP3Response;

/**
 * This class provides the basic implementation for the POP3Response.
 * 
 * @see org.apache.geronimo.javamail.store.pop3.POP3Response
 * @version $Rev$ $Date$
 */

public class DefaultPOP3Response implements POP3Response, POP3Constants {

    private int status = ERR;

    private String firstLine;

    private InputStream data;

    DefaultPOP3Response(int status, String firstLine, InputStream data) {
        this.status = status;
        this.firstLine = firstLine;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public InputStream getData() {
        return data;
    }

    public String getFirstLine() {
        return firstLine;
    }

}
