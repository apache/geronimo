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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.DynamicGOperationInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.ClassLoading;

/**
 * @version $Rev$ $Date$
 */
public final class GBeanMBeanOperation {
    private final GBeanMBean gmbean;
    private final String name;
    private final List parameterTypes;
    private final MBeanOperationInfo mbeanOperationInfo;
    private final MethodInvoker methodInvoker;

    public GBeanMBeanOperation(GBeanMBean gMBean, String name, List parameterTypes, Class returnType, MethodInvoker methodInvoker) {
        this.gmbean = gMBean;
        this.name = name;
        this.parameterTypes = Collections.unmodifiableList(new ArrayList(parameterTypes));
        this.methodInvoker = methodInvoker;

        MBeanParameterInfo[] signature = new MBeanParameterInfo[parameterTypes.size()];
        for (int i = 0; i < signature.length; i++) {
            signature[i] = new MBeanParameterInfo("arg" + i, (String) parameterTypes.get(i), null);
        }

        mbeanOperationInfo = new MBeanOperationInfo(name,
                null,
                signature,
                returnType.getName(),
                MBeanOperationInfo.UNKNOWN);

    }

    public GBeanMBeanOperation(GBeanMBean gMBean, GOperationInfo operationInfo) throws InvalidConfigurationException {
        this.gmbean = gMBean;
        this.name = operationInfo.getName();

        // get an array of the parameter classes
        this.parameterTypes = Collections.unmodifiableList(new ArrayList(operationInfo.getParameterList()));
        Class[] types = new Class[parameterTypes.size()];
        ClassLoader classLoader = gMBean.getClassLoader();
        for (int i = 0; i < types.length; i++) {
            String type = (String) parameterTypes.get(i);
            try {
                types[i] = ClassLoading.loadClass((String) parameterTypes.get(i), classLoader);
            } catch (ClassNotFoundException e) {
                throw new InvalidConfigurationException("Could not load operation parameter class:" +
                        " name=" + operationInfo.getName() +
                        " class=" + type);
            }
        }

        // get a method invoker for the operation
        Class returnType;
        if (operationInfo instanceof DynamicGOperationInfo) {
            returnType = Object.class;
            methodInvoker = new MethodInvoker() {
                private String[] types = (String[]) parameterTypes.toArray(new String[parameterTypes.size()]);

                public Object invoke(Object target, Object[] arguments) throws Exception {
                    DynamicGBean dynamicGBean = (DynamicGBean) GBeanMBeanOperation.this.gmbean.getTarget();
                    dynamicGBean.invoke(name, arguments, types);
                    return null;
                }
            };
        } else {
            try {
                Method javaMethod = gMBean.getType().getMethod(operationInfo.getMethodName(), types);
                returnType = javaMethod.getReturnType();
                methodInvoker = new FastMethodInvoker(javaMethod);
            } catch (Exception e) {
                throw new InvalidConfigurationException("Target does not have specified method (declared in a GBeanInfo operation):" +
                        " name=" + operationInfo.getName() +
                        " methodName=" + operationInfo.getMethodName() +
                        " targetClass=" + gMBean.getType().getName());
            }
        }

        MBeanParameterInfo[] signature = new MBeanParameterInfo[parameterTypes.size()];
        for (int i = 0; i < signature.length; i++) {
            signature[i] = new MBeanParameterInfo("arg" + i,
                    (String) parameterTypes.get(i),
                    null);
        }

        mbeanOperationInfo = new MBeanOperationInfo(operationInfo.getName(),
                null,
                signature,
                returnType.getName(),
                MBeanOperationInfo.UNKNOWN);
    }

    public String getName() {
        return name;
    }

    public List getParameterTypes() {
        return parameterTypes;
    }

    public MBeanOperationInfo getMbeanOperationInfo() {
        return mbeanOperationInfo;
    }

    public Object invoke(final Object[] arguments) throws ReflectionException {
        if (gmbean.isOffline()) {
            throw new IllegalStateException("Operations can not be called while offline");
        }

        try {
            return methodInvoker.invoke(gmbean.getTarget(), arguments);
        } catch (Exception e) {
            throw new ReflectionException(e);
        } catch (Throwable throwable) {
            throw new ReflectionException(new InvocationTargetException(throwable));
        }
    }

}
