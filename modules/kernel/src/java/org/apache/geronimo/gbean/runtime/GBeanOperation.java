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

package org.apache.geronimo.gbean.runtime;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.DynamicGOperationInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.ClassLoading;

/**
 * @version $Rev$ $Date$
 */
public final class GBeanOperation {
    private final GBeanInstance gbeanInstance;
    private final String name;
    private final List parameterTypes;
    private final MethodInvoker methodInvoker;
    private final boolean framework;
    private final GOperationInfo operationInfo;

    static GBeanOperation createFrameworkOperation(GBeanInstance gbeanInstance, String name, List parameterTypes, MethodInvoker methodInvoker) {
        return new GBeanOperation(gbeanInstance, name, parameterTypes, methodInvoker);
    }

    private GBeanOperation(GBeanInstance gbeanInstance, String name, List parameterTypes, MethodInvoker methodInvoker) {
        framework = true;
        this.gbeanInstance = gbeanInstance;
        this.name = name;
        this.parameterTypes = Collections.unmodifiableList(new ArrayList(parameterTypes));
        this.methodInvoker = methodInvoker;
        this.operationInfo = new GOperationInfo(this.name, this.parameterTypes);
    }

    public GBeanOperation(GBeanInstance gbeanInstance, GOperationInfo operationInfo) throws InvalidConfigurationException {
        framework = false;
        this.gbeanInstance = gbeanInstance;
        this.name = operationInfo.getName();
        this.operationInfo = operationInfo;

        // get an array of the parameter classes
        this.parameterTypes = Collections.unmodifiableList(new ArrayList(operationInfo.getParameterList()));
        Class[] types = new Class[parameterTypes.size()];
        ClassLoader classLoader = gbeanInstance.getClassLoader();
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
        if (operationInfo instanceof DynamicGOperationInfo) {
            methodInvoker = new MethodInvoker() {
                private String[] types = (String[]) parameterTypes.toArray(new String[parameterTypes.size()]);

                public Object invoke(Object target, Object[] arguments) throws Exception {
                    DynamicGBean dynamicGBean = (DynamicGBean) target;
                    dynamicGBean.invoke(name, arguments, types);
                    return null;
                }
            };
        } else {
            try {
                Method javaMethod = gbeanInstance.getType().getMethod(operationInfo.getMethodName(), types);
                methodInvoker = new FastMethodInvoker(javaMethod);
            } catch (Exception e) {
                throw new InvalidConfigurationException("Target does not have specified method (declared in a GBeanInfo operation):" +
                        " name=" + operationInfo.getName() +
                        " methodName=" + operationInfo.getMethodName() +
                        " targetClass=" + gbeanInstance.getType().getName());
            }
        }
    }

    public String getName() {
        return name;
    }

    public List getParameterTypes() {
        return parameterTypes;
    }

    public GOperationInfo getOperationInfo() {
        return operationInfo;
    }

    public boolean isFramework() {
        return framework;
    }

    public Object invoke(Object target, final Object[] arguments) throws Exception {
        return methodInvoker.invoke(target, arguments);
    }

    public String getDescription() {
        String signature = name + "(";
        for (Iterator iterator = parameterTypes.iterator(); iterator.hasNext();) {
            String type = (String) iterator.next();
            signature += type;
            if (iterator.hasNext()) {
                signature += ", ";
            }
        }
        signature += ")";
        return "Operation Signature: " + signature + ", GBeanInstance: " + gbeanInstance.getName();
    }
}
