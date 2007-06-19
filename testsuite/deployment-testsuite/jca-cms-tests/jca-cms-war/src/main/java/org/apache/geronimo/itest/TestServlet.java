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
package org.apache.geronimo.itest;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import javax.annotation.Resource;


/**
 * @version $Rev$ $Date$
 */
public class TestServlet extends HttpServlet {

    @Resource(name="configuredsecurityds")
    private DataSource csds;
    @Resource(name="cmsds")
    private DataSource cmsds;

    public void init() {
        System.out.println("Test Servlet init");
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        //this should create the database
        if (csds == null) {
            out.println("No configured datasource found");
            return;
        }
        try {
            Connection con = csds.getConnection();
            con.close();
            out.println("Successfully got configured connection\n");
        } catch (SQLException e) {
            out.println("Could not get configured connection");
            e.printStackTrace(out);
        }
        //now get a connection through the configured default subject
        if (cmsds == null) {
            out.println("No container managed datasource found");
            return;
        }
        try {
            Connection con = cmsds.getConnection();
            con.close();
            out.println("Successfully got container managed connection");
        } catch (SQLException e) {
            out.println("Could not get container managed connection");
            e.printStackTrace(out);
        }
    }


}
