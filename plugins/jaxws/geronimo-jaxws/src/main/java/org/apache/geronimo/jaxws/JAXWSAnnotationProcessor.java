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
package org.apache.geronimo.jaxws;

import org.apache.geronimo.jaxws.annotations.AnnotationProcessor;
import org.apache.geronimo.jaxws.annotations.EJBAnnotationHandler;
import org.apache.geronimo.jaxws.annotations.InjectionException;
import org.apache.geronimo.jaxws.annotations.ResourceAnnotationHandler;
import org.apache.geronimo.jaxws.annotations.WebServiceRefAnnotationHandler;

import javax.naming.NamingException;
import javax.xml.ws.WebServiceContext;
import java.lang.annotation.Annotation;

public class JAXWSAnnotationProcessor extends AnnotationProcessor {

    private JNDIResolver jndiResolver;
    private WebServiceContext context;
    
    public JAXWSAnnotationProcessor(JNDIResolver jndiResolver, WebServiceContext context) {
        this.jndiResolver = jndiResolver;
        this.context = context;
        
        // register @Resource annotation handler
        registerHandler(new JAXWSResourceAnnotationHandler());
        // register @EJB annotation handler
        registerHandler(new JAXWSEJBAnnotationHandler());
        // register @WebServiceRef annotation handler
        registerHandler(new JAXWSWebServiceRefAnnotationHandler());
    }

    private Object lookupJNDI(String name, Class<?> type)
            throws InjectionException {
        try {
            return jndiResolver.resolve(name, type);
        } catch (NamingException e) {
            throw new InjectionException("JNDI injection failed for resource '" 
                    + name + "'", e);
        }
    }

    private class JAXWSResourceAnnotationHandler extends
            ResourceAnnotationHandler {
        public Object getAnnotationValue(Annotation annotation,
                                         String name,
                                         Class<?> type)
                throws InjectionException {
            if (WebServiceContext.class.isAssignableFrom(type)) {
                return type.cast(context);
            } else {
                return lookupJNDI(name, type);
            }
        }
    }

    private class JAXWSEJBAnnotationHandler extends EJBAnnotationHandler {
        public Object getAnnotationValue(Annotation annotation,
                                         String name,
                                         Class<?> type)
                throws InjectionException {
            return lookupJNDI(name, type);
        }
    }

    private class JAXWSWebServiceRefAnnotationHandler extends
            WebServiceRefAnnotationHandler {
        public Object getAnnotationValue(Annotation annotation,
                                         String name,
                                         Class<?> type)
                throws InjectionException {
            return lookupJNDI(name, type);
        }
    }
}
