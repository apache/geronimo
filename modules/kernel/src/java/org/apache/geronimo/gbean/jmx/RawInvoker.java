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

import java.util.Map;
import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.ReflectionException;

/**
 * The raw invoker provides a raw (fast) access invoke operations, get attribute values, and set
 * attribute values on a GBean.  This class should only be use by GBean proxy generators or carefully
 * crafted container code, because this class maintains a hard reference to a gbean which has a huge
 * potential for memory leaks.  USE WITH CAUTION
 *
 * @version $Rev$ $Date$
 */
public final class RawInvoker {
    private final GBeanMBean gbean;
    private final Map attributeIndex;
    private final Map operationIndex;

    public RawInvoker(GBeanMBean gbean) {
        this.gbean = gbean;
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
        try {
            return gbean.getAttribute(index);
        } catch (ReflectionException e) {
            Throwable cause = e;
            while ((cause instanceof JMException || cause instanceof JMRuntimeException) && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                throw new AssertionError(cause);
            }
        }
    }

    public void setAttribute(final int index, final Object value) throws Exception {
        try {
            gbean.setAttribute(index, value);
        } catch (ReflectionException e) {
            Throwable cause = e;
            while ((cause instanceof JMException || cause instanceof JMRuntimeException) && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                throw new AssertionError(cause);
            }
        }
    }

    public Object invoke(final int index, final Object[] args) throws Exception {
        try {
            return gbean.invoke(index, args);
        } catch (ReflectionException e) {
            Throwable cause = e;
            while ((cause instanceof JMException || cause instanceof JMRuntimeException) && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                throw new AssertionError(cause);
            }
        }
    }
}
