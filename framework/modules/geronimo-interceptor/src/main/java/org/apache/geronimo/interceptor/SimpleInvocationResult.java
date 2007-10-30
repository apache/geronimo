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

package org.apache.geronimo.interceptor;

import java.io.Serializable;

import org.apache.geronimo.interceptor.InvocationResult;

/**
 *
 *
 *
 * @version $Rev$ $Date$
 */
public class SimpleInvocationResult implements InvocationResult, Serializable {
    private final Object result;
    private final boolean normal;

    /**
     * Create a object representing the normal result of an Invocation
     * @param normal true if the target returned; false if it threw an application Exception
     * @param result the result or Exception
     */
    public SimpleInvocationResult(boolean normal, Object result) {
        assert (normal || result instanceof Throwable) : "Result must be normal or a Throwable";
        this.normal = normal;
        this.result = result;
    }

    public boolean isNormal() {
        return normal;
    }

    public boolean isException() {
        return !normal;
    }

    public Object getResult() {
        assert (normal == true);
        return result;
    }

    public Exception getException() {
        assert (normal == false);
        return (Exception) result;
    }
}
