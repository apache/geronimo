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
 * @version $Revision: 1.2 $ $Date: 2004/05/27 01:05:59 $
 */
public final class RawInvoker {
    private GBeanMBean gbean;
    private Map attributeIndex;
    private Map operationIndex;

    public RawInvoker(GBeanMBean gbean) {
        this.gbean = gbean;
        attributeIndex = gbean.getAttributeIndex();
        operationIndex = gbean.getOperationIndex();
    }

    void close() {
        synchronized (this) {
            gbean = null;
        }
    }

    public Map getAttributeIndex() {
        return attributeIndex;
    }

    public Map getOperationIndex() {
        return operationIndex;
    }

    public Object getAttribute(int index) throws Exception {
        GBeanMBean gbean;
        synchronized (this) {
            gbean = this.gbean;
        }

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

    public void setAttribute(int index, Object value) throws Exception {
        GBeanMBean gbean;
        synchronized (this) {
            gbean = this.gbean;
        }

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

    public Object invoke(int index, Object[] args) throws Exception {
        GBeanMBean gbean;
        synchronized (this) {
            gbean = this.gbean;
        }

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
