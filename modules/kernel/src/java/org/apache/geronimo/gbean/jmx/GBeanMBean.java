/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;

/**
 * A GBeanMBean is a J2EE Management Managed Object, and is standard base for Geronimo services.
 * This wraps one or more target POJOs and exposes the attributes and operations according to a supplied
 * {@link GBeanInfo} instance.  The GBeanMBean also supports caching of attribute values and invocation results
 * which can reduce the number of calls to a target.
 *
 * @version $Rev$ $Date$
 */
public final class GBeanMBean {
    private static ClassLoader getContextClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = GBeanMBean.class.getClassLoader();
        }
        return classLoader;
    }

    /**
     * The data of the
     */
    private final GBeanData gbeanData;

    /**
     * The classloader used for all invocations and creating targets.
     */
    private final ClassLoader classLoader;

    /**
     * @deprecated use GBeanData instead
     */
    public GBeanMBean(GBeanData gbeanData, ClassLoader classLoader) throws InvalidConfigurationException {
        this.gbeanData = gbeanData;
        this.classLoader = classLoader;
    }

    /**
     * @deprecated use GBeanData instead
     */
    public GBeanMBean(GBeanInfo gbeanInfo, ClassLoader classLoader) throws InvalidConfigurationException {
        this.gbeanData = new GBeanData(gbeanInfo);
        this.classLoader = classLoader;
    }

    /**
     * @deprecated use kernel.loadGBean(GBeanData gbeanData, ClassLoader classLoader)
     */
    public GBeanMBean(GBeanInfo gbeanInfo) throws InvalidConfigurationException {
        this.gbeanData = new GBeanData(gbeanInfo);
        this.classLoader = getContextClassLoader();
    }

    /**
     * @deprecated use kernel.loadGBean(GBeanData gbeanData, ClassLoader classLoader)
     */
    public GBeanMBean(String className, ClassLoader classLoader) throws Exception {
        this.gbeanData = new GBeanData(GBeanInfo.getGBeanInfo(className, classLoader));
        this.classLoader = classLoader;
    }

    /**
     * @deprecated use GBeanData instead
     */
    public GBeanMBean(String className) throws Exception {
        this.classLoader = ClassLoader.getSystemClassLoader();
        this.gbeanData = new GBeanData(GBeanInfo.getGBeanInfo(className, classLoader));
    }

    /**
     * @deprecated use GBeanData instead
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * @deprecated use GBeanData instead
     */
    public GBeanData getGBeanData() {
        return gbeanData;
    }

    /**
     * @deprecated use GBeanData instead
     */
    public Object getAttribute(String name) throws ReflectionException, AttributeNotFoundException {
        return gbeanData.getAttribute(name);
    }

    /**
     * @deprecated use GBeanData instead
     */
    public void setAttribute(String name, Object value) throws ReflectionException, AttributeNotFoundException {
        gbeanData.setAttribute(name, value);
    }

    /**
     * @deprecated use GBeanData instead
     */
    public void setAttribute(Attribute attribute) throws ReflectionException, AttributeNotFoundException {
        String name = attribute.getName();
        Object value = attribute.getValue();
        gbeanData.setAttribute(name, value);
    }

    /**
     * @deprecated use GBeanData instead
     */
    public Set getReferencePatterns(String name) {
        return gbeanData.getReferencePatterns(name);
    }

    /**
     * @deprecated use GBeanData instead
     */
    public void setReferencePattern(String name, ObjectName pattern) {
        gbeanData.setReferencePattern(name, pattern);
    }

    /**
     * @deprecated use GBeanData instead
     */
    public void setReferencePatterns(String name, Set patterns) {
        gbeanData.setReferencePatterns(name, patterns);
    }
}
