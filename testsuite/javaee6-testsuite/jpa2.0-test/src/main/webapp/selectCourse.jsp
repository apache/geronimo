<!--

	Licensed to the Apache Software Foundation (ASF) under one or more
	contributor license agreements. See the NOTICE file distributed with
	this work for additional information regarding copyright ownership.
	The ASF licenses this file to You under the Apache License, Version
	2.0 (the "License"); you may not use this file except in compliance
	with the License. You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0 Unless required by
	applicable law or agreed to in writing, software distributed under the
	License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
	CONDITIONS OF ANY KIND, either express or implied. See the License for
	the specific language governing permissions and limitations under the
	License.
-->

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Set" %>
<%@page import="java.util.List" %>
<%@page import="org.apache.geronimo.javaee6.jpa20.entities.Student" %>
<%@page import="org.apache.geronimo.javaee6.jpa20.entities.Course" %>
<%@page import="org.apache.geronimo.javaee6.jpa20.bean.Facade"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%
                    String sid = request.getParameter("sid");
                    Facade facade = new Facade();
                    int intsid = Integer.parseInt(sid);
                    Student student = facade.findStudent(intsid);
        %>
        <a>Welcome!Student Name:<%=student.getInfo().getName()%></a>
        <%
        List<Course> selectedCourses = student.getCourses();
        %>

        <h2>Want to Select Course?</h2>
        <ol>
            <%
                        List<Course> courses = facade.findAllCourse();
                        for (Course course : courses) {
                            if (!selectedCourses.contains(course)){
            %>
                <li>Course name:
                    Here Select <%=course.getCname()%> from selectCourse.jsp
                    <a href="CourseSelect?cid=<%=course.getCid()%>&sid=<%=student.getId()%>">Select This Course</a>
                </li>

            <%}}%>
        </ol>

         <h2>Want to Cancel selected Course?</h2>

        <ol>
            <%for (Course course : selectedCourses) {%>
            <li>Course name:
                    Here Unselect <%=course.getCname()%> from selectCourse.jsp
                    <a href="CourseUnselect?cid=<%=course.getCid()%>&sid=<%=student.getId()%>">Unselect This Course</a>
            </li>
            <%}%>
        </ol>

         <h2>All Courses:</h2>
         <ol>
         <%for(Course course: courses){%>
            <li>Course name:
                All courses includes <%=course.getCname()%> from selectCourse.jsp<br/>
            </li>
         <%}%>
         </ol>
    </body>
</html>
