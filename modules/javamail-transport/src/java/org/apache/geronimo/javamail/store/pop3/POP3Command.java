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

/**
 * An abstraction for POP3Commands
 * 
 * @see org.apache.geronimo.javamail.store.pop3.POP3CommandFactory
 * 
 * @version $Rev$ $Date$
 */
public interface POP3Command {

    /**
     * This method will get the POP3 command in string format according o
     * rfc1939
     */
    public String getCommand();

    /**
     * Indicates wether this command expects a multiline response or not
     * 
     */
    public boolean isMultiLineResponse();
}
