/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jetty;

import java.util.Map;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.WebApplicationHandler;

/**
 * @version $Rev$ $Date$
 */
public class JettyFilterHolder extends FilterHolder {

    //todo consider an interface instead of this constructor for endpoint use.
    public JettyFilterHolder() {
    }

    public JettyFilterHolder(String filterName, String filterClass, Map initParams, JettyServletRegistration jettyServletRegistration) throws Exception {
        super(jettyServletRegistration == null? null: jettyServletRegistration.getServletHandler(), filterName, filterClass);
        if (jettyServletRegistration != null) {
            ((WebApplicationHandler)jettyServletRegistration.getServletHandler()).addFilterHolder(this);
            putAll(initParams);
            
            ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
            try {
                ClassLoader newCL = jettyServletRegistration.getWebClassLoader();
                Thread.currentThread().setContextClassLoader(newCL);
                start();
            } finally {
                Thread.currentThread().setContextClassLoader(oldCL);
            }
        }
    }

    public String getFilterName() {
        return getName();
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
