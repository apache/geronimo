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
package org.apache.geronimo.gbean.jmx;

import javax.management.ObjectName;

/**
 * @version $Revision: 1.1 $ $Date: 2004/06/02 20:40:58 $
 */
public final class RawSetAttributeInvoker implements GBeanInvoker {
    private final RawInvoker rawInvoker;
    private final int methodIndex;

    public RawSetAttributeInvoker(RawInvoker rawInvoker, int methodIndex) {
        this.rawInvoker = rawInvoker;
        this.methodIndex = methodIndex;
    }

    public Object invoke(final ObjectName objectName, final Object[] arguments) throws Throwable {
        rawInvoker.setAttribute(methodIndex, arguments[0]);
        return null;
    }
}
