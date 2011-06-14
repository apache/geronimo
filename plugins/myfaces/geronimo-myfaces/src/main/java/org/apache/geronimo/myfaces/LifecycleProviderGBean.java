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

package org.apache.geronimo.myfaces;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.annotation.LifecycleMethod;
import org.apache.geronimo.j2ee.jndi.ContextSource;
import org.apache.geronimo.kernel.Kernel;
import org.apache.myfaces.config.annotation.LifecycleProvider;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class LifecycleProviderGBean implements LifecycleProvider {

    private final Holder holder;
    private final Context context;
    private final ClassLoader classLoader;
    private final Bundle bundle;

    public LifecycleProviderGBean(@ParamAttribute(name="holder") Holder holder,
                                  @ParamReference(name="ContextSource", namingType = "Context") ContextSource contextSource,
                                  @ParamSpecial(type = SpecialAttributeType.kernel)Kernel kernel,
                                  @ParamSpecial(type = SpecialAttributeType.bundle)Bundle bundle,
                                  @ParamSpecial(type=SpecialAttributeType.classLoader)ClassLoader classLoader) throws NamingException {
        this.holder = holder;
        context = contextSource.getContext();
        this.bundle = bundle;
        this.classLoader = classLoader;
    }

    public Object newInstance(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NamingException, InvocationTargetException {
        if (className == null) {
            throw new InstantiationException("no class name provided");
        }
        return holder.newInstance(className, classLoader, context);
    }

    public void destroyInstance(Object o) throws IllegalAccessException, InvocationTargetException {
        Class<?> clazz = o.getClass();
        if (holder != null) {
            Map<String, LifecycleMethod> preDestroy = holder.getPreDestroy();
            if (preDestroy != null) {
                Holder.apply(o, clazz, preDestroy);
            }
        }
    }
}
