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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.xml.registry;

/**
 * Signals that a JAXR exception has occurred.
 * Note that the Exception chaining here is different from JDK1.4
 *
 * @version $Revision$ $Date$
 */
public class JAXRException extends Exception implements JAXRResponse {
    protected Throwable cause;

    public JAXRException() {
    }

    public JAXRException(String message) {
        super(message);
    }

    public JAXRException(Throwable cause) {
        super(cause);
        this.cause = cause;
    }

    public JAXRException(String message, Throwable cause) {
        super(message, cause);
        this.cause = cause;
    }

    public synchronized Throwable initCause(Throwable cause) {

        if (this.cause != null) {
            throw new IllegalStateException("Cannot overwrite cause.");
        }

        this.cause = cause;
        return this;
    }

    public String getMessage() {
        String message = super.getMessage();
        return (message == null && cause != null) ? cause.getMessage() : message;
    }

    public Throwable getCause() {
        return cause;
    }

    public String getRequestId() {
        return null;
    }

    public int getStatus() {
        return 0;
    }

    public boolean isAvailable() throws JAXRException {
        return true;
    }
}
