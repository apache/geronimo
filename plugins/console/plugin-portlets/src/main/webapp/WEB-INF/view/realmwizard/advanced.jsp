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
function <portlet:namespace/>validateForm(){
    with(document.forms[<portlet:namespace/>formName]) {
        if(enableAuditing.checked) {
            var requiredFields = new Array('auditPath');
            if (!textElementsNotEmpty(<portlet:namespace/>formName, requiredFields)) {
                addErrorMessage("<portlet:namespace/>", '<fmt:message key="realmwizard.common.emptyText"/>');
                return false;
            }
        }
        if(enableLockout.checked) {            
            var integerFields = new Array('lockoutCount', 'lockoutWindow', 'lockoutDuration');
            for(i in integerFields) {
                if (!checkIntegral(<portlet:namespace/>formName, integerFields[i])) {
                    addErrorMessage("<portlet:namespace/>", '<fmt:message key="realmwizard.common.integer"/>');
                    return false;
                }
            }
        }
        if(namedUPC.checked) {
            var requiredFields = new Array('credentialName');
            if (!textElementsNotEmpty(<portlet:namespace/>formName, requiredFields)) {
                addErrorMessage("<portlet:namespace/>", '<fmt:message key="realmwizard.common.emptyText"/>');
                return false;
            }
        }        
    }
    
    return true;
}
</script>

<div id="<portlet:namespace/>CommonMsgContainer"></div>

<p><fmt:message key="realmwizard.advanced.title" /></p>

