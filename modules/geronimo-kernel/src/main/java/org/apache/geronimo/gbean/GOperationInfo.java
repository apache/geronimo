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
     * The return type of this method.
     */
    private final String type;
    
    /**
     * Parameters of this method.
     */
    private final List parameters;

    /**
     * Target method name.
     */
    private final String methodName;

    public GOperationInfo(String name, String type) {
        this(name, name, Collections.EMPTY_LIST, type);
    }

    public GOperationInfo(String name, Class[] paramTypes, String type) {
        this.name = this.methodName = name;
        this.type = type;
        String[] args = new String[paramTypes.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = paramTypes[i].getName();
        }
        this.parameters = Collections.unmodifiableList(Arrays.asList(args));
    }

    public GOperationInfo(String name, String[] paramTypes, String type) {
        this(name, name, Arrays.asList(paramTypes), type);
    }
    
    public GOperationInfo(String name, List parameters, String type) {
        this(name, name, parameters, type);
    }
    
    public GOperationInfo(String name, String methodName, List parameters, String type) {
        this.name = name;
        this.type = type;
        this.methodName = methodName;
        this.parameters = Collections.unmodifiableList(new ArrayList(parameters));
    }

    public String getName() {
        return name;
    }
    
    public String getReturnType() {
        return type;
    }

    public String getMethodName() {
        return methodName;
    }

    public List getParameterList() {
        return parameters;
    }

    public String toString() {
        return "[GOperationInfo: name=" + name + " parameters=" + parameters + " type =" + type + "]";
    }
}
