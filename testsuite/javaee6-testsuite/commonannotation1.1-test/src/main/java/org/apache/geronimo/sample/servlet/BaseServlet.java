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
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

public abstract class BaseServlet extends HttpServlet {

    abstract DataSource getDataSourceA();
    
    abstract DataSource getDataSourceB();
      
    String getTitle() {
        return getClass().getName();
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        out.println("<html><head><title>");
        out.println(getTitle());
        out.println("</title></head></html><body>");

        try {
            initDB();
        } catch (SQLException e) {
            throw new ServletException("Error creating database", e);
        }
        
        List<Contact> contacts;
        
        try {
            contacts = getContacts();
        } catch (SQLException e) {
            throw new ServletException("Error accessing database", e);
        }
        
        for (Contact c : contacts) {
            out.println(c.firstName + " " + c.lastName + " " + c.phone + "<br>");
        }

        out.println("</body></html>");

    }
	
    public void init(ServletConfig config) {
        System.out.println("Initializing servlet");
    }
	    
    public void initDB() throws SQLException {
        Connection con = getDataSourceA().getConnection();
        Statement stmt = con.createStatement();

        try {
            stmt.executeUpdate("DROP TABLE CONTACTS");
        } catch (SQLException e) {
            // ignore
        }
        
        try {
            stmt.executeUpdate("CREATE TABLE CONTACTS (ID INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, FIRSTNAME VARCHAR(25), LASTNAME VARCHAR(25), PHONE VARCHAR(25))");
        } finally {
            stmt.close();
            con.close();
        }

        String ts = String.format("%1$tM%1$tS", Calendar.getInstance());
        addContact("Joe", "Smith", "111 111-" + ts);
        addContact("Jane", "Doe", "222 222-" + ts);
    }

    public List<Contact> getContacts() throws SQLException {
        List<Contact> contacts = new ArrayList<Contact>();
        Connection con = getDataSourceB().getConnection();
        Statement stmt = con.createStatement();
        try {
            ResultSet rs = stmt.executeQuery(
                    "SELECT ID, FIRSTNAME, LASTNAME, PHONE FROM CONTACTS");
            while (rs.next()) {
                Contact c = new Contact();
                c.id = rs.getLong(1);
                c.firstName = rs.getString(2);
                c.lastName = rs.getString(3);
                c.phone = rs.getString(4);
                contacts.add(c);
            }
        } finally {
            stmt.close();
            con.close();
        }

        return contacts;
    }

    public void addContact(String firstName, String lastName, String phone) throws SQLException {
        Connection con = getDataSourceA().getConnection();
        PreparedStatement pstmt = con.prepareStatement(
            "INSERT INTO CONTACTS (FIRSTNAME, LASTNAME, PHONE) VALUES (?, ?, ?)");
        try {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, phone);
            pstmt.executeUpdate();
        } finally {
            pstmt.close();
            con.close();
        }
    }
    
    private static class Contact {
        long id;
        String firstName;
        String lastName;
        String phone;
    }
}
