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

package org.apache.geronimo.testsuite.javaee6.ejbtest.Servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ejb.EJB;
import org.apache.geronimo.testsuite.javaee6.ejbtest.EJBBeans.CalculatorSingleBean;
import java.text.DecimalFormat;

public class CalculatorServlet extends HttpServlet {
    // the ejb container will route every request to the same Singleton EJB.
    @EJB
    private CalculatorSingleBean calcAdd;
    @EJB
    private CalculatorSingleBean calcSub;

    protected void processRequest(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String Number = req.getParameter("NumberValue");
            String operation = req.getParameter("operation");

            double result = (Number == null) ? 0 : Double.valueOf(Number).doubleValue();
            DecimalFormat mydf = new DecimalFormat("####.#");
            if ( "sub".equals(operation) ) {
                req.setAttribute("result", mydf.format(calcSub.sub(result)));
            } else if ( "add".equals(operation) ) {
                req.setAttribute("result", mydf.format(calcAdd.add(result)));
            }
            String output = this.calcSub.getOutput();
            System.out.println("Result is " + req.getAttribute("result"));
            System.out.println("Output is: "+ output);
            getServletContext().getRequestDispatcher("/index.jsp").forward(req, response);
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
        return "Short description";
    }

}

