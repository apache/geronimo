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

package org.apache.geronimo.kernel.config;

/**
 * Exception indicating the requested ConfigurationStore could not be located.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/06/02 19:50:41 $
 */
public class NoSuchStoreException extends Exception {
    public NoSuchStoreException() {
    }

    public NoSuchStoreException(Throwable cause) {
        super(cause);
    }

    public NoSuchStoreException(String message) {
        super(message);
    }

    public NoSuchStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
