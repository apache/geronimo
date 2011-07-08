/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.lang.reflect.Method;
import java.util.List;

/**
 * This is a key class based on a MBean operation name and parameters.
 *
 * @version $Rev$ $Date$
 */
public final class GOperationSignature {
    private final static String[] NO_TYPES = new String[0];
    private final String name;
    private final String[] argumentTypes;

    public GOperationSignature(Method method) {
        name = method.getName();
        Class[] parameters = method.getParameterTypes();
        argumentTypes = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            argumentTypes[i] = parameters[i].getName();
        }
    }

    public GOperationSignature(String name, String[] argumentTypes) {
        this.name = name;
        if (argumentTypes != null) {
            this.argumentTypes = argumentTypes;
        } else {
            this.argumentTypes = NO_TYPES;
        }
    }

    public GOperationSignature(String name, List argumentTypes) {
        this.name = name;
        if (argumentTypes != null) {
            this.argumentTypes = new String[argumentTypes.size()];
            for (int i = 0; i < argumentTypes.size(); i++) {
                this.argumentTypes[i] = (String) argumentTypes.get(i);
            }
        } else {
            this.argumentTypes = NO_TYPES;
        }
    }

    public boolean equals(Object object) {
        if (!(object instanceof GOperationSignature)) {
            return false;
        }

        // match names
        GOperationSignature methodKey = (GOperationSignature) object;
        if (!methodKey.name.equals(name)) {
            return false;
        }

        // match arg length
        int length = methodKey.argumentTypes.length;
        if (length != argumentTypes.length) {
            return false;
        }

        // match each arg
        for (int i = 0; i < length; i++) {
            if (!methodKey.argumentTypes[i].equals(argumentTypes[i])) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + name.hashCode();
        for (int i = 0; i < argumentTypes.length; i++) {
            result = 37 * result + argumentTypes[i].hashCode();
        }
        return result;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(name).append("(");
        for (int i = 0; i < argumentTypes.length; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append(argumentTypes[i]);
        }
        return buffer.append(")").toString();
    }
}
