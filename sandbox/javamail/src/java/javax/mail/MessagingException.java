/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package javax.mail;
/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:07 $
 */
public class MessagingException extends Exception {
    // Required because serialization expects it to be here
    private Exception next;
    public MessagingException() {
        super();
    }
    public MessagingException(String message) {
        super(message);
    }
    public MessagingException(String message, Exception cause) {
        super(message, cause);
        next = cause;
    }
    public Exception getNextException() {
        return next;
    }
    public synchronized boolean setNextException(Exception cause) {
        if (next == null) {
            initCause(cause);
            next = cause;
            return true;
        } else if (next instanceof MessagingException) {
            return ((MessagingException) next).setNextException(cause);
        } else {
            return false;
        }
    }
    public String getMessage() {
        Exception next = getNextException();
        if (next == null) {
            return super.getMessage();
        } else {
            return super.getMessage()
                + " ("
                + next.getClass().getName()
                + ": "
                + next.getMessage()
                + ")";
        }
    }
}
