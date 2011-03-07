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

package org.apache.geronimo.samples.requirebundle.resolution.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.geronimo.samples.requirebundle.bean.reexport.ExportBean;


public class checkResolutionAttrServlet extends HttpServlet {
    protected void processRequest(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
		    ExportBean eb = new ExportBean();
			String re = eb.hello_reexport();
        	out.println("<html><body>");
            out.println("<h1>This sample presents features:require-bundle-resolution:=optional</h1>");
			out.println("<p>"+re+"</p>");
			out.println("<p>Succeed to resolve Attr \"resolution=optional\"</p>");
            out.println("</body></html>");
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new ServletException(e);
        } finally {
            out.close();
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
        return "checkResolutionAttrServlet in require-bundle-test";
    }

}

