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

package org.apache.geronimo.validator;

/**
 * Used by the provided validation logic to indicate that a fatal error has
 * occured and the validation has failed.  Typically tests should not use this,
 * instead returning a ValidationResult to indicate an error.  This should only
 * be used when there's an urgent need to abort.
 *
 * It is a runtime exception because no user-provided validation code needs to
 * catch it; it will be trapped by the core validator implementation. 
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:29 $
 */
public class ValidationException extends RuntimeException {
    public ValidationException() {
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
