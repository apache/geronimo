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
package org.apache.geronimo.kernel.basic;

import javax.management.ObjectName;

import org.apache.geronimo.gbean.runtime.RawInvoker;

/**
 * @version $Rev$ $Date$
 */
public final class RawOperationInvoker implements ProxyInvoker {
    private final RawInvoker rawInvoker;
    private final int methodIndex;

    public RawOperationInvoker(RawInvoker rawInvoker, int methodIndex) {
        this.rawInvoker = rawInvoker;
        this.methodIndex = methodIndex;
    }

    public Object invoke(final ObjectName objectName, final Object[] arguments) throws Throwable {
        return rawInvoker.invoke(methodIndex, arguments);
    }
}
