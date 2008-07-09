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
<fmt:message key="keystore.common.keystore"/>: ${id}<br/>
<fmt:message key="keystore.common.alias"/>: ${alias}<br/>

<table>
<th><label for="<portlet:namespace/>csr"><fmt:message key="keystore.generateCSR.PKCS10CertificatioRequest"/></label></th>
<tr>
<td>
<form action=>
<textarea rows="15" cols="80" id="<portlet:namespace/>csr" readonly>
${csr}
</textarea>
</td>
</tr>
<tr>
<td><a href="<portlet:actionURL portletMode="view">
<portlet:param name="mode" value="generateCSR-after" />
<portlet:param name="id" value="${id}" />
<portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
<fmt:message key="consolebase.common.back"/></a></td>
</tr>
</table>
