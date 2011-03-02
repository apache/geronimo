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

package org.apache.geronimo.samples.requirebundle.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.geronimo.samples.requirebundle.bean.CalculatorBean;
import org.apache.geronimo.samples.requirebundle.bean.reexport.ExportBean;


public class CalculatorServlet extends HttpServlet {
    protected void processRequest(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
        	out.println("<html><body>");
            DecimalFormat mydf = new DecimalFormat("####.#");
            CalculatorBean cb = new CalculatorBean();
			ExportBean eb = new ExportBean();
            String re = eb.hello_reexport();
            out.println("<h1>This sample presents features:require-bundle</h1>");
			out.println("<p>"+re+"</p>");
            out.println("<li>Bean_B [\"CalculatorBean\"] says, result of ADD operation [\"10.0 + 8.0 = ?\"] is: " + mydf.format(cb.add(10.0, 8.0))+"</li>");
            out.println("<li>Bean_B [\"CalculatorBean\"] says, result of SUB operation [\"10.0 - 8.0 = ?\"] is: " + mydf.format(cb.sub(10.0, 8.0))+"</li>");
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
        return "CalculatorServlet in require-bundle-test";
    }

}

