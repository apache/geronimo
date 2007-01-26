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
package org.apache.geronimo.cxf;

import java.lang.annotation.Annotation;

import javax.naming.NamingException;
import javax.xml.ws.WebServiceContext;

import org.apache.geronimo.cxf.annotations.AnnotationProcessor;
import org.apache.geronimo.cxf.annotations.EJBAnnotationHandler;
import org.apache.geronimo.cxf.annotations.InjectionException;
import org.apache.geronimo.cxf.annotations.ResourceAnnotationHandler;
import org.apache.geronimo.cxf.annotations.WebServiceRefAnnotationHandler;

public class CXFAnnotationProcessor extends AnnotationProcessor {

    private JNDIResolver jndiResolver;
    
    public CXFAnnotationProcessor(JNDIResolver jndiResolver) {
        this.jndiResolver = jndiResolver;
        
        // register @Resource annotation handler
        registerHandler(new CXFResourceAnnotationHandler());
        // register @EJB annotation handler
        registerHandler(new CXFEJBAnnotationHandler());
        // register @WebServiceRef annotation handler
        registerHandler(new CXFWebServiceRefAnnotationHandler());
    }

    private Object lookupJNDI(String name, Class<?> type)
            throws InjectionException {
        try {
            return jndiResolver.resolve(name, type);
        } catch (NamingException e) {
            throw new InjectionException("JNDI injection failed", e);
        }
    }

    private class CXFResourceAnnotationHandler extends
            ResourceAnnotationHandler {
        public Object getAnnotationValue(Annotation annotation,
                                         String name,
                                         Class<?> type)
                throws InjectionException {
            if (WebServiceContext.class.isAssignableFrom(type)) {
                return type.cast(new CXFWebServiceContext());
            } else {
                return lookupJNDI(name, type);
            }
        }
    }

    private class CXFEJBAnnotationHandler extends EJBAnnotationHandler {
        public Object getAnnotationValue(Annotation annotation,
                                         String name,
                                         Class<?> type)
                throws InjectionException {
            return lookupJNDI(name, type);
        }
    }

    private class CXFWebServiceRefAnnotationHandler extends
            WebServiceRefAnnotationHandler {
        public Object getAnnotationValue(Annotation annotation,
                                         String name,
                                         Class<?> type)
                throws InjectionException {
            return lookupJNDI(name, type);
        }
    }
}
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
package org.apache.geronimo.cxf;

import java.lang.annotation.Annotation;

import javax.naming.NamingException;
import javax.xml.ws.WebServiceContext;

import org.apache.geronimo.cxf.annotations.AnnotationProcessor;
import org.apache.geronimo.cxf.annotations.EJBAnnotationHandler;
import org.apache.geronimo.cxf.annotations.InjectionException;
import org.apache.geronimo.cxf.annotations.ResourceAnnotationHandler;
import org.apache.geronimo.cxf.annotations.WebServiceRefAnnotationHandler;

public class CXFAnnotationProcessor extends AnnotationProcessor {

    private JNDIResolver jndiResolver;
    
    public CXFAnnotationProcessor(JNDIResolver jndiResolver) {
        this.jndiResolver = jndiResolver;
        
        // register @Resource annotation handler
        registerHandler(new CXFResourceAnnotationHandler());
        // register @EJB annotation handler
        registerHandler(new CXFEJBAnnotationHandler());
        // register @WebServiceRef annotation handler
        registerHandler(new CXFWebServiceRefAnnotationHandler());
    }

    private Object lookupJNDI(String name, Class<?> type)
            throws InjectionException {
        try {
            return jndiResolver.resolve(name, type);
        } catch (NamingException e) {
            throw new InjectionException("JNDI injection failed", e);
        }
    }

    private class CXFResourceAnnotationHandler extends
            ResourceAnnotationHandler {
        public Object getAnnotationValue(Annotation annotation,
                                         String name,
                                         Class<?> type)
                throws InjectionException {
            if (WebServiceContext.class.isAssignableFrom(type)) {
                return type.cast(new CXFWebServiceContext());
            } else {
                return lookupJNDI(name, type);
            }
        }
    }

    private class CXFEJBAnnotationHandler extends EJBAnnotationHandler {
        public Object getAnnotationValue(Annotation annotation,
                                         String name,
                                         Class<?> type)
                throws InjectionException {
            return lookupJNDI(name, type);
        }
    }

    private class CXFWebServiceRefAnnotationHandler extends
            WebServiceRefAnnotationHandler {
        public Object getAnnotationValue(Annotation annotation,
                                         String name,
                                         Class<?> type)
                throws InjectionException {
            return lookupJNDI(name, type);
        }
    }
}
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
package org.apache.geronimo.cxf;

import java.lang.annotation.Annotation;

import javax.naming.NamingException;
import javax.xml.ws.WebServiceContext;

import org.apache.geronimo.cxf.annotations.AnnotationProcessor;
import org.apache.geronimo.cxf.annotations.EJBAnnotationHandler;
import org.apache.geronimo.cxf.annotations.InjectionException;
import org.apache.geronimo.cxf.annotations.ResourceAnnotationHandler;
import org.apache.geronimo.cxf.annotations.WebServiceRefAnnotationHandler;

public class CXFAnnotationProcessor extends AnnotationProcessor {

    private JNDIResolver jndiResolver;
    
    public CXFAnnotationProcessor(JNDIResolver jndiResolver) {
        this.jndiResolver = jndiResolver;
        
        // register @Resource annotation handler
        registerHandler(new CXFResourceAnnotationHandler());
        // register @EJB annotation handler
        registerHandler(new CXFEJBAnnotationHandler());
        // register @WebServiceRef annotation handler
        registerHandler(new CXFWebServiceRefAnnotationHandler());
    }

    private Object lookupJNDI(String name, Class<?> type)
            throws InjectionException {
        try {
            return jndiResolver.resolve(name, type);
        } catch (NamingException e) {
            throw new InjectionException("JNDI injection failed", e);
        }
    }

    private class CXFResourceAnnotationHandler extends
            ResourceAnnotationHandler {
        public Object getAnnotationValue(Annotation annotation,
                                         String name,
                                         Class<?> type)
                throws InjectionException {
            if (WebServiceContext.class.isAssignableFrom(type)) {
                return type.cast(new CXFWebServiceContext());
            } else {
                return lookupJNDI(name, type);
            }
        }
    }

    private class CXFEJBAnnotationHandler extends EJBAnnotationHandler {
        public Object getAnnotationValue(Annotation annotation,
                                         String name,
                                         Class<?> type)
                throws InjectionException {
            return lookupJNDI(name, type);
        }
    }

    private class CXFWebServiceRefAnnotationHandler extends
            WebServiceRefAnnotationHandler {
        public Object getAnnotationValue(Annotation annotation,
                                         String name,
                                         Class<?> type)
                throws InjectionException {
            return lookupJNDI(name, type);
        }
    }
}
