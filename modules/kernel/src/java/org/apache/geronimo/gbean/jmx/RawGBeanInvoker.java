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
 * @version $Revision: 1.1 $ $Date: 2004/05/26 03:22:21 $
 */
public class RawGBeanInvoker implements GBeanInvoker {
    private final RawInvoker rawInvoker;
    private final int methodType;
    private final int methodIndex;

    public RawGBeanInvoker(RawInvoker rawInvoker, int methodIndex, int methodType) {
        this.rawInvoker = rawInvoker;
        this.methodIndex = methodIndex;
        this.methodType = methodType;
    }

    public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
        switch (methodType) {
            case GBeanInvoker.OPERATION:
                return rawInvoker.invoke(methodIndex, arguments);
            case GBeanInvoker.GETTER:
                return rawInvoker.getAttribute(methodIndex);
            case GBeanInvoker.SETTER:
                rawInvoker.setAttribute(methodIndex, arguments[0]);
                return null;
            default:
                throw new AssertionError();
        }
    }
}
