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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>UnlockCAForm";
var <portlet:namespace/>requiredFields = new Array("password");

function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields))
        return false;
    return true;
}
</script>
<b>Unlock Certification Authority</b>

<p> This screen lets you unlock the CA by providing the password used to protect
the CA's private key.  Once unlocked, the CA functions will be available.</p>

<jsp:include page="_header.jsp" />

<form name="<portlet:namespace/>UnlockCAForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="unlockCA-after" />
    <b>Enter the CA's private key password:</b>
    <input type="password" name="password" size="20" maxlength="200" />
    <br />

    <input type="submit" value="Unlock Certification Authority" onClick="return <portlet:namespace/>validateForm();"/>
    <input type="reset" name="reset" value="Reset">
</form>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>">Cancel</a></p>
