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
package org.apache.geronimo.jaxws.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.ejb.EJB;

public abstract class EJBAnnotationHandler extends InjectingAnnotationHandler {

    public Class<? extends Annotation> getAnnotationType() {
        return EJB.class;
    }

    public void processFieldAnnotation(Object instance,
                                       Field field,
                                       Annotation annotation)
            throws InjectionException {
        EJB resource = (EJB) annotation;
        injectField(instance, field, annotation, resource.name(), null);
    }

    public void processMethodAnnotation(Object instance,
                                        Method method,
                                        Annotation annotation)
            throws InjectionException {
        EJB resource = (EJB) annotation;
        injectMethod(instance, method, annotation, resource.name(), null);
    }

}
