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
package org.apache.geronimo.gbean.runtime;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;
import org.apache.geronimo.kernel.management.State;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractGBeanReference implements GBeanReference {
    /**
     * Name of this reference.
     */
    private final String name;

    /**
     * Interface this GBeanInstance uses to refer to the other.
     */
    private final Class referenceType;

    /**
     * Proxy type which is injected into the GBeanInstance.
     */
    private final Class proxyType;

    /**
     * The GBeanInstance to which this reference belongs.
     */
    private final GBeanInstance gbeanInstance;

    /**
     * The method that will be called to set the attribute value.  If null, the value will be set with
     * a constructor argument
     */
    private final MethodInvoker setInvoker;

    /**
     * The target objectName patterns to watch for a connection.
     */
    private Set patterns = Collections.EMPTY_SET;

    /**
     * Our listener for lifecycle events
     */
    private final LifecycleListener listener;

    /**
     * Current set of targets
     */
    private final Set targets = new HashSet();

    /**
     * The metadata for this reference
     */
    private final GReferenceInfo referenceInfo;

    /**
     * The kernel to which the reference is bound.
     */
    private final Kernel kernel;

    /**
     * The dependency manager of the kernel.
     */
    private final DependencyManager dependencyManager;

    /**
     * Proxy for this reference
     */
    private Object proxy;

    /**
     * is this reference online
     */
    private boolean isOnline = false;

    public AbstractGBeanReference(GBeanInstance gbeanInstance, GReferenceInfo referenceInfo, Kernel kernel, DependencyManager dependencyManager) throws InvalidConfigurationException {
        this.gbeanInstance = gbeanInstance;
        this.referenceInfo = referenceInfo;
        this.kernel = kernel;
        this.dependencyManager = dependencyManager;

        this.name = referenceInfo.getName();
        try {
            this.referenceType = ClassLoading.loadClass(referenceInfo.getReferenceType(), gbeanInstance.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load reference proxy interface class:" + getDescription());
        }
        if (Modifier.isFinal(referenceType.getModifiers())) {
            throw new IllegalArgumentException("Proxy interface cannot be a final class: " + referenceType.getName());
        }
        try {
            this.proxyType = ClassLoading.loadClass(referenceInfo.getProxyType(), gbeanInstance.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load proxy class:" + getDescription());
        }

        if (referenceInfo.getSetterName() != null) {
            try {
                String setterName = referenceInfo.getSetterName();
                Method setterMethod = gbeanInstance.getType().getMethod(setterName, new Class[] {proxyType});
                setInvoker = new FastMethodInvoker(setterMethod);
            } catch (NoSuchMethodException e) {
                throw new InvalidConfigurationException("Setter method not found " + getDescription());
            }
        } else {
            setInvoker = null;
        }

        listener = createLifecycleListener();
    }

    protected abstract LifecycleListener createLifecycleListener();

    protected abstract void targetAdded(ObjectName target);

    protected abstract void targetRemoved(ObjectName target);

    protected final Kernel getKernel() {
        return kernel;
    }

    protected final DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    public final GBeanInstance getGBeanInstance() {
        return gbeanInstance;
    }

    public final String getName() {
        return name;
    }

    public final GReferenceInfo getReferenceInfo() {
        return referenceInfo;
    }

    public final Class getReferenceType() {
        return referenceType;
    }

    public final Class getProxyType() {
        return proxyType;
    }

    public final Object getProxy() {
        return proxy;
    }

    protected final void setProxy(Object proxy) {
        this.proxy = proxy;
    }

    public final Set getPatterns() {
        return patterns;
    }

    public final void setPatterns(Set patterns) {
        if (isOnline) {
            throw new IllegalStateException("Pattern set can not be modified while online");
        }

        if (patterns == null || patterns.isEmpty() || (patterns.size() == 1 && patterns.iterator().next() == null)) {
            this.patterns = Collections.EMPTY_SET;
        } else {
            patterns = new HashSet(patterns);
            for (Iterator iterator = this.patterns.iterator(); iterator.hasNext();) {
                if (iterator.next() == null) {
                    iterator.remove();
                    //there can be at most one null value in a set.
                    break;
                }
            }
            this.patterns = Collections.unmodifiableSet(patterns);
        }
    }

    public final synchronized void online() {
        Set gbeans = kernel.listGBeans(patterns);
        for (Iterator objectNameIterator = gbeans.iterator(); objectNameIterator.hasNext();) {
            ObjectName target = (ObjectName) objectNameIterator.next();
            if (!targets.contains(target)) {

                // if the bean is running add it to the runningTargets list
                if (isRunning(kernel, target)) {
                    targets.add(target);
                }
            }
        }

        kernel.getLifecycleMonitor().addLifecycleListener(listener, patterns);
        isOnline = true;
    }

    public final synchronized void offline() {
        // make sure we are stoped
        stop();

        kernel.getLifecycleMonitor().removeLifecycleListener(listener);

        targets.clear();
        isOnline = false;
    }

    protected final Set getTargets() {
        return targets;
    }

    protected final void addTarget(ObjectName objectName) {
        if (!targets.contains(objectName)) {
            targets.add(objectName);
            targetAdded(objectName);
        }
    }

    protected final void removeTarget(ObjectName objectName) {
        boolean wasTarget = targets.remove(objectName);
        if (wasTarget) {
            targetRemoved(objectName);
        }
    }

    public final synchronized void inject(Object target) throws Exception {
        // set the proxy into the instance
        if (setInvoker != null && patterns.size() > 0) {
            setInvoker.invoke(target, new Object[]{getProxy()});
        }
    }

    /**
     * Is the component in the Running state
     *
     * @param objectName name of the component to check
     * @return true if the component is running; false otherwise
     */
    private boolean isRunning(Kernel kernel, ObjectName objectName) {
        try {
            final int state = kernel.getGBeanState(objectName);
            return state == State.RUNNING_INDEX;
        } catch (GBeanNotFoundException e) {
            // mbean is no longer registerd
            return false;
        } catch (Exception e) {
            // problem getting the attribute, mbean has most likely failed
            return false;
        }
    }

    protected final String getDescription() {
        return "Reference Name: " + getName() +
                ", Reference Type: " + getReferenceType() +
                ", Proxy Type: " + getProxy() +
                ", GBeanInstance: " + gbeanInstance.getName();
    }
}
