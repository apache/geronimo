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

package org.apache.geronimo.gbean.annotation;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GReferenceInfo;




/**
 *
 * @version $Rev:$ $Date:$
 */
public class AnnotationGBeanInfoBuilder {
    private static final String DEFAULT_J2EE_TYPE = "GBean";
    
    private final Class<?> gbeanClass;
    
    public AnnotationGBeanInfoBuilder(Class<?> gbeanClass) {
        if (null == gbeanClass) {
            throw new IllegalArgumentException("gbeanClass is required");
        }
        this.gbeanClass = gbeanClass;
    }

    public GBeanInfo buildGBeanInfo() throws GBeanAnnotationException {
        try {
            String name = getName();
            String j2eeType = getJ2eeyType();
            GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(name, gbeanClass, j2eeType);

            setPriority(infoBuilder);
            setOsgiService(infoBuilder);
            setConstructor(infoBuilder);
            markPersistent(infoBuilder);
            addReferences(infoBuilder);

            return infoBuilder.getBeanInfo();
        } catch (NoClassDefFoundError e) {
            throw new GBeanAnnotationException("Could not fully load class: " + gbeanClass.getName() + "\n due to: " + e.getMessage() +  "\n in classloader \n" + gbeanClass.getClassLoader(), e);
        }
    }

    private void setOsgiService(GBeanInfoBuilder infoBuilder) {
        OsgiService osgiService = gbeanClass.getAnnotation(OsgiService.class);
        infoBuilder.setOsgiService(osgiService != null);
        if (osgiService != null) {
            infoBuilder.getServiceInterfaces().addAll(Arrays.asList(osgiService.serviceInterfaces()));
        }
    }

    protected void setPriority(GBeanInfoBuilder infoBuilder) {
        Priority priority = gbeanClass.getAnnotation(Priority.class);
        if (null == priority) {
            return;
        }

        infoBuilder.setPriority(priority.priority());
    }

    protected void setConstructor(GBeanInfoBuilder infoBuilder) {
        Constructor[] constructors = gbeanClass.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            Annotation[][] paramsAnnotations = constructor.getParameterAnnotations();
            if (0 < paramsAnnotations.length) {
                Annotation[] paramAnnotations = paramsAnnotations[0];
                for (Annotation paramAnnotation : paramAnnotations) {
                    if (paramAnnotation instanceof ParamAttribute 
                            || paramAnnotation instanceof ParamReference
                            || paramAnnotation instanceof ParamSpecial) {
                        setConstructor(infoBuilder, constructor);
                        return;
                    }
                }
            }
        }
        
