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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="javax.sql.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="javax.naming.*"%>
<%@ page import="javax.annotation.Resource"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>JTA Testpage</title>
</head>
<body>
<br/>

<br/>
<%	
Context initContext = new InitialContext();
//Context envContext  = (Context)initContext.lookup("java:comp/env");
DataSource ds =null;
Connection conn = null;
Statement stmt = null;
ResultSet rs = null;

String id;
String sav;
%>

<table border="0" width="600">
<tr>
	<!--////////// BJ DB info ///////////-->
	<td width="300">
	The Account at BJ:
		<table border="1">
			<tr>
				<th>Account ID</th>
				<th>Saving</th>
			</tr>
	
<%
	ds = (javax.sql.DataSource)initContext.lookup("java:app/BJAccTx");
	//System.out.println("In desplay.jsp,sucess get java:app/BJAcc");	
	conn = ds.getConnection();
	stmt = conn.createStatement();
	rs = stmt.executeQuery("SELECT ID, SAVINGS FROM ACCOUNTS");
        
	while (rs.next()) {
		id = Long.valueOf(rs.getLong(1)).toString();
		sav = Long.valueOf(rs.getLong(2)).toString();
	
%>
			<tr>
				<td><%=id%></td>
				<td id="bj"><%=sav%></td>
			</tr>
<%
	}
	rs.close();
	conn.close();
%>
		</table>
	</td>
	
	<!--//////////  SH DB info ///////////-->
	<td width="300">
		The Account at SH:
		<table border="1">
			<tr>
				<th>Account ID</th>
				<th>Saving</th>
			</tr>

<%

	ds = (javax.sql.DataSource)initContext.lookup("java:app/SHAccTx");
    //System.out.println("In desplay.jsp,sucess get java:app/SHAcc");	
	conn = ds.getConnection();
	stmt = conn.createStatement();
	rs = stmt.executeQuery("SELECT ID, SAVINGS FROM ACCOUNTS");

	while (rs.next()) {
		id = Long.valueOf(rs.getLong(1)).toString();
		sav = Long.valueOf(rs.getLong(2)).toString();

	
%>
			<tr>
				<td><%=id%></td>
				<td id="sh"><%=sav%></td>
			</tr>

<%
	}
	rs.close();
	conn.close();

        String output = String.valueOf(request.getAttribute("output"));
        //System.out.println("in jsp,output is "+output);
        output = output.equals("null") ? " ":output;
        //System.out.println("in jsp,output is "+output);
        request.setAttribute("output",null);
        request.removeAttribute("output");

%>

		</table>
	</td>
</tr>
</table>

<br/>
<form name="accForm" method="post" action="DoTransfer">
    <b>Transfer Money from BJ to SH:</b>
<br/>$<input type="text" name="amount" size="10" />

<br/>
<b>Want to invoke transaction failing?</b>
<br/>&nbsp;<input type="text" name="flag" size="10" />
<br/>(Input 0 for Transaction SUCESS, 1 for FAIL)
<br/>
<br/>
<input type="submit" value="Submit" />
<br/>
<br/>
</form>
<b>Result:</b>
<br/>
<%=output%>


</body>
</html>