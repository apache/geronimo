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

import java.io.InputStream;

/**
 * An abstraction for POP3 Response
 * 
 * @see org.apache.geronimo.javamail.store.pop3.response.POP3ResponseFactory
 * @see org.apache.geronimo.javamail.store.pop3.response.DefaultPOP3Response
 * @see org.apache.geronimo.javamail.store.pop3.response.POP3StatusResponse
 * 
 * @version $Rev$ $Date$
 */
public interface POP3Response {

    /**
     * Returns the response OK or ERR
     * <ul>
     * <li>OK --> +OK in pop3 spec
     * <li>ERR --> -ERR in pop3 spec
     * </ul>
     */
    public int getStatus();

    /**
     * this corresponds to the line with the status however the status will be
     * removed and the remainder is returned. Ex. "+OK 132 3023673" is the first
     * line of response for a STAT command this method will return "132 3023673"
     * 
     * So any subsequent process can parse the params 132 as no of msgs and
     * 3023674 as the size.
     * 
     * @see org.apache.geronimo.javamail.store.pop3.response.POP3StatusResponse
     */
    public String getFirstLine();

    /**
     * This way we are not restricting anybody as InputStream.class is the most
     * basic type to represent an inputstream and ppl can decorate it anyway
     * they want, for ex BufferedInputStream or as an InputStreamReader allowing
     * maximum flexibility in using it.
     */
    public InputStream getData();
}
