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
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Student Information</h1>
        <form action="StudentAdd">
            <table>
                 <tr>
                    <td align="right">Student ID</td>
                    <td><input type="text" name="sid"></td>
                </tr>
                <tr>
                    <td align="right">Student Name</td>
                    <td><input type="text" name="sname"></td>
                </tr>
                <tr>
                    <td align="right">Address</td>
                    <td>
                        Country:<input type="text" name="country" size="38px"><br>
                        City:<input type="text" name="city" size="38px"><br>
                        Street:<input type="text" name="street" size="38px"><br>
                    </td>
                </tr>
                <tr>
                    <td align="right">Telephone</td>
                    <td><input type="text" name="telephone"></td>
                </tr>
                <tr>
                    <td align="right">Age(Integer)</td>
                    <td><input type="text" name="age"></td>
                </tr>
            </table>
            <input type="submit" value="Save">
        </form>
    </body>
</html>
