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


package org.apache.geronimo.openwebbeans;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.webbeans.component.ResourceBean;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.api.ResourceReference;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.SecurityUtil;

/**
 * @version $Rev:$ $Date:$
 */
public class GeronimoResourceInjectionService implements ResourceInjectionService {

    private final Context context;

    public GeronimoResourceInjectionService() {
        try {
            this.context = new InitialContext();
        } catch (NamingException e) {
            throw new WebBeansException("could not set up naming context", e);
        }
    }

    //from StandaloneResourceInjectionService
    @Override
    public void injectJavaEEResources(Object managedBeanInstance) throws Exception {
        Class<?> currentClass = managedBeanInstance.getClass();

        while (currentClass != null && Object.class != currentClass)
        {
            Field[] fields = SecurityUtil.doPrivilegedGetDeclaredFields(currentClass);

            for(Field field : fields)
            {
                if(!field.isAnnotationPresent(Produces.class))
                {
                    if(!Modifier.isStatic(field.getModifiers()))
                    {
                        Annotation ann = AnnotationUtil.hasOwbInjectableResource(field.getDeclaredAnnotations());
                        if(ann != null)
                        {
                            @SuppressWarnings("unchecked")
                            ResourceReference<Object, ?> resourceRef = new ResourceReference(field.getDeclaringClass(), field.getName(), field.getType(), ann);
                            boolean acess = field.isAccessible();
                            try
                            {
                                SecurityUtil.doPrivilegedSetAccessible(field, true);
                                field.set(managedBeanInstance, getResourceReference(resourceRef));

                            }
                            catch(Exception e)
                            {
                                throw new WebBeansException("Unable to inject field" + field, e);

                            }
                            finally
                            {
                                SecurityUtil.doPrivilegedSetAccessible(field, acess);
                            }
                        }
                    }
                }
            }

            currentClass = currentClass.getSuperclass();
        }
    }

    @Override
    public <X, T extends Annotation> X getResourceReference(ResourceReference<X, T> resourceReference) {
        String jndiName = resourceReference.getJndiName();
        Class<X> type = resourceReference.getResourceType();

        try {
            return type.cast(context.lookup(jndiName));
        } catch (NamingException e) {
            throw new WebBeansException("Could not get resource of type " + type + " at jndi name " + jndiName, e);
        }
    }

    @Override
    public void clear() {
    }

    /**
     * delegation of serialization behavior
     */
    public <T> void writeExternal(Bean<T> bean, T actualResource, ObjectOutput out) throws IOException {
        //do nothing
    }

    /**
     * delegation of serialization behavior
     */
    public <T> T readExternal(Bean<T> bean, ObjectInput out) throws IOException,
            ClassNotFoundException {
        return (T) ((ResourceBean)bean).getActualInstance();
    }
        

}
