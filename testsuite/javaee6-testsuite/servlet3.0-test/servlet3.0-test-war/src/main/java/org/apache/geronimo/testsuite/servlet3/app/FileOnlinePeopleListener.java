/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.testsuite.servlet3.app;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener()
public class FileOnlinePeopleListener implements ServletContextListener, HttpSessionListener {

    private int onlineNumber;
    private ServletContext context = null;

    public FileOnlinePeopleListener() {
        onlineNumber = 0;
    }

    public void sessionCreated(HttpSessionEvent se) {
        onlineNumber++;
        se.getSession().getServletContext().setAttribute("onLineNumber", new Integer(onlineNumber));
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        onlineNumber--;
        se.getSession().getServletContext().setAttribute("onLineNumber", new Integer(onlineNumber));
    }

    public void contextInitialized(ServletContextEvent event) {

        this.context = event.getServletContext();

    }

    public void contextDestroyed(ServletContextEvent event) {

        this.context = null;

    }
}
