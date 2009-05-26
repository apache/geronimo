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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg"%>
<fmt:setBundle basename="pluginportlets"/>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>RealmForm";
var <portlet:namespace/>requiredFields;
var <portlet:namespace/>passwordFields;
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="realmwizard.common.emptyText"/>');
        return false;
    }
    if(!passwordElementsConfirm(<portlet:namespace/>formName, <portlet:namespace/>passwordFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="realmwizard.common.passwordMismatch"/>');
        return false;
    }
    return true;
}
</script>

<CommonMsg:commonMsg/>
<div id="<portlet:namespace/>CommonMsgContainer"></div>

<p><fmt:message key="realmwizard.configure.title" /></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>RealmForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="process-configure" />
    <input type="hidden" name="name" value="${realm.name}" />
    <input type="hidden" name="realmType" value="${realm.realmType}" />
    <input type="hidden" name="global" value="${realm.global}" />
  <c:if test="${!fn:contains(realm.realmType, 'SQL')}">
    <input type="hidden" name="jar" value="${realm.jar}" />
  </c:if>
    <input type="hidden" name="auditPath" value="${realm.auditPath}" />
    <input type="hidden" name="lockoutCount" value="${realm.lockoutCount}" />
    <input type="hidden" name="lockoutWindow" value="${realm.lockoutWindow}" />
    <input type="hidden" name="lockoutDuration" value="${realm.lockoutDuration}" />
    <input type="hidden" name="storePassword" value="${realm.storePassword}" />
    <input type="hidden" name="credentialName" value="${realm.credentialName}" />
    <input type="hidden" name="abstractName" value="${realm.abstractName}" />
    <input type="hidden" name="module-domain-0" value="${realm.modules[0].loginDomainName}" />
    <input type="hidden" name="module-class-0" value="${realm.modules[0].className}" />
    <input type="hidden" name="module-control-0" value="${realm.modules[0].controlFlag}" />
    <input type="hidden" name="module-wrap-0" value="${realm.modules[0].wrapPrincipals}" />
    <input type="hidden" name="module-options-0" value="${realm.modules[0].optionString}" />
    <input type="hidden" name="module-domain-1" value="${realm.modules[1].loginDomainName}" />
    <input type="hidden" name="module-class-1" value="${realm.modules[1].className}" />
    <input type="hidden" name="module-control-1" value="${realm.modules[1].controlFlag}" />
    <input type="hidden" name="module-wrap-1" value="${realm.modules[1].wrapPrincipals}" />
    <input type="hidden" name="module-options-1" value="${realm.modules[1].optionString}" />
    <input type="hidden" name="module-domain-2" value="${realm.modules[2].loginDomainName}" />
    <input type="hidden" name="module-class-2" value="${realm.modules[2].className}" />
    <input type="hidden" name="module-control-2" value="${realm.modules[2].controlFlag}" />
    <input type="hidden" name="module-wrap-2" value="${realm.modules[2].wrapPrincipals}" />
    <input type="hidden" name="module-options-2" value="${realm.modules[2].optionString}" />
    <input type="hidden" name="module-domain-3" value="${realm.modules[3].loginDomainName}" />
    <input type="hidden" name="module-class-3" value="${realm.modules[3].className}" />
    <input type="hidden" name="module-control-3" value="${realm.modules[3].controlFlag}" />
    <input type="hidden" name="module-wrap-3" value="${realm.modules[3].wrapPrincipals}" />
    <input type="hidden" name="module-options-3" value="${realm.modules[3].optionString}" />
    <input type="hidden" name="module-domain-4" value="${realm.modules[4].loginDomainName}" />
    <input type="hidden" name="module-class-4" value="${realm.modules[4].className}" />
    <input type="hidden" name="module-control-4" value="${realm.modules[4].controlFlag}" />
    <input type="hidden" name="module-wrap-4" value="${realm.modules[4].wrapPrincipals}" />
    <input type="hidden" name="module-options-4" value="${realm.modules[4].optionString}" />
    <table border="0">
<c:choose>
  <c:when test="${fn:contains(realm.realmType, 'SQL')}">
<jsp:include page="_sql.jsp" />
  </c:when>
  <c:otherwise>
    <script language="JavaScript">
      <portlet:namespace/>requiredFields = new Array();
      <portlet:namespace/>passwordFields = new Array();
    </script>
    <c:forEach var="option" items="${realm.optionNames}">
      <tr>
        <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>option-${option}"><fmt:message key="${optionMap[option].displayName}"/></label>:</div></th>
        <td><input name="option-${option}" id="<portlet:namespace/>option-${option}"
                   type="<c:choose><c:when test="${optionMap[option].password}">password</c:when><c:otherwise>text</c:otherwise></c:choose>"
                   size="${optionMap[option].length}" value="${realm.options[option]}"></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="${optionMap[option].description}"/></td>
      </tr>
    <c:if test="${optionMap[option].password}">
      <tr>
        <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>confirm-option-${option}"><fmt:message key="consolebase.common.confirmPassword"/></label>:</div></th>
        <td><input name="confirm-option-${option}" id="<portlet:namespace/>confirm-option-${option}" type="password"
                   size="${optionMap[option].length}" value="${realm.options[option]}"></td>
      </tr>
      <script language="JavaScript">
          <portlet:namespace/>passwordFields = <portlet:namespace/>passwordFields.concat(new Array('option-${option}'))
      </script>
    </c:if>
      <c:if test="${!optionMap[option].blankAllowed}">
        <script language="JavaScript">
          <portlet:namespace/>requiredFields = <portlet:namespace/>requiredFields.concat(new Array('option-${option}'))
        </script>
      </c:if>
    </c:forEach>
  </c:otherwise>
</c:choose>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td><input type="submit" value='<fmt:message key="consolebase.common.next"/>' onClick="return <portlet:namespace/>validateForm()"/></td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
