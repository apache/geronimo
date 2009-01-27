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
<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<p><fmt:message key="keystore.confirmCertificate.title"/></p>

<form name="<portlet:namespace/>KeystoreForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="id" value="${id}" />
    <input type="hidden" name="alias" value="${alias}" />
    <input type="hidden" name="certificate" value="${certificate}" />
    <input type="hidden" name="mode" value="confirmCertificate-after" />

    <table border="0">
        <tr>
            <th class="DarkBackground" colspan="2" align="left"><fmt:message key="keystore.confirmCertificate.certificateDetails"/></th>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="keystore.confirmCertificate.fingerprint"/>:</th>
            <td class="LightBackground">${fingerprint}</td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right"><fmt:message key="keystore.common.subject"/>:</th>
            <td class="MediumBackground">${subject}</td>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="keystore.common.issuer"/>:</th>
            <td class="LightBackground">${issuer}</td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right"><fmt:message key="keystore.common.validity"/>:</th>
            <td class="MediumBackground">${validStart} to ${validEnd}</td>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="keystore.common.serialNumber"/>:</th>
            <td class="LightBackground">${serial}</td>
        </tr>
    </table>

    <input type="submit" value='<fmt:message key="keystore.confirmCertificate.importCertificate"/>' />
</form>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="viewKeystore-before" />
              <portlet:param name="id" value="${id}" />
            </portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
