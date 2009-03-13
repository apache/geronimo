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
var <portlet:namespace/>formName = "<portlet:namespace/>ProcessCSRForm";
var <portlet:namespace/>requiredFields = new Array("pkcs10certreq");

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

<fmt:message key="ca.processCSR.title"/>

<p>
<fmt:message key="ca.processCSR.summary"/>
</p>

<form name="<portlet:namespace/>ProcessCSRForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="processCSR-after" />
    <table border="0">
        <tr>
            <th colspan="2" align="left"><label for="<portlet:namespace/>pkcs10certreq"><fmt:message key="ca.common.CSRText"/></label></th>
        </tr>
        <tr>
            <td colspan="2">
                <textarea rows="20" cols="80" name="pkcs10certreq" id="<portlet:namespace/>pkcs10certreq">
                <fmt:message key="ca.processCSR.pastePkcs10here"/>                
                </textarea>
            </td>
        </tr>
    </table>
    <input type="submit" value='<fmt:message key="ca.common.processCSR"/>' onClick="return <portlet:namespace/>validateForm();"/>
    <input type="reset" name="reset" value='<fmt:message key="consolebase.common.reset"/>'>
</form>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before"/>
            </portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
