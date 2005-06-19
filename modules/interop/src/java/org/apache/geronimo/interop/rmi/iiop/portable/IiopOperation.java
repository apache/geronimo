/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.interop.rmi.iiop.portable;

import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public class IiopOperation {
    private final String name;
    private final Method method;

    public IiopOperation(String name, Method method) {
        assert name != null;
        assert method != null;
        this.method = method;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Method getMethod() {
        return method;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        if (other instanceof IiopOperation) {
            IiopOperation iiopOperation = (IiopOperation) other;
            return name.equals(iiopOperation.name);
        }
        return false;
    }
}
