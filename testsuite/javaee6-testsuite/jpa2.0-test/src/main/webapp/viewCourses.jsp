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
<%@page import="java.util.List" %>
<%@page import="org.apache.geronimo.javaee6.jpa20.entities.Course" %>
<%@page import="javax.ejb.EJB" %>
<%@page import="org.apache.geronimo.javaee6.jpa20.bean.Facade" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <table border="1px">
            <tr>
                <td>Course Name</td>
                <td>Classroom</td>
                <td>Teacher</td>
                <td>Assist Teacher</td>
                <td>actions</td>
        
            </tr>
            <%

            Facade facade;
            List<Course> courses = facade.findAllCourse();
                   for (Course course : courses) {%>
            <tr>
                <td><%=course.getCname()%></td>
                <td><%=course.getClassroom()%></td>
                <td><%=course.getTeacher()%></td>
                <td><%=course.getAssistTeacher()%></td>
                <td>
                <a href="testComment.jsp?cid=<%=course.getCid()%>">Comments</a>
                <a href="queryCourseByName.jsp?cname=<%=course.getCname()%>">Query Course By Name</a>
                <a href="CourseDelete?cid=<%=course.getCid()%>">Delete</a>
                </td>

            </tr>
            <%}%>
        </table>       
        <a href="addCourse.jsp">Add Course</a>
    </body>
</html>
