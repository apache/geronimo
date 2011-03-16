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
package org.apache.geronimo.testsuite.ds.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;



/**
 * Servlet implementation class DSServlet
 */
@WebServlet("/dsservlet")
public class DSServlet extends HttpServlet {
   private static final long serialVersionUID = 1L;
   private DataSource ds = null;
   private static final String OSGI_SERVICE_PREFIX = "osgi:service/";

       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DSServlet() {
        super();
        // TODO Auto-generated constructor stub
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
            PrintWriter pw = response.getWriter();
            
            ds = (DataSource) getOSGIService(DataSource.class.getName() , "(osgi.jndi.service.name=jdbc/NoTxjndidbDataSource)");           
            if(ds != null){
                pw.println("Datasource OSGI Service JNDI Lookup Pass");
            } else{
                pw.println("Datasource OSGI Service JNDI Lookup Fail");
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
