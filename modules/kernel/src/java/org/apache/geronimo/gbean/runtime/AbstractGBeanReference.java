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
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.LifecycleAdapter;
import org.apache.geronimo.kernel.LifecycleListener;
import org.apache.geronimo.kernel.NoSuchAttributeException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
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
    private final Class type;

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
     * The kernel to which the reference is bound.
     */
    private Kernel kernel;

    /**
     * Proxy for this reference
     */
    private Object proxy;

    public AbstractGBeanReference(GBeanInstance gbeanInstance, GReferenceInfo referenceInfo, Class constructorType) throws InvalidConfigurationException {
        this.gbeanInstance = gbeanInstance;
        this.name = referenceInfo.getName();
        try {
            this.type = ClassLoading.loadClass(referenceInfo.getType(), gbeanInstance.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load reference proxy interface class:" +
                    " name=" + name +
                    " class=" + referenceInfo.getType());
        }
        if (Modifier.isFinal(type.getModifiers())) {
            throw new IllegalArgumentException("Proxy interface cannot be a final class: " + type.getName());
        }

        if (constructorType != null) {
            setInvoker = null;
        } else {
            Method setterMethod = searchForSetter(gbeanInstance, referenceInfo);
            setInvoker = new FastMethodInvoker(setterMethod);
        }

        listener = new LifecycleAdapter() {
            public void running(ObjectName objectName) {
                if (!targets.contains(objectName)) {
                    targets.add(objectName);
                    targetAdded(objectName);
                }
            }

            public void stoping(ObjectName objectName) {
                removeTarget(objectName);
            }

            public void stopped(ObjectName objectName) {
                removeTarget(objectName);
            }

            public void failed(ObjectName objectName) {
                removeTarget(objectName);
            }

            public void unloaded(ObjectName objectName) {
                removeTarget(objectName);
            }

            private void removeTarget(ObjectName objectName) {
                boolean wasTarget = targets.remove(objectName);
                if (wasTarget) {
                    targetRemoved(objectName);
                }
            }
        };
    }

    protected final Kernel getKernel() {
        return kernel;
    }

    public final GBeanInstance getGBeanInstance() {
        return gbeanInstance;
    }

    public final String getName() {
        return name;
    }

    public final Class getType() {
        return type;
    }

    public Object getProxy() {
        return proxy;
    }

    protected void setProxy(Object proxy) {
        this.proxy = proxy;
    }

    public final Set getPatterns() {
        return patterns;
    }

    public final void setPatterns(Set patterns) {
        if (kernel != null) {
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

    public final synchronized void online(Kernel kernel) {
        this.kernel = kernel;

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
    }

    public final synchronized void offline() {
        // make sure we are stoped
        stop();

        kernel.getLifecycleMonitor().removeLifecycleListener(listener);

        targets.clear();
        kernel = null;
    }

    protected abstract void targetAdded(ObjectName target);

    protected abstract void targetRemoved(ObjectName target);

    protected final Set getTargets() {
        return targets;
    }

    public final synchronized void inject() throws Exception {
        // set the proxy into the instance
        if (setInvoker != null && patterns.size() > 0) {
            setInvoker.invoke(gbeanInstance.getTarget(), new Object[]{getProxy()});
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
            final int state = ((Integer) kernel.getAttribute(objectName, "state")).intValue();
            return state == State.RUNNING_INDEX;
        } catch (NoSuchAttributeException e) {
            // ok -- mbean is not a startable
            return true;
        } catch (GBeanNotFoundException e) {
            // mbean is no longer registerd
            return false;
        } catch (Exception e) {
            // problem getting the attribute, mbean has most likely failed
            return false;
        }
    }

    protected static Method searchForSetter(GBeanInstance gbeanInstance, GReferenceInfo referenceInfo) throws InvalidConfigurationException {
        if (referenceInfo.getSetterName() == null) {
            // no explicit name give so we must search for a name
            String setterName = "set" + referenceInfo.getName();
            Method[] methods = gbeanInstance.getType().getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getParameterTypes().length == 1 &&
                        method.getReturnType() == Void.TYPE &&
                        setterName.equalsIgnoreCase(method.getName())) {

                    return method;
                }
            }
        } else {
            // even though we have an exact name we need to search the methods because
            // we don't know the parameter type
            Method[] methods = gbeanInstance.getType().getMethods();
            String setterName = referenceInfo.getSetterName();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getParameterTypes().length == 1 &&
                        method.getReturnType() == Void.TYPE &&
                        setterName.equals(method.getName())) {

                    return method;
                }
            }
        }
        throw new InvalidConfigurationException("Target does not have specified method:" +
                " name=" + referenceInfo.getName() +
                " targetClass=" + gbeanInstance.getType().getName());
    }
}
