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

package javax.resource;

/**
 *
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:45 $
 */
public class ResourceException extends Exception {
    private String errorCode;
    private Exception linkedException;

    public ResourceException() {
        super();
    }

    public ResourceException(String message) {
        super(message);
    }

    public ResourceException(Throwable cause) {
        super(cause);
    }

    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceException(String message, String errorCode) {
        super(message);
        setErrorCode(errorCode);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @deprecated
     */
    public Exception getLinkedException() {
        return linkedException;
    }

    /**
     * @deprecated
     */
    public void setLinkedException(Exception ex) {
        // unit tests revealed that Throwable.initCause is not invoked
        this.linkedException = ex;
    }

    public String toString() {
        // unit tests revealed that the errorCode is not included
        return getMessage();
    }
}
