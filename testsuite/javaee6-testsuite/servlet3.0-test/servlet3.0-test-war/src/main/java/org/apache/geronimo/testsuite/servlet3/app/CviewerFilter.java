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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class CviewerFilter implements Filter {

    public CviewerFilter() {
    }


    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        try {

            String className = request.getParameter("class");
            String message = "";

            if (className != null) {
                System.out.println("Got Classname = " + className + "\n");
            }
            // if nothing is provided, default to the String class
            if (className == null || className.length() == 0) {
                message += "You have input nothing.We set it to the default class :java.lang.Integer.<br>";
                className = "java.lang.Integer";
            }

            Class<?> clazz = null;

            try {
                clazz = Class.forName(className);
                message += "The class "+className+" is valid.The detail information is:<br>";

            } catch (ClassNotFoundException e) {
                System.out.println("Died in Class.forName " + className + " \n");
                message += "You have input an invalid class.So we set it to default class:java.lang.String<br>";
                className = "java.lang.String";
            }
            request.setAttribute("message", message);
            request.setAttribute("className", className);
            chain.doFilter(request, response);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }


    public void init(FilterConfig filterConfig) {
    }


    public void destroy() {
    }


    @Override
    public String toString() {
       // if (filterConfig == null) {
       //      return ("NewFilter()");
       //  }
        StringBuilder sb = new StringBuilder("NewFilter(");
        //sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    }

}
