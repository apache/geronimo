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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

/**
 * Servlet implementation class DoTransfer
 */
public class DoTransfer extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // @Resource(lookup="java:app/SHAcc")
    // DataSource ds_SH;

    // @Resource(lookup="java:app/SHAcc")
    // DataSource ds_BJ;

    public DoTransfer() {
        super();

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int amount = new Integer(request.getParameter("amount")).intValue();
        int isFaildFlag = new Integer(request.getParameter("flag")).intValue();
        boolean failFlag = (isFaildFlag == 1) ? true : false;
        String output = new String();

        UserTransaction tx = null;
        DataSource ds = null;
        Connection conn_SH = null;
        Connection conn_BJ = null;
        Statement stmt = null;
        String sql = null;
        ResultSet rs = null;

        try {
            Context initContext = new InitialContext();

            tx = (UserTransaction) initContext.lookup("java:comp/UserTransaction");

            // Start a transaction
            tx.begin();

            // System.out.println("tx begin!");

            // First, add to SH
            ds = (javax.sql.DataSource) initContext.lookup("java:app/SHAccTx");
            System.out.println("in DoTransfer, get java:app/SHAcc sucess!");
            // conn_SH = ds_SH.getConnection();
            conn_SH = ds.getConnection();
            conn_SH.setAutoCommit(false);
            stmt = conn_SH.createStatement();
            sql = "UPDATE ACCOUNTS set SAVINGS = SAVINGS+" + amount;
            stmt.executeUpdate(sql);
            // System.out.println("execute SQL 1 sucess!");

            if (failFlag) {
                throw new Exception("fail shoud happen!");
            }

            // Second, del in BJ
            ds = (javax.sql.DataSource) initContext.lookup("java:app/BJAccTx");
            // System.out.println("in DoTransfer, get java:app/BJAcc sucess!");
            // conn_BJ = ds_BJ.getConnection();
            conn_BJ = ds.getConnection();
            conn_BJ.setAutoCommit(false);
            stmt = conn_BJ.createStatement();
            rs = stmt.executeQuery("SELECT * FROM ACCOUNTS");
            int saving_BJ = 0;
            while (rs.next()) {
                saving_BJ = rs.getInt("SAVINGS");
                if (saving_BJ < amount) {
                    // throw a exception if no enough money left
                    throw new Exception("No enough money left in BJ Account!");
                }
            }

            sql = "UPDATE ACCOUNTS set SAVINGS = SAVINGS-" + amount;
            stmt.executeUpdate(sql);

            // commit all the operations
            tx.commit();

        } catch (Exception e) {
            if (tx != null) {
                try {
                    // rollback the operations
                    tx.rollback();
                    // System.out.println("catch: roll back success.");
                    output += "catch: roll back success.";
                } catch (Exception e1) {
                    // System.out.println("catch: roll back fail.");
                    output += "catch: roll back fail.";
                }
            }
            System.out.println("catch: " + e.getClass() + "; " + e.getMessage() + "");
        } finally {
            if (conn_SH != null) {
                try {
                    conn_SH.close();
                    // System.out.println("finally: close conn_SH success.");
                    output += "finally: close conn_SH success.";
                } catch (Exception e1) {
                    // System.out.println("finally: close conn_SH fail.");
                    output += "finally: close conn_SH fail.";
                }
            }
            if (conn_BJ != null) {
                try {
                    conn_BJ.close();
                    // System.out.println("finally: close conn_BJ success.");
                    output += "finally: close conn_BJ success.";
                } catch (Exception e1) {
                    // System.out.println("finally: close conn_BJ fail.");
                    output += "finally: close conn_BJ success.";
                }
            }
            request.setAttribute("output", output);
            // response.sendRedirect("AccountList.jsp");
            getServletContext().getRequestDispatcher("/display.jsp").forward(request, response);
        }
    }

}
