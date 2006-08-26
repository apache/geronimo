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

package org.apache.geronimo.gbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Describes an operation on a GBean.
 *
 * @version $Rev$ $Date$
 */
public class GOperationInfo implements Serializable {
    /**
     * The name of this method.
     */
    private final String name;

    /**
     * Parameters of this method.
     */
    private final List parameters;

    /**
     * Target method name.
     */
    private final String methodName;

    public GOperationInfo(String name) {
        this(name, name, Collections.EMPTY_LIST);
    }

    public GOperationInfo(String name, Class[] paramTypes) {
        this.name = this.methodName = name;
        String[] args = new String[paramTypes.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = paramTypes[i].getName();
        }
        this.parameters = Collections.unmodifiableList(Arrays.asList(args));
    }

    public GOperationInfo(String name, String[] paramTypes) {
        this(name, name, Arrays.asList(paramTypes));
    }

    public GOperationInfo(String name, List parameters) {
        this(name, name, parameters);
    }

    public GOperationInfo(String name, String methodName, List parameters) {
        this.name = name;
        this.methodName = methodName;
        this.parameters = Collections.unmodifiableList(new ArrayList(parameters));
    }

    public String getName() {
        return name;
    }

    public String getMethodName() {
        return methodName;
    }

    public List getParameterList() {
        return parameters;
    }

    public String toString() {
        return "[GOperationInfo: name=" + name + " parameters=" + parameters + "]";
    }
}
