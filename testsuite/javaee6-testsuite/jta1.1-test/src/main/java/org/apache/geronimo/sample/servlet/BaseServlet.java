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

package org.apache.geronimo.sample.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

public abstract class BaseServlet extends HttpServlet {

    abstract DataSource getNonTxDataSourceA();

    abstract DataSource getTxDataSourceB();

    String getTitle() {
        return getClass().getName();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        out.println("<html><head><title>");
        out.println(getTitle());
        out.println("</title></head></html><body>");

        try {
            initDB();
        } catch (SQLException e) {
            throw new ServletException("Error creating database", e);
        }

        List<Account> Accounts;

        try {
            Accounts = getAccounts();
        } catch (SQLException e) {
            throw new ServletException("Error accessing database", e);
        }

        for (Account c : Accounts) {
            out.println(c.savings + "<br>");
        }

        out.println("<a href=\"index.jsp\">Return</a>");
        out.println("</body></html>");

    }

    public void init(ServletConfig config) {
        // System.out.println("Initializing servlet");
    }

    public void initDB() throws SQLException {
        Connection con = getNonTxDataSourceA().getConnection();
        Statement stmt = con.createStatement();

        try {
            stmt.executeUpdate("DROP TABLE ACCOUNTS");
        } catch (SQLException e) {
            // ignore
        }

        try {
            stmt
                    .executeUpdate("CREATE TABLE ACCOUNTS (ID INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, SAVINGS INTEGER)");
        } finally {
            stmt.close();
            con.close();
        }

        addAccount(200);
        // addAccount(100);
    }

    public List<Account> getAccounts() throws SQLException {
        List<Account> Accounts = new ArrayList<Account>();
        Connection con = getTxDataSourceB().getConnection();
        Statement stmt = con.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT ID, SAVINGS FROM ACCOUNTS");
            while (rs.next()) {
                Account c = new Account();
                c.id = rs.getLong(1);
                c.savings = rs.getLong(2);
                Accounts.add(c);
            }
        } finally {
            stmt.close();
            con.close();
        }

        return Accounts;
    }

    public void addAccount(long sav) throws SQLException {
        Connection con = getTxDataSourceB().getConnection();
        PreparedStatement pstmt = con.prepareStatement("INSERT INTO ACCOUNTS (SAVINGS) VALUES (?)");
        try {
            pstmt.setLong(1, sav);
            pstmt.executeUpdate();
        } finally {
            pstmt.close();
            con.close();
        }
    }

    private static class Account {
        long id;

        long savings;
    }
}
