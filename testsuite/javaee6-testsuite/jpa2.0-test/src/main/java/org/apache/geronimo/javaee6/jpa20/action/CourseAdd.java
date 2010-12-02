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

import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.javaee6.jpa20.bean.Facade;
import org.apache.geronimo.javaee6.jpa20.entities.Course;


@WebServlet(name="CourseAdd", urlPatterns={"/CourseAdd"})
public class CourseAdd extends HttpServlet {
    @EJB
    private Facade coursefacade = null;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=8859_1");
        PrintWriter out = response.getWriter();
        try {
            String cid=request.getParameter("cid");
            String cname=request.getParameter("cname");
            String classroom=request.getParameter("classroom");
            String teacher=request.getParameter("teacher");
            String assistTeacher=request.getParameter("assistTeacher");
            Course course=new Course();
            course.setCid(Integer.parseInt(cid));
            course.setCname(cname);
            course.setClassroom(classroom);
            course.setTeacher(teacher);
            course.setAssistTeacher(assistTeacher);
//            System.out.println("in CourseAdd, cname is:"+cname);
            coursefacade.createCourse(course);
            RequestDispatcher dispatcher=request.getRequestDispatcher("viewCourses");
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
    }// </editor-fold>

}
