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

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractGBeanReference implements GBeanReference {
    /**
     * Should we proxy references.
     */
    protected static final boolean NO_PROXY;
    static {
        Logger log = LoggerFactory.getLogger(AbstractGBeanReference.class);
        String no_proxy = System.getProperty("Xorg.apache.geronimo.gbean.NoProxy", "true");
        NO_PROXY = no_proxy.equalsIgnoreCase("true");
        if (NO_PROXY) {
            log.info("GBean references are not using proxies");
        } else {
            log.info("GBean references are using proxies");
        }
    }

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

    private final boolean hasTargets;

    /**
     * The metadata for this reference
     */
    private final GReferenceInfo referenceInfo;

    /**
     * The kernel to which the reference is bound.
     */
    private final Kernel kernel;

    /**
     * Proxy for this reference
     */
    private Object proxy;


    public AbstractGBeanReference(GBeanInstance gbeanInstance, GReferenceInfo referenceInfo, Kernel kernel, boolean hasTargets) throws InvalidConfigurationException {
        this.gbeanInstance = gbeanInstance;
        this.referenceInfo = referenceInfo;
        this.kernel = kernel;
        this.hasTargets = hasTargets;

        this.name = referenceInfo.getName();
        try {
            this.referenceType = ClassLoading.loadClass(referenceInfo.getReferenceType(), gbeanInstance.getType().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load Reference Type: " + getDescription(), e);
        }
        if (Modifier.isFinal(referenceType.getModifiers())) {
            throw new IllegalArgumentException("Proxy interface cannot be a final class: " + referenceType.getName());
        }
        try {
            this.proxyType = ClassLoading.loadClass(referenceInfo.getProxyType(), gbeanInstance.getType().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load Proxy Type:" + getDescription(), e);
        }

        if (referenceInfo.getSetterName() != null) {
            try {
                String setterName = referenceInfo.getSetterName();
                Method setterMethod = gbeanInstance.getType().getMethod(setterName, new Class[] {proxyType});
                if (NO_PROXY) {
                    setInvoker = new ReflectionMethodInvoker(setterMethod);
                } else {
                    setInvoker = new FastMethodInvoker(setterMethod);
                }
            } catch (NoSuchMethodException e) {
                throw new InvalidConfigurationException("Setter method not found " + getDescription(), e);
            }
        } else {
            setInvoker = null;
        }

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

    /**
     * Is the component in the Running state
     *
     * @param abstractName name of the component to check
     * @return true if the component is running; false otherwise
     */
    protected boolean isRunning(Kernel kernel, AbstractName abstractName) {
        try {
            final int state = kernel.getGBeanState(abstractName);
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
        return "\n    GBeanInstance: " + gbeanInstance.getName() +
                "\n    Reference Name: " + getName() +
                "\n    Reference Type: " + referenceInfo.getReferenceType() +
                "\n    Proxy Type: " + referenceInfo.getProxyType();
    }

}
