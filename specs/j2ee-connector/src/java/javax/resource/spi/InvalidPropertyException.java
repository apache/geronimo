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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.resource.spi;

import java.beans.PropertyDescriptor;
import javax.resource.ResourceException;

/**
 *
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:28 $
 */
public class InvalidPropertyException extends ResourceException {
    private PropertyDescriptor[] invalidProperties;

    public InvalidPropertyException() {
        super();
    }

    public InvalidPropertyException(String message) {
        super(message);
    }

    public InvalidPropertyException(Throwable cause) {
        super(cause);
    }

    public InvalidPropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPropertyException(String message, String errorCode) {
        super(message, errorCode);
    }

    public void setInvalidPropertyDescriptors(PropertyDescriptor[] invalidProperties) {
        this.invalidProperties = invalidProperties;
    }

    public PropertyDescriptor[] getInvalidPropertyDescriptors() {
        return invalidProperties;
    }
}