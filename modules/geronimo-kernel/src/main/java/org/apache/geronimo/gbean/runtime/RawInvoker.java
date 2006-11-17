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
package org.apache.geronimo.gbean.runtime;

import java.util.Map;

/**
 * The raw invoker provides a raw (fast) access invoke operations, get attribute values, and set
 * attribute values on a GBean.  This class should only be use by GBean proxy generators or carefully
 * crafted container code, because this class maintains a hard reference to a gbeanInstance which has a huge
 * potential for memory leaks.  USE WITH CAUTION
 *
 * @version $Rev$ $Date$
 */
public final class RawInvoker {
    private final GBeanInstance gbeanInstance;
    private final Map attributeIndex;
    private final Map operationIndex;

    public RawInvoker(GBeanInstance gbean) {
        this.gbeanInstance = gbean;
        attributeIndex = gbean.getAttributeIndex();
        operationIndex = gbean.getOperationIndex();
    }

    public Map getAttributeIndex() {
        return attributeIndex;
    }

    public Map getOperationIndex() {
        return operationIndex;
    }

    public Object getAttribute(final int index) throws Exception {
        return gbeanInstance.getAttribute(index);
    }

    public void setAttribute(final int index, final Object value) throws Exception {
        gbeanInstance.setAttribute(index, value);
    }

    public Object invoke(final int index, final Object[] args) throws Exception {
        return gbeanInstance.invoke(index, args);
    }
}
