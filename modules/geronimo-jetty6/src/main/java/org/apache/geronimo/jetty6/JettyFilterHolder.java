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
package org.apache.geronimo.jetty6;

import java.util.Map;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.annotation.Injection;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.xbean.recipe.StaticRecipe;
import org.mortbay.jetty.servlet.FilterHolder;

/**
 * @version $Rev$ $Date$
 */
public class JettyFilterHolder implements GBeanLifecycle {

    private final FilterHolder filterHolder;

    //todo consider an interface instead of this constructor for endpoint use.
    public JettyFilterHolder() {
        filterHolder = null;
    }

    public JettyFilterHolder(String filterName, String filterClass, Map initParams, JettyServletRegistration jettyServletRegistration) throws Exception {
        filterHolder = new InternalFilterHolder(jettyServletRegistration);
        if (jettyServletRegistration != null) {
            filterHolder.setName(filterName);
            filterHolder.setClassName(filterClass);
            filterHolder.setInitParameters(initParams);
            (jettyServletRegistration.getServletHandler()).addFilter(filterHolder);

//            ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
//            try {
//                ClassLoader newCL = jettyServletRegistration.getWebClassLoader();
//                Thread.currentThread().setContextClassLoader(newCL);
//                start();
//            } finally {
//                Thread.currentThread().setContextClassLoader(oldCL);
//            }
        }
    }

    public String getFilterName() {
        return filterHolder.getName();
    }

    public void doStart() throws Exception {
            filterHolder.start();
    }

    public void doStop() throws Exception {
                filterHolder.stop();
    }

    public void doFail() {
        try {
            filterHolder.stop();
        } catch (Exception e) {
            //ignore?
        }
    }

    private static class InternalFilterHolder extends FilterHolder {
        private final JettyServletRegistration servletRegistration;

        public InternalFilterHolder(JettyServletRegistration servletRegistration) {
            this.servletRegistration = servletRegistration;
        }


        public synchronized Object newInstance() throws InstantiationException, IllegalAccessException {
            return servletRegistration.newInstance(_class);
        }
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(JettyFilterHolder.class, NameFactory.WEB_FILTER);
        infoBuilder.addAttribute("filterName", String.class, true);
        infoBuilder.addAttribute("filterClass", String.class, true);
        infoBuilder.addAttribute("initParams", Map.class, true);

        infoBuilder.addReference("JettyServletRegistration", JettyServletRegistration.class, NameFactory.WEB_MODULE);

        infoBuilder.setConstructor(new String[] {"filterName", "filterClass", "initParams", "JettyServletRegistration"});

        GBEAN_INFO = infoBuilder.getBeanInfo();

    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
