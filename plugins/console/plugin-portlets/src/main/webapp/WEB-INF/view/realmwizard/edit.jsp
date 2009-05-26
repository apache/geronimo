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
<fmt:setBundle basename="pluginportlets"/>
<portlet:defineObjects/>
<fmt:message key="realmwizard.edit.summary" />


<c:if test="${empty realm.abstractName}">
<p><fmt:message key="realmwizard.edit.ifLeaveBlank" /></p>
</c:if>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>RealmForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="save" />
    <input type="hidden" name="name" value="${realm.name}" />
    <input type="hidden" name="realmType" value="${realm.realmType}" />
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

    <table border="0">
    <!-- ENTRY FIELD: NAME -->
      <tr>
        <th style="min-width: 140px"><div align="right"><fmt:message key="realmwizard.common.realmName" />:</div></th>
        <td>
      <c:choose> <%-- Can't change the realm name after deployment because it's wired into all the abstractNames --%>
        <c:when test="${empty realm.abstractName}">
          <input name="name" title='<fmt:message key="realmwizard.common.realmName" />' type="text" size="30" value="${realm.name}">
        </c:when>
        <c:otherwise>
          <input name="name" type="hidden" value="${realm.name}" />
          <b><c:out value="${realm.name}" /></b>
        </c:otherwise>
      </c:choose>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard.edit.realmNameExp" /></td>
      </tr>
    <!-- ENTRY FIELD: LoginModule JAR -->
    <c:choose>
      <c:when test="${mode eq 'custom'}">
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>jar"><fmt:message key="realmwizard.edit.loginModuleJAR" /></label>:</div></th>
        <td>
          <select name="jar" id="<portlet:namespace/>jar">
                  <option />
              <c:forEach var="availableJar" items="${jars}">
                  <option <c:if test="${availableJar == realm.jar}">selected</c:if>>
                      ${availableJar}
                  </option>
              </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard.edit.loginModuleJARExp" /></td>
      </tr>
      </c:when>
      <c:otherwise>
        <input type="hidden" name="jar" value="${realm.jar}" />
      </c:otherwise>
    </c:choose>
    <!-- HEADER -->
    <c:forEach var="module" items="${realm.modules}" varStatus="status" >
      <tr>
        <th colspan="2"><fmt:message key="realmwizard.common.loginModule" />${status.index+1}</th>
      </tr>
      <tr>
        <th><div align="right"><fmt:message key="realmwizard.common.loginDomainName" />:</div></th>
        <td>
      <c:choose> <%-- Can't change the login domain name after deployment because it's how we know which GBean is which --%>
        <c:when test="${empty realm.abstractName}">
          <input name="module-domain-${status.index}" title='<fmt:message key="realmwizard.common.loginDomainName" />' type="text" size="20" value="${module.loginDomainName}" />
        </c:when>
        <c:otherwise>
          <input name="module-domain-${status.index}" type="hidden" value="${module.loginDomainName}" />
          <b><c:out value="${module.loginDomainName}" /></b>
        </c:otherwise>
      </c:choose>
        </td>

        <td></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard.edit.loginDomainExp" /></td>
      </tr>
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>module-class-${status.index}"><fmt:message key="realmwizard.common.loginModuleClass" /></label>:</div></th>
        <td><input name="module-class-${status.index}" id="<portlet:namespace/>module-class-${status.index}" type="text" size="60" value="${module.className}" /></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard.edit.loginModuleClassExp" /></td>
      </tr>
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>module-control-${status.index}"><fmt:message key="realmwizard.common.controlFlag" /></label>:</div></th>
        <td>
          <select name="module-control-${status.index}" id="<portlet:namespace/>module-control-${status.index}">
            <option value="OPTIONAL"<c:if test="${module.controlFlag eq 'OPTIONAL'}"> selected</c:if>>Optional</option>
            <option value="REQUIRED"<c:if test="${module.controlFlag eq 'REQUIRED'}"> selected</c:if>>Required</option>
            <option value="REQUISITE"<c:if test="${module.controlFlag eq 'REQUISITE'}"> selected</c:if>>Requisite</option>
            <option value="SUFFICIENT"<c:if test="${module.controlFlag eq 'SUFFICIENT'}"> selected</c:if>>Sufficient</option>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard.edit.controlFlagExp" />
          <a href="http://java.sun.com/j2se/1.4.2/docs/api/javax/security/auth/login/Configuration.html">javax.security.auth.login.Configuration</a>.</td>
      </tr>
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>module-wrap-${status.index}"><fmt:message key="realmwizard.common.supportAdvancedMapping" /></label>:</div></th>
        <td>
          <select name="module-wrap-${status.index}" id="<portlet:namespace/>module-wrap-${status.index}">
            <option value="true"<c:if test="${module.wrapPrincipals}"> selected</c:if>>Yes</option>
            <option value="false"<c:if test="${!module.wrapPrincipals}"> selected</c:if>>No</option>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard.edit.supportAdvancedMappingExp" /></td>
      </tr>
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>module-options-${status.index}"><fmt:message key="realmwizard.common.configurationOptions" /></label>:</div></th>
        <td><textarea name="module-options-${status.index}" id="<portlet:namespace/>module-options-${status.index}" rows="5" cols="60">${module.optionString}</textarea></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard.edit.configurationOptionsExp" /></td>
      </tr>
    </c:forEach>

    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
          <input type="button" value="<c:choose><c:when test="${empty realm.abstractName}"><fmt:message key='realmwizard.common.deploy' /></c:when><c:otherwise><fmt:message key='consolebase.common.save'/></c:otherwise></c:choose>"
                 onclick="document.<portlet:namespace/>RealmForm.mode.value='save';document.<portlet:namespace/>RealmForm.submit();return false;" />
          <input type="button" value='<fmt:message key="realmwizard.common.showPlan" />' onclick="document.<portlet:namespace/>RealmForm.mode.value='plan';document.<portlet:namespace/>RealmForm.submit();return false;" />
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->



<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
