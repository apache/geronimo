/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.gbean;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:00 $
 */
public class WaitingException extends Exception {
    public WaitingException() {
    }

    public WaitingException(String message) {
        super(message);
    }

    public WaitingException(String message, Throwable cause) {
        super(message, cause);
    }

    public WaitingException(Throwable cause) {
        super(cause);
    }
}
