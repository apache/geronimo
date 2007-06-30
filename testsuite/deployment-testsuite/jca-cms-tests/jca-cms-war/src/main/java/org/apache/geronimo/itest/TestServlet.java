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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.security.Principal;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import javax.annotation.Resource;
import javax.security.auth.Subject;

import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;


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
        Callers callers = ContextManager.getCallers();
        Subject current = callers.getCurrentCaller();
        Subject next = callers.getNextCaller();
        PrintWriter out = response.getWriter();
        out.println("Current subject: " + current);
        out.println("Next subject:    " + next);
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
            DatabaseMetaData md = con.getMetaData();
            ResultSet rs = md.getSchemas();
            while (rs.next()) {
                String schema = rs.getString(1);
                for (Principal p: next.getPrincipals()) {
                    String user = p.getName();
                    if (schema.equals(user)) {
                        out.println("expected schema: " + user);
                    }
                }
            }
            rs.close();
            con.close();
            out.println("Successfully got container managed connection");
        } catch (SQLException e) {
            out.println("Could not get container managed connection");
            e.printStackTrace(out);
        }
    }


}
