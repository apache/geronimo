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

<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<jsp:include page="_header.jsp" />

<p><fmt:message key="keystore.unlockKey.title"/></p>

<form name="<portlet:namespace/>KeystoreForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="keystore" value="${keystore}" />
    <input type="hidden" name="password" value="${password}" />
    <input type="hidden" name="mode" value="unlockKey-after" />
    <b><label for="<portlet:namespace/>keyAlias"><fmt:message key="keystore.unlockKey.unlockPrivateKey"/></label>:</b>
    <select name="keyAlias" id="<portlet:namespace/>keyAlias">
        <c:forEach var="alias" items="${keys}">
            <option>${alias}</option>
        </c:forEach>
    </select>
    <label for="<portlet:namespace/>keyPassword"><fmt:message key="consolebase.common.password"/></label>:
    <input type="password" name="keyPassword" id="<portlet:namespace/>keyPassword" size="20" maxlength="200" />
    <br />

    <input type="submit" value='<fmt:message key="keystore.unlockKey.unlockPrivateKey"/>' />
</form>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list-before" />
            </portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
