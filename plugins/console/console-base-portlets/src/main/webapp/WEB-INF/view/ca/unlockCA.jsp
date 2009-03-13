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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>UnlockCAForm";
var <portlet:namespace/>requiredFields = new Array("password");

function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="ca.common.emptyText"/>');
        return false;
    }
    return true;
}
</script>

<jsp:include page="_header.jsp" />

<div id="<portlet:namespace/>CommonMsgContainer"></div><br>

<b><fmt:message key="ca.common.unlockCertAuthority"/></b>

<p><fmt:message key="ca.unlockCA.summary"/></p>

<form name="<portlet:namespace/>UnlockCAForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="unlockCA-after" />
    <b><label for="<portlet:namespace/>password"><fmt:message key="ca.unlockCA.enterCAPrivateKeyPassword"/></label>:</b>
    <input type="password" name="password" id="<portlet:namespace/>password" size="20" maxlength="200" />
    <br /><br />

    <input type="submit" value='<fmt:message key="ca.common.unlockCertAuthority"/>' onClick="return <portlet:namespace/>validateForm();"/>
    <input type="reset" name="reset" value='<fmt:message key="consolebase.common.reset"/>'>
</form>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
