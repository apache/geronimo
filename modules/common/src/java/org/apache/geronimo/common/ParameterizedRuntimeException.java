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

package org.apache.geronimo.common;

/**
 * Allows for an unchecked exception to be formatted using a given list of
 * parameters.
 * 
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:25 $
 */
public class ParameterizedRuntimeException
    extends RuntimeException
{
    /**
     * Default constructor. 
     */
    public ParameterizedRuntimeException() {
        super();
    }

    /**
     * Exception with a given message.
     *
     * @param message unformatted message.
     */
    public ParameterizedRuntimeException(String message) {
        super(message);
    }

    /**
     * Exception with a given message and root cause.
     *
     * @param message unformatted message.
     * @param cause root cause of the exception.
     */
    public ParameterizedRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Exception with a root cause.
     *
     * @param cause root cause of the exception.
     */
    public ParameterizedRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * Exception with a formatted message
     *
     * @param msg message format used by <tt>MessageFormat</tt>
     * @param params message parameters
     *
     * @see java.text.MessageFormat
     */
    public ParameterizedRuntimeException(String msg, Object[] params) {
        this(msg, params, null);
    }

    /**
     * Exception with a formatted message and root cause.
     *
     * @param msg message format used by <tt>MessageFormat</tt>
     * @param params message parameters
     * @param ex root cause
     *
     * @see java.text.MessageFormat
     */
    public ParameterizedRuntimeException(String msg, Object[] params, Throwable ex) {
        super(ParameterizedException.getFormattedMessage(msg, params), ex);
    }
}
