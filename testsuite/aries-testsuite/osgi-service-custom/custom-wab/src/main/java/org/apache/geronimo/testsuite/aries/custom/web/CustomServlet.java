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
package org.apache.geronimo.testsuite.aries.custom.web;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import java.io.PrintWriter;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.apache.geronimo.testsuite.aries.custom.api.HelloService;
/**
 * Call Custom OSGI Registered Service
 *
 */
@WebServlet("/customservlet")
public class CustomServlet extends HttpServlet
{
    private ServletContext sc;
    private BundleContext bundleContext;   
    private static final String OSGI_SERVICE_PREFIX = "osgi:service/";
    
    /**
     * @see Servlet#init(ServletConfig)
     */
    public void init(ServletConfig config) throws ServletException {
       sc = config.getServletContext();       
    }
    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        performTask(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        performTask(request, response);
    }
    
    protected void performTask(HttpServletRequest request, HttpServletResponse response){
        try {
            bundleContext = (BundleContext) sc.getAttribute("osgi-bundlecontext");
            PrintWriter pw = response.getWriter();
            if (bundleContext != null) {
                ServiceReference sr = bundleContext.getServiceReference(HelloService.class.getName());
                if (sr != null) {
                    HelloService sm = (HelloService) bundleContext.getService(sr);
                    pw.println(sm.sayHello());
                    bundleContext.ungetService(sr);
                }
            } else {
                pw.println("Bundle Context is Null");
            }
          
            //JNDI Lookup via osgi:service
            HelloService sm = (HelloService) getOSGIService(HelloService.class.getName(),null);
            if(sm != null){
                pw.println(sm.sayHello()); 
            }else {
                pw.println("Service Lookup is Null");
            }
            
           
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        
    } 
    
    /**
     * Lookup and return an osgi service
     * 
     * @return Object
     * 
     */
    public static final Object getOSGIService(String serviceName, String filter) {
        
        String name = OSGI_SERVICE_PREFIX + serviceName;
        if (filter != null) {
            name = name + "/" + filter;
        }

        try {
            InitialContext ic = new InitialContext();
            return ic.lookup(name);
        } catch (NamingException e) {            
            e.printStackTrace();
            return null;
        }
    }

}
