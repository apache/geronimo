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

package org.apache.geronimo.javaee6.jpa20.action;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.geronimo.javaee6.jpa20.bean.Facade;
import org.apache.geronimo.javaee6.jpa20.entities.Address;
import org.apache.geronimo.javaee6.jpa20.entities.BasicInfo;
import org.apache.geronimo.javaee6.jpa20.entities.Student;
import javax.ejb.EJB;

@WebServlet(name="StudentAdd", urlPatterns={"/StudentAdd"})
public class StudentAdd extends HttpServlet {
    @EJB
    private Facade facade =null ;
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=8859_1");
        PrintWriter out = response.getWriter();
        try {
            String sid=request.getParameter("sid");
            String sname=request.getParameter("sname");
            String country=request.getParameter("country");
            String city=request.getParameter("city");
            String street=request.getParameter("street");
            String telephone=request.getParameter("telephone");
            String age=request.getParameter("age");
            String score=request.getParameter("score");
            double sco = Double.parseDouble(score);
            Address address=new Address();
            address.setCity(city);
            address.setCountry(country);
            address.setStreet(street);
            BasicInfo basicInfo=new BasicInfo();
            basicInfo.setAddress(address);
            basicInfo.setAge(Integer.parseInt(age));
            basicInfo.setName(sname);
            basicInfo.setTelephone(telephone);
            Student student=new Student();
            student.setId(Integer.parseInt(sid));
            student.setInfo(basicInfo);
            student.setRank("N/A");
            student.setTotalScore(sco+student.getTotalScore());
            
//            System.out.println("student info before em.persist:sname:"+sname+",Address.city"+address.getCity()+",BasicInfo.TEL:"+basicInfo.getTelephone());
            facade.createStudent(student);
            RequestDispatcher dispatcher=request.getRequestDispatcher("viewAllStudents");
            dispatcher.forward(request, response);
        } finally { 
            out.close();
        }
    } 

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
