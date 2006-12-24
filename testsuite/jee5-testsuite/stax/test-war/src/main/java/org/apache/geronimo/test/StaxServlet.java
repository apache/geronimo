/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.geronimo.test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLEventFactory;

public class StaxServlet extends HttpServlet {

    public void doGet(HttpServletRequest theRequest,
                      HttpServletResponse theResponse) 
        throws IOException {

        PrintWriter pw = theResponse.getWriter();

        theResponse.setContentType("text/html");
        pw.println("<html><head><title>Servlet</title></head>");
        pw.println("<body>");

        try {
            XMLInputFactory inFactory = XMLInputFactory.newInstance();
            pw.println(inFactory.getClass().getName());
            StaxTest.testParse(inFactory);
            XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
            pw.println(outFactory.getClass().getName());
            StaxTest.testStreamGenerate(outFactory);
            XMLEventFactory eventFactory = XMLEventFactory.newInstance();
            pw.println(eventFactory.getClass().getName());
            StaxTest.testEventGenerate(outFactory, eventFactory);
        } catch (Exception e) {
            e.printStackTrace();
            IOException exception = new IOException("Error");
            exception.initCause(e);
            throw exception;
        }
        
        pw.println("</body></html>");
    }

}
