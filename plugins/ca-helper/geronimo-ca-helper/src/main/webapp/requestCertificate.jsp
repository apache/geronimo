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
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Request Certificate - Enter Name details</title>
<script language="JavaScript">
function textElementsNotEmpty(formName, elementNameArray){
    var obj;
    for(i in elementNameArray){
        var elem = elementNameArray[i];
        obj = eval("document.forms['" + formName + "'].elements['" + elem + "']"); 
        if(isEmptyString(obj.value)){
            alert(elem + " must not be empty.");
            obj.focus(); 
            return false;             
        }
    }
    return true;
}
function isEmptyString(value){
    return value.length < 1;
}

var formName = "CertReqForm";
var requiredFields = new Array("reqCN", "reqOU", "reqO", "reqL", "reqST", "reqC");

function validateForm(){
    if(!textElementsNotEmpty(formName, requiredFields))
        return false;
    ch = eval("document." + formName + ".challenge"); 
    cf = eval("document." + formName + ".confirm");
    if(ch.value != cf.value) {
        alert("Challenge phrase and confirm challenge do not match.");
        ch.focus();
        return false;
    }
    return true;
}

</script>
</head>
<body>
<h2>Request Certificate: Enter Name Details</h2>
<p>This is  step 1 of 2 in requesting your certificate.  Please enter your identity details below.
The optional <i>Challenge Phrase</i> can be used later on if you ever need to revoke your certificate.
The next screen will let you review these details before submitting the certificate request.
If you do not want the browser to generate a CSR and would like to enter the CSR text directly,
use the <a href="submitCSR.jsp">CSR submission page</a> instead.</p>

<form action="confirmRequest.jsp" method="post" name="CertReqForm">
    <table border="0">
        <tr>
            <th colspan="2" align="left">Identity Details</th>
        </tr>
        <tr>
            <th align="right"><label for="reqCN">Common Name (CN)</label>:</th>
            <td>
                <input type="text" name="reqCN" id="reqCN" size="20" maxlength="200"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="reqOU">Division/Business Unit (OU)</label>:</th>
            <td>
                <input type="text" name="reqOU" id="reqOU" size="20" maxlength="200"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="reqO">Company/Organization (O)</label>:</th>
            <td>
                <input type="text" name="reqO" id="reqO" size="20" maxlength="200"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="reqL">City/Locality (L)</label>:</th>
            <td>
                <input type="text" name="reqL" id="reqL" size="20" maxlength="200"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="reqST">State/Province (ST)</label>:</th>
            <td>
                <input type="text" name="reqST" id="reqST" size="20" maxlength="200"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="reqC">Country Code (2 char) (C)</label>:</th>
            <td>
                <input type="text" name="reqC" id="reqC" size="3" maxlength="2"/>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <th align="right"><label for="challenge">Challenge Phrase</label></th>
            <td>
                <input type="password" name="challenge" id="challenge" size="20" maxlength="200"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="confirm">Confirm Challenge</label></th>
            <td>
                <input type="password" name="confirm" id="confirm" size="20" maxlength="200"/>
            </td>
        </tr>
    </table>
    <input type="submit" value="Review Name Details" onClick="return validateForm();"/>
    <input type="reset" name="reset" value="Reset"/>
</form>
<a href="<%=request.getContextPath()%>">Cancel</a>
</body>
</html>
