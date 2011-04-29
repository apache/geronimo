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
<title>Submit CSR</title>
</head>
<script language="JavaScript">
function validateForm(){
    if(document.Confirmform.pkcs10req.value.length < 1) {
        alert("CSR text is empty.");
        document.Confirmform.pkcs10req.focus();
        return false;
    }
    return true;
}
</script>
<body>
<h2>Request Certificate: Submit CSR</h2>
<p>This is page lets you submit a CSR text directly without having to generate a request using the web browser.
   Upon clicking the <i>Submit CSR</i> button, your CSR will sent to the CA for further
   processing.</p>

<form name="Confirmform" action="CertificateRequestServlet" method="post">
    <table border="0">
        <tr>
            <th colspan="2" align="left"><label for="pkcs10req">CSR Text</label></th>
        </tr>
        <tr>
            <td colspan="2">
                <textarea rows="15" cols="80" name="pkcs10req" id="pkcs10req">
                ...paste pkcs10 encoded certificate request here...
                </textarea>
            </td>
        </tr>
    </table>
    <input type="submit" name="submit" value="Submit CSR" onClick="return validateForm();"/>
    <input type="reset" name="reset" value="Reset"/>
</form>
<a href="<%=request.getContextPath()%>">Cancel</a>
</body>
</html>
