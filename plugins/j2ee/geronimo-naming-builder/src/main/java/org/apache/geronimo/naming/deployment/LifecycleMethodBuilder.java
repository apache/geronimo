/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.naming.deployment;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.annotation.LifecycleMethod;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedApp;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.xbeans.javaee6.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee6.JavaIdentifierType;
import org.apache.geronimo.xbeans.javaee6.LifecycleCallbackType;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class LifecycleMethodBuilder extends AbstractNamingBuilder {
    public void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map componentContext) throws DeploymentException {
        // skip ejb modules... they have alreayd been processed
        if (module.getType() == ConfigurationModuleType.EJB) {
            return;
        }

        ClassFinder classFinder = module.getClassFinder();
        AnnotatedApp annotatedApp = module.getAnnotatedApp();
        if (annotatedApp == null) {
            throw new NullPointerException("No AnnotatedApp supplied");
        }
        Map<String, LifecycleCallbackType> postConstructMap = mapLifecycleCallbacks(annotatedApp.getPostConstructArray(), annotatedApp.getComponentType());
        Map<String, LifecycleCallbackType> preDestroyMap = mapLifecycleCallbacks(annotatedApp.getPreDestroyArray(), annotatedApp.getComponentType());
        if (module.getClassFinder() != null) {
            List<Method> postConstructs = classFinder.findAnnotatedMethods(PostConstruct.class);
            for (Method m : postConstructs) {
                String methodName = m.getName();
                String className = m.getDeclaringClass().getName();
                if (!postConstructMap.containsKey(className)) {
                    LifecycleCallbackType callback = annotatedApp.addPostConstruct();
                    FullyQualifiedClassType classType = callback.addNewLifecycleCallbackClass();
                    classType.setStringValue(className);
                    JavaIdentifierType method = callback.addNewLifecycleCallbackMethod();
                    method.setStringValue(methodName);
                    postConstructMap.put(className, callback);
                }
            }
            List<Method> preDestroys = classFinder.findAnnotatedMethods(PreDestroy.class);
            for (Method m : preDestroys) {
                String methodName = m.getName();
                String className = m.getDeclaringClass().getName();
                if (!preDestroyMap.containsKey(className)) {
                    LifecycleCallbackType callback = annotatedApp.addPreDestroy();
                    FullyQualifiedClassType classType = callback.addNewLifecycleCallbackClass();
                    classType.setStringValue(className);
                    JavaIdentifierType method = callback.addNewLifecycleCallbackMethod();
                    method.setStringValue(methodName);
                    preDestroyMap.put(className, callback);
                }
            }
        }
        Map<String, LifecycleMethod> postConstructs = map(postConstructMap);
        Map<String, LifecycleMethod> preDestroys = map(preDestroyMap);
        Holder holder = NamingBuilder.INJECTION_KEY.get(componentContext);
        holder.addPostConstructs(postConstructs);
        holder.addPreDestroys(preDestroys);
    }

    private Map<String, LifecycleMethod> map(Map<String, LifecycleCallbackType> lifecycleCallbackTypes) {
        if (lifecycleCallbackTypes.isEmpty()) {
            return null;
        }
        Map<String, LifecycleMethod> map = new HashMap<String, LifecycleMethod>();
        for (Map.Entry<String, LifecycleCallbackType> entry : lifecycleCallbackTypes.entrySet()) {
            String className = entry.getKey();
            LifecycleCallbackType callback = entry.getValue();
            LifecycleMethod method = new LifecycleMethod(className, callback.getLifecycleCallbackMethod().getStringValue().trim());
            map.put(className, method);
        }
        return map;
    }

    private Map<String, LifecycleCallbackType> mapLifecycleCallbacks(LifecycleCallbackType[] callbackArray, String componentType) throws DeploymentException {
        Map<String, LifecycleCallbackType> map = new HashMap<String, LifecycleCallbackType>();
        for (LifecycleCallbackType callback : callbackArray) {
            String className;
            if (callback.isSetLifecycleCallbackClass()) {
                className = callback.getLifecycleCallbackClass().getStringValue().trim();
            } else {
                if (componentType == null) {
                    throw new DeploymentException("No component type available and none in  lifecycle callback");
                }
                className = componentType;
            }
            map.put(className, callback);
        }
        return map;
    }

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return QNameSet.EMPTY;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(LifecycleMethodBuilder.class, NameFactory.MODULE_BUILDER);

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
