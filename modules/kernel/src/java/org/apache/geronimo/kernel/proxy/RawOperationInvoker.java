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
package org.apache.geronimo.kernel.proxy;

import javax.management.ObjectName;

import org.apache.geronimo.gbean.jmx.RawInvoker;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
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
