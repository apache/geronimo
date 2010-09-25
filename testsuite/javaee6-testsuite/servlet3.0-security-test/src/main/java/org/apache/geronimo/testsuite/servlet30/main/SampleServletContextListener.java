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

package org.apache.geronimo.testsuite.servlet30.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.HttpConstraintElement;
import javax.servlet.HttpMethodConstraintElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.ServletSecurityElement;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebListener;

/**
 * @version $Rev$ $Date$
 */
@WebListener
public class SampleServletContextListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        ServletContext servletContext = servletContextEvent.getServletContext();

        //dynamic register /SampleServlet3Dynamic and security constraint
        Dynamic servlet3Dynamic = servletContext.addServlet("SampleServlet3Dynamic", SampleServlet3.class);
        servlet3Dynamic.addMapping("/SampleServlet3Dynamic", "/TestDynamic");
        HttpConstraintElement httpConstraintElement = new HttpConstraintElement();
        List<HttpMethodConstraintElement> httpMethodConstraintElements = new ArrayList<HttpMethodConstraintElement>();
        httpMethodConstraintElements.add(new HttpMethodConstraintElement("GET", new HttpConstraintElement(ServletSecurity.TransportGuarantee.NONE, "RoleC")));
        ServletSecurityElement servletSecurityElement = new ServletSecurityElement(httpConstraintElement, httpMethodConstraintElements);
        Set<String> uneffectedUrlPatterns = servlet3Dynamic.setServletSecurity(servletSecurityElement);
        if (uneffectedUrlPatterns.size() == 0) {
            throw new RuntimeException("/SampleServlet3Dynamic should be returned as it is defined in the web.xml file");
        }
        servlet3Dynamic.addMapping("/TestDynamicAfter");
    }
}
