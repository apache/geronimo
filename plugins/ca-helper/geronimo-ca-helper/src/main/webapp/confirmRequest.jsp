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

<%-- $Rev$ $Date$ --%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
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
<div id="Non-IE-Content" style="display:none">
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
<%--Non-IE Content ends here --%>
</div>

<div id="IE-Content" style="display:none">
<p> This is step 2 of 2 in requesting your certificate.  Please review your name details.
    Upon clicking the <i>Submit Certificate Request</i> button, your certificate request will be generated
    and sent to the CA for further processing.</p>

<%-- ActiveX Control to generate PKCS10 request in Internet Explorer --%>
<object classid="clsid:127698e4-e730-4e5c-a2b1-21490a70c8a1"
    codebase="xenroll.dll"
    id="newCertHelper">
</object>

<%-- VBScript to generate a PKCS10 request in Internet Explorer --%>
<SCRIPT language="VBScript">
<!--
Sub GenerateReq
    ' Distinguished name variable.
    Dim strDN

    ' Request Variable.
    Dim strReq

    ' Request Disposition.
    Dim nDisp

    ' Enable error handling.
    On Error Resume Next

    ' Constants For CertRequest object.
    const CR_IN_BASE64 = &H1
    const CR_IN_PKCS10 = &H100

    ' Build the DN.
    strDN =  "CN="&document.Confirmform.reqCN.value _
         &",OU="&document.Confirmform.reqOU.value _
         &",O="&document.Confirmform.reqO.value _
         &",L="&document.Confirmform.reqL.value _
         &",ST="&document.Confirmform.reqST.value _
         &",C="&document.Confirmform.reqC.value _
         '&",CC=ask"  
    ' Attempt to use the control, in this case, to create a PKCS #10.
    strReq = newCertHelper.CreatePKCS10(strDN, " ")
    ' If above line failed, Err.Number will not be 0.
    if ( Err.Number <> 0 ) then
        MsgBox("Error in call to createPKCS10 " & Err.Number)
        err.clear
        return
    else
        document.Confirmform.pkcs10req.value = strReq
    end if
    document.Confirmform.submit()
End Sub
-->
</SCRIPT>

<form name="Confirmform" action="CertificateRequestServlet" method="post">
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
                <input type="hidden" name="pkcs10req"> <%-- This hidden field stores the pkcs10 request --%>
            </td>
        </tr>
        <tr>
            <th align="right">Challenge Phrase:</th>
            <td>
                Not Supported for IE
            </td>
        </tr>
    </table>
    <input type="button" value="Submit Certificate Request" onClick="GenerateReq()"/>
</form>
<%-- IE Content ends here --%>
</div>

<!-- The following is used to detect if the browser supports KEYGEN tag and disply only the relevant form -->
<div style="display:none"><form name='keygentest' method="POST"><keygen name="test"/></form></div>
<SCRIPT language="JavaScript">
if(document.keygentest.elements.length == 0)
   document.getElementById('IE-Content').style.display = 'block'
else
   document.getElementById('Non-IE-Content').style.display = 'block'
</SCRIPT>

<a href="<%=request.getContextPath()%>">Cancel</a>
</body>
</html>
