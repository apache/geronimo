/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop;

/*
 * This class is used to help marshall server side exceptions back to the client.
 * A server exception is caught and then set as a cause of the system exception.
 *
 * Originally there were two different implementations of the this cause, one for 
 * JDK 1.3 (which didn't have the cause object on an Exception) and this one for JDK 1.4
 * The JDK 1.3 class has been removed.
 *
 * In the stubs/skeletons, they check to see if the marshalled exception is an instance of
 * SystemException and then it take appropriate actions.
 *
 * Its possible that this could change to use the JDK 1.4 RuntimeException.  This may 
 * cause some troubles with identification of specific server side exceptions vs. a
 * general runtime exception.
 *
 * For now, I am going to leave this class in place.  A future TODO would be to investigate
 * this situation more carefully.
 */

public class SystemException extends RuntimeException {
    public SystemException(String message) {
        super(message);
    }

    public SystemException(Throwable cause) {
        super(cause != null && cause instanceof SystemException
              && cause.getMessage() == null
              ? cause.getCause() : cause);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause != null && cause instanceof SystemException
                       && cause.getMessage() == null
                       ? cause.getCause() : cause);
    }
}