        try {
            gbeanClass.getConstructor();
        } catch (Exception e) {
            throw new GBeanAnnotationException("Missing default constructor");
        }
        infoBuilder.setConstructor(new String[0]);   
    }

    protected void setConstructor(GBeanInfoBuilder infoBuilder, Constructor constructor) {
        Class[] types = constructor.getParameterTypes();
        Type[] genericTypes = constructor.getGenericParameterTypes();
        Annotation[][] parametersAnnotations = constructor.getParameterAnnotations();
        String[] cstrNames = new String[types.length];
        int index = 0;
        for (Annotation[] paramterAnnotations : parametersAnnotations) {
            Class parameterType = types[index];
            boolean annotationFound = false;
            for (Annotation parameterAnnotation : paramterAnnotations) {
                if (parameterAnnotation instanceof ParamAttribute) {
                    ParamAttribute attribute = (ParamAttribute) parameterAnnotation;
                    String name = attribute.name();
                    boolean persistent = attribute.persistent();
                    boolean manageable = attribute.manageable();
                    if (attribute.encrypted() == EncryptionSetting.ENCRYPTED) {
                        infoBuilder.addAttribute(name, parameterType,
                                persistent, manageable, true);
                    } else if (attribute.encrypted() == EncryptionSetting.PLAINTEXT) {
                        infoBuilder.addAttribute(name, parameterType,
                                persistent, manageable, false);
                    } else {
                        infoBuilder.addAttribute(name, parameterType,
                                persistent, manageable);
                    }
                    cstrNames[index] = name;
                    annotationFound = true;
                    break;
                } else if (parameterAnnotation instanceof ParamSpecial) {
                    ParamSpecial attribute = (ParamSpecial) parameterAnnotation;
                    String name = attribute.type().name();
                    infoBuilder.addAttribute(name, parameterType, false);
                    cstrNames[index] = name;
                    annotationFound = true;
                    break;
                } else if (parameterAnnotation instanceof ParamReference) {
                    ParamReference reference = (ParamReference) parameterAnnotation;
                    String name = reference.name();
                    Class referenceType = getGenericActualType(genericTypes[index], parameterType);
                    String namingType = reference.namingType();
                    if (namingType.equals("")) {
                        namingType = null;
                    }
                    infoBuilder.addReference(name, referenceType, namingType);
                    cstrNames[index] = name;
                    annotationFound = true;
                    break;
                }
            }
            if (!annotationFound) {
                throw new GBeanAnnotationException("Missing constructor parameter annotation for constructor ["
                        + constructor + "] at index [" + index + "]");
            }
            index++;
        }
        infoBuilder.setConstructor(cstrNames);
    }

    protected Set<Method> filterSettersByAnnotation(Class<? extends Annotation> annotationClass) {
        Set<Method> filteredMethods = new HashSet<Method>();
        Method[] methods = gbeanClass.getMethods();
        for (Method method : methods) {
            if (null == method.getAnnotation(annotationClass)) {
                continue;
            }
            if (isNotSetter(method)) {
                throw new GBeanAnnotationException("[" + method + "] is not a setter and annotated with ["
                        + annotationClass + "]");
            }
            filteredMethods.add(method);
        }  
        return filteredMethods;
    }
    
    protected void markPersistent(GBeanInfoBuilder infoBuilder) {
        Set<Method> methods = filterSettersByAnnotation(Persistent.class);
        for (Method method : methods) {
            Persistent persistent = method.getAnnotation(Persistent.class);
            Class type = method.getParameterTypes()[0];
            String name = getName(method);
            name = Introspector.decapitalize(name);
            if (persistent.encrypted() == EncryptionSetting.ENCRYPTED) {
                infoBuilder.addAttribute(name, type, true, persistent.manageable(), true);
            } else if (persistent.encrypted() == EncryptionSetting.PLAINTEXT) {
                infoBuilder.addAttribute(name, type, true, persistent.manageable(), false);
            } else {
                infoBuilder.addAttribute(name, type, true, persistent.manageable());
            }
        }
    }
    
    protected void addReferences(GBeanInfoBuilder infoBuilder) {
        Set<Method> methods = filterSettersByAnnotation(Reference.class);
        for (Method method : methods) {
            Reference reference = method.getAnnotation(Reference.class);
            Class type = method.getParameterTypes()[0];
            Class referenceType = getGenericActualType(method.getGenericParameterTypes()[0], type);
            String name = getName(method);
            String namingType = reference.namingType();
            if (namingType.equals("")) {
                namingType = null;
            }
            GReferenceInfo referenceInfo = new GReferenceInfo(name,
                referenceType.getName(), 
                type.getName(), 
                method.getName(), 
                namingType);
            infoBuilder.addReference(referenceInfo);
        }
    }
    
    protected Class getGenericActualType(Type genericType, Class parameterType) {
        if (Collection.class.isAssignableFrom(parameterType)) {
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type componentType = parameterizedType.getActualTypeArguments()[0];
                if (componentType instanceof Class) {
                    return (Class) componentType;
                }
                if (componentType instanceof WildcardType) {
                    Type[] upper = ((WildcardType)componentType).getUpperBounds();
                    if (upper.length == 1 && upper[0] instanceof Class) {
                        return (Class) upper[0];
                    }
                }
                throw new GBeanAnnotationException("Generic type is not a class: " + componentType);
            } else {
                throw new GBeanAnnotationException(Collection.class + " parameter must be generified");
            }
        }
        return parameterType;
    }
    
    protected String getName() {
        GBean bean = (GBean) gbeanClass.getAnnotation(GBean.class);
        if (null == bean) {
            return gbeanClass.getSimpleName();
        }
        String name = bean.name();
        if (name.equals("")) {
            name = gbeanClass.getSimpleName();
        }
        return name;
    }

    protected String getJ2eeyType() {
        GBean bean = (GBean) gbeanClass.getAnnotation(GBean.class);
        if (null == bean) {
            return DEFAULT_J2EE_TYPE;
        }
        return bean.j2eeType();
    }

    protected String getName(Method method) {
        return method.getName().substring(3);
    }

    protected boolean isNotSetter(Method method) {
        return !(method.getName().startsWith("set") && method.getParameterTypes().length == 1);
    }

}
