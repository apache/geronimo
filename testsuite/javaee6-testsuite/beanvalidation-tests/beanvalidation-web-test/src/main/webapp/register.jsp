<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Basic Information</h1>
        <div  style="float: left; margin-right: 40px">
        <form action="submitInfo">
            <table border="1">
                <tr>
                    <td align="right">Name:</td>
                    <td><input name="name" type="text" value="<%String name=request.getParameter("name");if(name!=null){%><%=name%><%}%>"/></td>
                    
                </tr>
                <tr>
                    <td align="right">Age:</td>
                    <td><input name="age" type="text"  value="<%String age=request.getParameter("age");if(age!=null){%><%=age%><%}%>"/></td>
                </tr>
                <tr>
                    <td align="right">Mail:</td>
                    <td><input name="mail" type="text" value="<%String mail=request.getParameter("mail");if(mail!=null){%><%=mail%><%}%>"/></td>
                </tr>
                <tr>
                    <td align="right">Birthday:</td>
                    <td><input name="birthday" type="text" value="<%String birthday=request.getParameter("birthday");if(birthday!=null){%><%=birthday%><%}%>"/></td>
                </tr>
                <tr>
                    <td align="right">Address:</td>
                    <td>Country:<input name="country" type="text" value="<%String country=request.getParameter("country");if(country!=null){%><%=country%><%}%>"/>
                    <br>State:&nbsp;&nbsp;&nbsp;&nbsp;<input name="state" type="text" value="<%String state=request.getParameter("state");if(state!=null){%><%=state%><%}%>"/>
                    <br>City:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input name="city" type="text" value="<%String city=request.getParameter("city");if(city!=null){%><%=city%><%}%>"/></td>
                </tr>
                <tr>
                    <td align="right">Salary:</td>
                    <td><input name="salary" type="text" value="<%String salary=request.getParameter("salary");if(salary!=null){%><%=salary%><%}%>"/></td>
                </tr>
                <tr>
                    <td><input name="submit" type="submit" value="Register"/></td>
                    <td><input name="submitAsVIP" type="submit" value="Register as VIP"/></td>
                </tr>
            </table>
        </form>
                </div>
                <div  id="hints" style="padding: 5px;margin: 5px;border: 2px">
                    <h2> Registration Notice!</h2><br>
                    <ol>
                        <li><b>Name</b> Attribute can not be null.and the length of it should between<b> 1</b> and <b>5</b>.</li>
                        <li>Your <b>age</b> should between <b>1</b> and <b>100</b>.</li>
                        <li>Valid <b>mail</b> format is like <b>somebody@whatever.com</b>.</li>
                        <li>Your <b>birthday</b> format is like <b>2012-12-30</b>.</li>
                        <li><b>Address</b> should not be null.</li>
                        <li>To <b>registe</b>r as a member,your salary should be less than <b>1000</b>.<br>To register as a <b>VIP</b>,your salary should be less than<b> 10000</b>.</li>
                    </ol>
                </div>
        <hr>
        <div id="errormessages" style="padding: 5px;margin-top: 5px">
        <%Object message=request.getSession().getAttribute("message");
        if(null!=message){%><%=message.toString()%><%}%>
        </div>
    </body>
</html>
