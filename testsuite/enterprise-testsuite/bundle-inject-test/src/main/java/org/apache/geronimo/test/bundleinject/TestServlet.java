/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
*/

package org.apache.geronimo.test.bundleinject;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.annotation.Resource;
import javax.ejb.EJB;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class TestServlet extends HttpServlet {

    @Resource
    private Bundle bundle;

    @Resource
    private BundleContext bundleContext;

    @EJB
    private TestBean testBean;

    protected void processRequest(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        try {
            String testName = req.getParameter("testName");
            
            out.println("Test name:" + testName);
            if ("servlet".equals(testName)) {
                out.println(getOutput());
            } else if ("ejb".equals(testName)) {
                out.println(testBean.getOutput());      
            } else if ("jsp".equals(testName)) {
                getServletContext().getRequestDispatcher("/test.jsp").forward(req, response);
            } else {
                throw new Exception("Invalid test name: " + testName);
            }            
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    } 

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    } 

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
    
    public String getOutput() {
        StringBuilder buf = new StringBuilder();
        
        buf.append("Bundle: " + bundle.getSymbolicName());
        buf.append(" ");
        buf.append("BundleContext: ").append((bundleContext != null) ? "ok" : "failed");
        
        return buf.toString();
    }

}
