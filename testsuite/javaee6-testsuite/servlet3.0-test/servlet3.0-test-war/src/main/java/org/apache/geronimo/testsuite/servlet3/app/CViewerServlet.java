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

package org.apache.geronimo.testsuite.servlet3.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CViewerServlet extends HttpServlet
{
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res)
  throws ServletException, IOException {

    res.setContentType("text/html");
    PrintWriter out = res.getWriter();
    String className = req.getAttribute("className").toString();
    Class clazz = null;

    try
    {
      clazz = Class.forName(className);
    }
    catch (ClassNotFoundException e)
    {
      System.out.println ("Died in Class.forName " + className + " \n");
      throw new ServletException(e);
    }

    Method[] methods = clazz.getDeclaredMethods();
    Class superclass = clazz.getSuperclass();
    Class[] interfaces = clazz.getInterfaces();

    out.print("<html>");
    out.print("<head><title>Class Viewer Servlet</title></head>");
    out.print("<body>");
    out.print("<font color=green><b>Message:</b></font><br>");
    out.print("<font color=red><b>"+req.getAttribute("message")+"</b></font>");
    out.print("<font color=green><b>ClassName:</b></font><br>");
    out.print(clazz.getName());
    out.print("<br><font color=green><b>Extends:</b></font><br>");
    out.print(superclass);
    out.print("<br><font color=green><b>Implements:</b></font><br>");
    for (int i=0; i < interfaces.length; i++)
    {
      out.println(interfaces[i]);
    }
    out.print("<br><font color=green><b>Methods:</b></font><br>");
    for (int i=0; i < methods.length; i++)
    {
      out.print(methods[i]+"<br>");
    }
    out.print("</body>");
    out.print("</html>");
  }
}
