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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="pluginportlets"/>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>RealmForm";
var <portlet:namespace/>requiredFields = new Array('username');
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName, <portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="realmwizard.common.emptyText"/>');
        return false;
    }
    return true;
}
</script>

<div id="<portlet:namespace/>CommonMsgContainer"></div>

<p><fmt:message key="realmwizard.testLogin.title" /></p>

<p><fmt:message key="realmwizard.testLogin.summary" /></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>RealmForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="process-test" />
    <input type="hidden" name="name" value="${realm.name}" />
    <input type="hidden" name="realmType" value="${realm.realmType}" />
    <input type="hidden" name="jar" value="${realm.jar}" />
    <input type="hidden" name="global" value="${realm.global}" />
  <c:forEach var="option" items="${realm.options}">
    <input type="hidden" name="option-${option.key}" value="${option.value}" />
  </c:forEach>
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
    <!-- ENTRY FIELD: Username -->
      <tr>
        <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>username"><fmt:message key="consolebase.common.userName"/></label>:</div></th>
        <td><input name="username" id="<portlet:namespace/>username" type="text" size="20" /></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard.testLogin.usernameExp" /></td>
      </tr>
    <!-- ENTRY FIELD: Password -->
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>password"><fmt:message key="consolebase.common.password"/></label>:</div></th>
        <td><input name="password" id="<portlet:namespace/>password" type="password" size="20" /></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard.testLogin.passwordExp" /></td>
      </tr>
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
