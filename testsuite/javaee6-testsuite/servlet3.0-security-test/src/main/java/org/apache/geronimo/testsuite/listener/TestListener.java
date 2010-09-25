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
package org.apache.geronimo.testsuite.listener;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.annotation.WebListener;

import org.apache.geronimo.testsuite.servlet30.main.SampleServlet5;
import org.apache.geronimo.testsuite.servlet30.main.SampleServlet6;

@WebListener (value="testListener")
public class TestListener implements ServletContextListener {

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        ServletContext servletContext = arg0.getServletContext();
        try {
            // addServlet takes instance created by ServletContext.createServlet()
            Servlet servlet5_1 = servletContext.createServlet(SampleServlet5.class);
            Dynamic dynamic5_1 = servletContext.addServlet("Sample Servlet 5_1", servlet5_1);
            dynamic5_1.addMapping("/SampleServlet5_1", "/SampleServlet5_1/*");

            // addServlet takes instance which is not created by ServletContext.createServlet()
            Dynamic dynamic5_2 = servletContext.addServlet("Sample Servlet 5_2", new SampleServlet5());
            dynamic5_2.addMapping("/SampleServlet5_2", "/SampleServlet5_2/*");

            // addServlet takes className
            Dynamic dynamic5_3 = servletContext.addServlet("Sample Servlet 5_3", SampleServlet5.class);
            dynamic5_3.addMapping("/SampleServlet5_3", "/SampleServlet5_3/*");

            //
            Servlet servlet6 = servletContext.createServlet(SampleServlet6.class);
            Dynamic dynamic6 = servletContext.addServlet("Sample Servlet 6", servlet6);
            dynamic6.addMapping("/SampleServlet6_1");

        } catch (ServletException e) {
            e.printStackTrace();
        }
    }
}
