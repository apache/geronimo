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


package org.apache.geronimo.jasper;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.j2ee.RuntimeCustomizer;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.tomcat.InstanceManager;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = GBeanInfoBuilder.DEFAULT_J2EE_TYPE)
public class JasperServletContextCustomizer implements RuntimeCustomizer {
    private final Holder holder;

    static {
        try {
            Class clazz = Class.forName("org.apache.geronimo.jdbc.DataSourceDriver");
            clazz.newInstance();
        } catch (Throwable e) {
            //how can we notify?
        }
    }

    public JasperServletContextCustomizer(@ParamAttribute(name = "holder") Holder holder) {
        this.holder = holder;
    }

    public void customize(Map<Class, Object> context) {
        Map<String, Object> servletContext = (Map<String, Object>) context.get(Map.class);
        Context jndiContext = (Context) context.get(Context.class);
        servletContext.put(InstanceManager.class.getName(), new JasperInstanceManager(holder, jndiContext));
    }


    public static class JasperInstanceManager implements InstanceManager {
        private final Holder holder;
        private final Context context;

        public JasperInstanceManager(Holder holder, Context context) {
            this.holder = holder;
            this.context = context;
        }

        public Object newInstance(String fqcn, ClassLoader classLoader) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
            return holder.newInstance(fqcn, classLoader, context);
        }

        public void destroyInstance(Object o) throws IllegalAccessException, InvocationTargetException {
            try {
                holder.destroyInstance(o);
            } catch (Exception e) {
                throw new InvocationTargetException(e, "Attempted to destroy instance");
            }
        }

        public void newInstance(Object o) throws IllegalAccessException, InvocationTargetException, NamingException {
            throw new UnsupportedOperationException("separate instantiation and injection is not supported");
        }

        public Object newInstance(String fqcn) throws IllegalAccessException, InvocationTargetException, NamingException,
                InstantiationException, ClassNotFoundException {
            throw new UnsupportedOperationException("separate instantiation and injection is not supported");
        }
    }

}