<c:if test="${!(empty AdvancedError)}"><p><font color="red"><b>Error: ${AdvancedError}</b></font></p></c:if>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>RealmForm" action="<portlet:actionURL/>" onSubmit="return <portlet:namespace/>validateForm()" method="POST">
    <input type="hidden" name="mode" value="process-advanced" />
    <input type="hidden" name="test" value="true" />
    <input type="hidden" name="name" value="${realm.name}" />
    <input type="hidden" name="realmType" value="${realm.realmType}" />
    <input type="hidden" name="jar" value="${realm.jar}" />
  <c:forEach var="option" items="${realm.options}">
    <input type="hidden" name="option-${option.key}" value="${option.value}" />
  </c:forEach>
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
    <!-- ENTRY FIELD: Audit Log -->
      <tr>
        <th valign="top" style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>auditCheckbox"><fmt:message key="realmwizard.common.enableAuditing" /></label>:</div></th>
        <td valign="top">
          <input type="checkbox" id="<portlet:namespace/>auditCheckbox" name="enableAuditing"<c:if test="${!(empty realm.auditPath)}"> checked="checked"</c:if>
          onclick="document.getElementById('<portlet:namespace/>auditDiv').style.display=this.checked ? 'block' : 'none';document.getElementById('<portlet:namespace/>auditPath').value='';"/>
          <div id="<portlet:namespace/>auditDiv" style="display: <c:choose><c:when test="${empty realm.auditPath}">none</c:when><c:otherwise>block</c:otherwise></c:choose>;">
          <label for="<portlet:namespace/>auditPath"><fmt:message key="realmwizard.common.logFile" /></label>: <input type="text" id="<portlet:namespace/>auditPath" name="auditPath" size="30" value="${realm.auditPath}" />
          </div>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard.advanced.AuditEnabledExp" /></td>
      </tr>
    <!-- ENTRY FIELDS: Lockout -->
      <tr>
        <th valign="top"><div align="right"><label for="<portlet:namespace/>lockoutCheckbox"><fmt:message key="realmwizard.common.enableLockout" /></label>:</div></th>
        <td valign="top">
          <input type="checkbox" id="<portlet:namespace/>lockoutCheckbox" name="enableLockout"<c:if test="${realm.lockoutEnabled}"> checked="checked"</c:if>
                 onclick="document.getElementById('<portlet:namespace/>lockoutDiv').style.display=this.checked ? 'block' : 'none';document.getElementById('<portlet:namespace/>lockoutCount').value='';document.getElementById('<portlet:namespace/>lockoutWindow').value='';document.getElementById('<portlet:namespace/>lockoutDuration').value='';"/>
          <div id="<portlet:namespace/>lockoutDiv" style="display: <c:choose><c:when test="${realm.lockoutEnabled}">block</c:when><c:otherwise>none</c:otherwise></c:choose>;">
          <label for="<portlet:namespace/>lockoutCount"><fmt:message key="realmwizard.advanced.lockUserAfter" /> </label><input type="text" id="<portlet:namespace/>lockoutCount" name="lockoutCount" size="2" maxlength="3" value="${realm.lockoutCount}" />
          <label for="<portlet:namespace/>lockoutWindow"><fmt:message key="realmwizard.advanced.failuresWithin" /> </label><input type="text" id="<portlet:namespace/>lockoutWindow" name="lockoutWindow" size="4" maxlength="5" value="${realm.lockoutWindow}" /> <fmt:message key="realmwizard.advanced.failuresWithinSeconds" /><br />
          <label for="<portlet:namespace/>lockoutDuration"><fmt:message key="realmwizard.advanced.keepAccountLockedFor" /> </label><input type="text" id="<portlet:namespace/>lockoutDuration" name="lockoutDuration" size="5" maxlength="5" value="${realm.lockoutDuration}" /> <fmt:message key="realmwizard.advanced.keepAccountLockedForSeconds" />.
          </div>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard.advanced.lockoutEnabledExp" /></td>
      </tr>
    <!-- ENTRY FIELD: Store Password -->
      <tr>
        <th valign="top"><div align="right"><label for="<portlet:namespace/>storePassword"><fmt:message key="realmwizard.common.storePassword" /></label>:</div></th>
        <td valign="top">
          <input type="checkbox" name="storePassword" id="<portlet:namespace/>storePassword"<c:if test="${realm.storePassword}"> checked="checked"</c:if>/>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard.advanced.storePasswordEnabledExp" /></td>
      </tr>
    <!-- ENTRY FIELD: Named UPC -->
      <tr>
        <th valign="top"><div align="right"><label for="<portlet:namespace/>namedUPCCheckbox"><fmt:message key="realmwizard.advanced.namedCredential" /></label>:</div></th>
        <td valign="top">
          <input type="checkbox" id="<portlet:namespace/>namedUPCCheckbox" name="namedUPC"<c:if test="${!(empty realm.credentialName)}"> checked="checked"</c:if>
          onclick="document.getElementById('<portlet:namespace/>namedUPCDiv').style.display=this.checked ? 'block' : 'none';document.getElementById('<portlet:namespace/>credentialName').value='';"/>
          <div id="<portlet:namespace/>namedUPCDiv" style="display: <c:choose><c:when test="${empty realm.credentialName}">none</c:when><c:otherwise>block</c:otherwise></c:choose>;">
          <label for="<portlet:namespace/>credentialName"><fmt:message key="realmwizard.advanced.credentialName" /></label>: <input type="text" id="<portlet:namespace/>credentialName" name="credentialName" size="30" value="${realm.credentialName}" />
          </div>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard.advanced.credentialNameExp" /></td>
      </tr>
        <tr>
            <th valign="top"><div align="right"><fmt:message key="realmwizard.advanced.global" /></div></th>
            <td>
                <input type="checkbox" name="global" id="<portlet:namespace/>global" <c:if test="${realm.global}">checked="true"</c:if> />
            </td>
        </tr>
        <tr>
          <td></td>
          <td><fmt:message key="realmwizard.advanced.global.description" /></td>
        </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
      <c:choose>
        <c:when test="${realm.testable}">
          <input type="submit" value='<fmt:message key="realmwizard.common.testLogin" />' />
          <input type="button" value='<fmt:message key="realmwizard.common.skipTestAndDeploy" />'  onclick="document.<portlet:namespace/>RealmForm.test.value='false';document.<portlet:namespace/>RealmForm.submit();return false;" />
          <input type="button" value='<fmt:message key="realmwizard.common.skipTestAndShowPlan" />'  onclick="document.<portlet:namespace/>RealmForm.mode.value='plan';document.<portlet:namespace/>RealmForm.submit();return false;" />
        </c:when>
        <c:otherwise>
          <input type="button" value='<fmt:message key="realmwizard.common.deployRealm" />' onclick="document.<portlet:namespace/>RealmForm.test.value='false';document.<portlet:namespace/>RealmForm.submit();return false;" />
          <input type="button" value='<fmt:message key="realmwizard.common.showPlan" />' onclick="document.<portlet:namespace/>RealmForm.mode.value='plan';document.<portlet:namespace/>RealmForm.submit();return false;" />
        </c:otherwise>
      </c:choose>
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
