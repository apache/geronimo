<%--
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
--%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Request Certificate - Confirm Request</title>
</head>
<%
    String reqCN = request.getParameter("reqCN");
    String reqOU = request.getParameter("reqOU");
    String reqO = request.getParameter("reqO");
    String reqL = request.getParameter("reqL");
    String reqST = request.getParameter("reqST");
    String reqC = request.getParameter("reqC");
    String challenge = request.getParameter("challenge");
%>

<body>
<h2>Request Certificate: Confirm and Submit Request</h2>
<p>This is step 2 of 2 in requesting your certificate.  Please review your name details and select the keysize for
your keypair.  Upon clicking the <i>Submit Certificate Request</i> button, your certificate request will be generated
and sent to the CA for further processing.</p>

<form action="CertificateRequestServlet" method="post">
    <table border="0">
        <tr>
            <th align="right">Common Name (CN):</th>
            <td>
                <input type="hidden" name="reqCN" value="<%=reqCN%>"/> <%=reqCN%>
            </td>
        </tr>
        <tr>
            <th align="right">Division/Business Unit (OU):</th>
            <td>
                <input type="hidden" name="reqOU" value="<%=reqOU%>"/> <%=reqOU%>
            </td>
        </tr>
        <tr>
            <th align="right">Company/Organization (O):</th>
            <td>
                <input type="hidden" name="reqO" value="<%=reqO%>"/> <%=reqO%>
            </td>
        </tr>
        <tr>
            <th align="right">City/Locality (L):</th>
            <td>
                <input type="hidden" name="reqL" value="<%=reqL%>"/> <%=reqL%>
            </td>
        </tr>
        <tr>
            <th align="right">State/Province (ST):</th>
            <td>
                <input type="hidden" name="reqST" value="<%=reqST%>"/> <%=reqST%>
            </td>
        </tr>
        <tr>
            <th align="right">Country Code (2 char) (C):</th>
            <td>
                <input type="hidden" name="reqC" value="<%=reqC%>"/> <%=reqC%>
            </td>
        </tr>
        <tr>
            <th align="right">Challenge Phrase:</th>
            <td>
                ********
            </td>
        </tr>
        <tr>
            <th align="right">Key Size:</th>
            <td>
                <keygen name="spkac" challenge="<%=challenge%>"/>
            </td>
        </tr>
    </table>
    <input type="submit" value="Submit Certificate Request"/>
    <input type="reset" name="reset" value="Reset"/>
</form>
<a href="<%=request.getContextPath()%>">Cancel</a>
</body>
</html>
