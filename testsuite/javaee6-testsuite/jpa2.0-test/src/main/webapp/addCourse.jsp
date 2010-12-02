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
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Add Course</title>
    </head>
    <body>
        <h1>Course Information</h1>
        <form action="CourseAdd">
            <table border="1px">
            <tr><td align="right">Course ID</td><td><input type="text" name="cid"></td></tr>
            <tr><td align="right">Course Name</td><td><input type="text" name="cname"></td></tr>
            <tr><td align="right">Classroom</td><td><input type="text" name="classroom"></td></tr>
            <tr><td align="right">Teacher</td><td><input type="text" name="teacher"></td></tr>
            <tr><td align="right">Assist Teacher</td><td><input type="text" name="assistTeacher"></td></tr>
        </table>
            <input type="submit" value="Save">
        </form>
    </body>
</html>
