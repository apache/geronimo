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

<jsp:include page="_header.jsp" /><br>

<b><fmt:message key="ca.confirmCertReq.confirmCertRequest"/></b>

<p><fmt:message key="ca.confirmCertReq.summary"/></p>

<form name="<portlet:namespace/>confirmCertReqForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="confirmCertReq-after"/>
    <input type="hidden" name="requestId" value="${requestId}"/>
    <table border="0">
        <tr>
            <th class="DarkBackground" colspan="2" align="left"><fmt:message key="ca.common.certRequestorDetails"/></th>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="ca.common.subject"/>:</th>
            <td class="LightBackground">
                ${subject}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right"><fmt:message key="ca.common.publicKey"/>:</th>
            <td class="MediumBackground">
                <pre>${publickey}</pre>
            </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
    </table>
    <input type="submit" name="approve" value='<fmt:message key="ca.confirmCertReq.approveCSR"/>'/>
    <input type="submit" name="reject" value='<fmt:message key="ca.confirmCertReq.rejectCSR"/>'>
</form>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before"/>
            </portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
