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

import java.util.Collection;
import java.util.Set;
import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Revision: 1.2 $ $Date: 2004/05/27 01:05:59 $
 */
public final class DependencyServiceProxy implements DependencyServiceMBean {
    private final MBeanServer server;

    public DependencyServiceProxy(MBeanServer server) {
        this.server = server;
    }

    public void addDependency(ObjectName child, ObjectName parent) {
        invoke("addDependency",
                new Object[]{child, parent},
                new String[]{ObjectName.class.getName(), ObjectName.class.getName()});
    }

    public void removeDependency(ObjectName child, ObjectName parent) {
        invoke("removeDependency",
                new Object[]{child, parent},
                new String[]{ObjectName.class.getName(), ObjectName.class.getName()});
    }

    public void removeAllDependencies(ObjectName child) {
        invoke("removeAllDependencies",
                new Object[]{child},
                new String[]{ObjectName.class.getName()});
    }

    public void addDependencies(ObjectName child, Set parents) {
        invoke("addDependencies",
                new Object[]{child, parents},
                new String[]{ObjectName.class.getName(), Set.class.getName()});
    }

    public Set getParents(ObjectName child) {
        return (Set) invoke("getParents",
                new Object[]{child},
                new String[]{ObjectName.class.getName()});
    }

    public Set getChildren(ObjectName parent) {
        return (Set) invoke("getChildren",
                new Object[]{parent},
                new String[]{ObjectName.class.getName()});
    }

    public void addStartHolds(ObjectName objectName, Collection holds) {
        invoke("addStartHolds",
                new Object[]{objectName, holds},
                new String[]{ObjectName.class.getName(), Collection.class.getName()});
    }

    public void removeStartHolds(ObjectName objectName, Collection holds) {
        invoke("removeStartHolds",
                new Object[]{objectName, holds},
                new String[]{ObjectName.class.getName(), Collection.class.getName()});
    }

    public void removeAllStartHolds(ObjectName objectName) {
        invoke("removeAllStartHolds",
                new Object[]{objectName},
                new String[]{ObjectName.class.getName()});
    }

    public ObjectName checkBlocker(ObjectName objectName) {
        return (ObjectName) invoke("checkBlocker",
                new Object[]{objectName},
                new String[]{ObjectName.class.getName()});
    }

    private Object invoke(String operationName, Object[] params, String[] signature) {
        try {
            return server.invoke(Kernel.DEPENDENCY_SERVICE, operationName, params, signature);
        } catch (JMException e) {
            Throwable cause = e;
            while ((cause instanceof JMException || cause instanceof JMRuntimeException) && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new AssertionError(cause);
            }
        }
    }
}
