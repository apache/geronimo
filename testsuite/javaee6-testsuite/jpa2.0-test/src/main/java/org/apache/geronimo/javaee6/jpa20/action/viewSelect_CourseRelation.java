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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ejb.EJB;
import java.util.List;
import java.util.ArrayList;
import org.apache.geronimo.javaee6.jpa20.entities.Student;
import org.apache.geronimo.javaee6.jpa20.entities.Course;
import org.apache.geronimo.javaee6.jpa20.bean.Facade;

@WebServlet(name="viewSelect_CourseRelation", urlPatterns={"/viewSelect_CourseRelation"})
public class viewSelect_CourseRelation extends HttpServlet {
    @EJB
    private Facade facade = null ;
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=8859_1");
        PrintWriter out = response.getWriter();
        try {
            String sid = request.getParameter("sid");
            int intsid = Integer.parseInt(sid);
            Student student = facade.findStudent(intsid);

            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet selectCourse</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<a>Welcome!Student Name:"+student.getInfo().getName()+"</a>");
			
			List<Course> courses = facade.findAllCourse();

			out.println("<h2>Want to Select Course?</h2>");
            List<Course> selectedCourses = new ArrayList<Course>();
			selectedCourses = student.getCourses();
			{
				out.println("</ol>");
				for (Course course : courses) {
					if (selectedCourses==null || !selectedCourses.contains(course)){
						out.println("<li>Course name:");
						out.println("Click Here to Select "+course.getCname()+" from selectCourse ");
						out.println("<a href=\"CourseSelect?cid="+course.getCid()+"&sid="+student.getId()+"\">Select This Course</a>");
						out.println("</li>");
						}
				}
				out.println("</ol>");
			}
			
            out.println("<h2>Want to Cancel selected Course?</h2>");
			if(student.getCourses()==null){
				out.println("<p>INFO:Student has no course selected.</p>");
			}
			else
			{
				selectedCourses = student.getCourses();
				out.println("<ol>");
				for (Course course : selectedCourses) {
					out.println("<li>Course name:");
					out.println("Click Here to Unselect "+course.getCname()+" from selectCourse");
					out.println("<a href=\"CourseUnselect?cid="+course.getCid()+"&sid="+student.getId()+"\">Unselect This Course</a>");
					out.println("</li>");
				}
				out.println("</ol>");
			}

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
