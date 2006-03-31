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
 * Defines a few constants that are used throught the implementation.
 * 
 * @version $Rev$ $Date$
 */

public interface POP3Constants {
    public final static String SPACE = " ";

    public final static String CRLF = "\r\n";

    public final static int LF = '\n';

    public final static int CR = '\r';

    public final static int DOT = '.';

    public final static int OK = 0;

    public final static int ERR = 1;
}