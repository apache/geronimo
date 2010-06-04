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
package org.apache.geronimo.test.jpa.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.test.jpa.entity.Author;
import org.apache.geronimo.test.jpa.entity.BlogPersistenceService;

public class BlogServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BlogPersistenceService service = getBlogPersistenceService();
        
        service.createAuthor("john@gmail.com", new Date(), "John Doe", "JDoe", "Test Name");
        service.createBlogPost("john@gmail.com", "First Blog Entry", "Hello. This is my first blog entry", null);
        
        Author author = service.getAuthor("john@gmail.com");
        
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>OSGi JPA Test Application</title></head></html>");
        out.println("<body><h1>");
        out.println("Blog entry successfully created: " + author.getDisplayName());
        out.println("</h1></body></html>");
    }
    
    private BlogPersistenceService getBlogPersistenceService() throws ServletException {
        try {
            InitialContext ctx = new InitialContext();
            return (BlogPersistenceService) ctx.lookup("osgi:service/BlogService");
        } catch (NamingException e) {
            throw new ServletException("Unable to obtain BlogService", e);
        }
    }
}
