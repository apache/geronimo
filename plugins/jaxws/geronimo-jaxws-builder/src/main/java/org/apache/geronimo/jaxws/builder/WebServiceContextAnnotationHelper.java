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
package org.apache.geronimo.jaxws.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.annotation.Injection;
import org.apache.xbean.finder.ClassFinder;

public class WebServiceContextAnnotationHelper {
      
    public static final String RELATIVE_JNDI_NAME = "env/WebServiceContext";
    public static final String ABSOLUTE_JNDI_NAME = "java:comp/" + RELATIVE_JNDI_NAME;
    
    public static void addWebServiceContextInjections(Holder holder, Class clazz) {
        List<Class> classes = new ArrayList<Class>();
        while (clazz != Object.class) {
            classes.add(clazz);
            clazz = clazz.getSuperclass();
        }
        addWebServiceContextInjections(holder, new ClassFinder(classes));
    }
    
    public static void addWebServiceContextInjections(Holder holder, ClassFinder finder) {        
        List<Field> fields = finder.findAnnotatedFields(Resource.class);
        for (Field field : fields) {
            Resource resource = (Resource) field.getAnnotation(Resource.class);
            Class type = getInjectionType(resource.type(), null, field);
            if (WebServiceContext.class == type) {
                holder.addInjection(field.getDeclaringClass().getName(), 
                                    new Injection(field.getDeclaringClass().getName(), getInjectionName(null, field), ABSOLUTE_JNDI_NAME));
            }
        }
        List<Method> methods = finder.findAnnotatedMethods(Resource.class);
        for (Method method : methods) {
            Resource resource = (Resource) method.getAnnotation(Resource.class);
            Class type = getInjectionType(resource.type(), method, null);
            if (WebServiceContext.class == type) {
                holder.addInjection(method.getDeclaringClass().getName(), 
                                    new Injection(method.getDeclaringClass().getName(), getInjectionName(method, null), ABSOLUTE_JNDI_NAME));
            }            
        }
    }
    
    private static Class<?> getInjectionType(Class<?> type, Method method, Field field) {
        if (type == null || Object.class == type) {
            if (field != null) {
                return field.getType();
            } else if (method != null) {
                return method.getParameterTypes()[0];
            } else {
                throw new IllegalArgumentException("You must supply exactly one of Method, Field");
            }
        } else {
            return type;
        }
    }
    
    private static String getInjectionName(Method method, Field field) {
        if (method != null) {
            String injectionJavaType = method.getName().substring(3);
            StringBuilder stringBuilder = new StringBuilder(injectionJavaType);
            stringBuilder.setCharAt(0, Character.toLowerCase(stringBuilder.charAt(0)));
            return stringBuilder.toString();
        } else if (field != null) {
            return field.getName();
        } else {
            throw new IllegalArgumentException("You must supply exactly one of Method, Field");
        }
    }

}
