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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.annotation.LifecycleMethod;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.Lifecycle;
import org.apache.openejb.jee.LifecycleCallback;
import org.apache.xbean.finder.AbstractFinder;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class LifecycleMethodBuilder extends AbstractNamingBuilder {

    @Override
    public void buildNaming(JndiConsumer specDD, XmlObject plan, Module module, Map<EARContext.Key, Object> sharedContext) throws DeploymentException {
        // skip ejb modules... they have already been processed
        //skip ears, they have no standalone components
//        if (module.getType() == ConfigurationModuleType.EJB || module.getType() == ConfigurationModuleType.EAR) {
//            return;
//        }
        if (!(specDD instanceof Lifecycle)) {
            return;
        }
        Lifecycle lifecycle = (Lifecycle) specDD;
        AbstractFinder classFinder = module.getClassFinder();
        //TODO Need to double check the LifecycleMethod Scanning, seems we also do it in the OpenEJB
        String componentType = null;
        if (specDD instanceof EnterpriseBean) {
            componentType = ((EnterpriseBean) specDD).getEjbClass();
        } else if (specDD instanceof Interceptor) {
            componentType = ((Interceptor) specDD).getInterceptorClass();
        } else if (specDD instanceof ApplicationClient) {
            componentType = ((ApplicationClient) specDD).getMainClass();
        }
        Map<String, LifecycleCallback> postConstructMap = mapLifecycleCallbacks(lifecycle.getPostConstruct(), componentType);
        Map<String, LifecycleCallback> preDestroyMap = mapLifecycleCallbacks(lifecycle.getPreDestroy(), componentType);
        if (module.getClassFinder() != null) {
            List<Method> postConstructs = classFinder.findAnnotatedMethods(PostConstruct.class);
            for (Method m : postConstructs) {
                String methodName = m.getName();
                String className = m.getDeclaringClass().getName();
                if (!postConstructMap.containsKey(className)) {
                    LifecycleCallback callback = new LifecycleCallback();
                    callback.setLifecycleCallbackClass(className);
                    callback.setLifecycleCallbackMethod(methodName);
                    lifecycle.getPostConstruct().add(callback);
                    postConstructMap.put(className, callback);
                }
            }
            List<Method> preDestroys = classFinder.findAnnotatedMethods(PreDestroy.class);
            for (Method m : preDestroys) {
                String methodName = m.getName();
                String className = m.getDeclaringClass().getName();
                if (!preDestroyMap.containsKey(className)) {
                    LifecycleCallback callback = new LifecycleCallback();
                    callback.setLifecycleCallbackClass(className);
                    callback.setLifecycleCallbackMethod(methodName);
                    preDestroyMap.put(className, callback);
                    lifecycle.getPreDestroy().add(callback);
                }
            }
        }
        Map<String, LifecycleMethod> postConstructs = map(postConstructMap);
        Map<String, LifecycleMethod> preDestroys = map(preDestroyMap);
        Holder holder = NamingBuilder.INJECTION_KEY.get(sharedContext);
        holder.addPostConstructs(postConstructs);
        holder.addPreDestroys(preDestroys);
    }

    private Map<String, LifecycleMethod> map(Map<String, LifecycleCallback> LifecycleCallbacks) {
        if (LifecycleCallbacks.isEmpty()) {
            return null;
        }
        Map<String, LifecycleMethod> map = new HashMap<String, LifecycleMethod>();
        for (Map.Entry<String, LifecycleCallback> entry : LifecycleCallbacks.entrySet()) {
            String className = entry.getKey();
            LifecycleCallback callback = entry.getValue();
            LifecycleMethod method = new LifecycleMethod(className, callback.getLifecycleCallbackMethod().trim());
            map.put(className, method);
        }
        return map;
    }

    private Map<String, LifecycleCallback> mapLifecycleCallbacks(Collection<LifecycleCallback> callbackArray, String componentType) throws DeploymentException {
        Map<String, LifecycleCallback> map = new HashMap<String, LifecycleCallback>();
        for (LifecycleCallback callback : callbackArray) {
            String className;
            if (callback.getLifecycleCallbackClass() != null) {
                className = callback.getLifecycleCallbackClass().trim();
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

    @Override
    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    @Override
    public QNameSet getPlanQNameSet() {
        return QNameSet.EMPTY;
    }

}
