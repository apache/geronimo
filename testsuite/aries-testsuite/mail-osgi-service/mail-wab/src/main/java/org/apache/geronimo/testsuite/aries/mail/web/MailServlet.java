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
package org.apache.geronimo.testsuite.aries.mail.web;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import java.io.PrintWriter;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;

import javax.mail.Session;
/**
 * Call Java Mail Session via osgi:service
 *
 */
@WebServlet("/mailservlet")
public class MailServlet extends HttpServlet
{
           
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
        PrintWriter pw = null;
        try {
           pw = response.getWriter();
           InitialContext ic = new InitialContext();
           
           Session mailSession = (Session) ic.lookup("ger:MailSession");
            if (mailSession != null){               
                pw.println("Java Mail JNDI Lookup via ger:MailSession Pass" ); 
            } else {
                pw.println("Java Mail JNDI Lookup via ger:MailSession Fail"); 
            }
            
            mailSession = (Session) ic.lookup("jca:/org.apache.geronimo.configs/javamail/JavaMailResource/mail/MailSession");
            if (mailSession != null){               
                pw.println("Java Mail JNDI Lookup via jca:/org.apache.geronimo.configs/javamail/JavaMailResource/mail/MailSession Pass" ); 
            } else {
                pw.println("Java Mail JNDI Lookup via jca:/org.apache.geronimo.configs/javamail/JavaMailResource/mail/MailSession Fail"); 
            }
           
        } catch (IOException e) {
            // TODO Auto-generated catch block
            pw.println(e.toString());            
        } catch (NamingException e){
          pw.println(e.toString());
        }
        
    }
}