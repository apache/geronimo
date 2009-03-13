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
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg" %>
<fmt:setBundle basename="systemdatabase"/>

<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>DatabaseForm";
var <portlet:namespace/>requiredFields = new Array("driverClass", "jars");
var <portlet:namespace/>passwordFields = new Array("password");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="dbwizard.common.emptyText"/>');
        return false;
    }
    if(!passwordElementsConfirm(<portlet:namespace/>formName, <portlet:namespace/>passwordFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="dbwizard.common.passwordMismatch"/>');
        return false;
    }
    return true;
}
</script>

<CommonMsg:commonMsg/><div id="<portlet:namespace/>CommonMsgContainer"></div>

<p><fmt:message key="dbwizard.basicParams.title"/></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>DatabaseForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="process-params" />
    <input type="hidden" name="name" value="${pool.name}" />
    <input type="hidden" name="dbtype" value="${pool.dbtype}" />
    <input type="hidden" name="url" value="${pool.url}" />
    <input type="hidden" name="urlPrototype" value="${pool.urlPrototype}" />
    <input type="hidden" name="minSize" value="${pool.minSize}" />
    <input type="hidden" name="maxSize" value="${pool.maxSize}" />
    <input type="hidden" name="idleTimeout" value="${pool.idleTimeout}" />
    <input type="hidden" name="blockingTimeout" value="${pool.blockingTimeout}" />
    <input type="hidden" name="adapterDisplayName" value="${pool.adapterDisplayName}" />
    <input type="hidden" name="adapterDescription" value="${pool.adapterDescription}" />
    <input type="hidden" name="rarPath" value="${pool.rarPath}" />
    <input type="hidden" name="transactionType" value="${pool.transactionType}" />
  <c:forEach var="prop" items="${pool.properties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
    <table border="0">
    <!-- ENTRY FIELD: Driver Class -->
      <tr>
        <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>driverClass"><fmt:message key="dbwizard.common.JDBCDriverClass"/></label>:</div></th>
        <td><input name="driverClass" id="<portlet:namespace/>driverClass" type="text" size="30" value="${pool.driverClass}"></td>
      </tr>
      <tr>
        <td></td>
        <td>
          <fmt:message key="dbwizard.basicParams.seeDocumentation"/>
        </td>
      </tr>
    <!-- ENTRY FIELD: Driver JAR -->
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>jars"><fmt:message key="dbwizard.common.driverJAR"/></label>:</div></th>
        <td>
          <select multiple="true" name="jars" id="<portlet:namespace/>jars" size="10">
              <c:forEach var="availableJar" items="${availableJars}">
                  <option value="${availableJar}" <c:forEach var="jar" items="${pool.jars}"><c:if test="${availableJar == jar}">selected</c:if></c:forEach>>
                      ${availableJar}
                  </option>
              </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>
        <fmt:message key="dbwizard.common.driverJARExplanation"/>        
          <input type="button" value='<fmt:message key="dbwizard.common.downloadDriver"/>' onclick="document.<portlet:namespace/>DatabaseForm.mode.value='download';document.<portlet:namespace/>DatabaseForm.submit();return false;" />)
        </td>
      </tr>
    <!-- ENTRY FIELD: Username -->
      <tr>
        <th><div align="right"><fmt:message key="dbwizard.common.DBUserName"/>:</div></th>
        <td><input name="user" id="<portlet:namespace/>user" type="text" size="20" value="${pool.user}" autocomplete="off"></td>
      </tr>
      <tr>
        <td></td>
        <td><label for="<portlet:namespace/>user"><fmt:message key="dbwizard.common.DBUserNameExp"/></label></td>
      </tr>
    <!-- ENTRY FIELD: Password -->
      <tr>
        <th><div align="right"><fmt:message key="dbwizard.common.DBPassword"/>:</div></th>
        <td><input name="password" id="<portlet:namespace/>password" type="password" size="20" value="${pool.password}" autocomplete="off"></td>
      </tr>
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>confirm-password"><fmt:message key="dbwizard.common.confirmPassword"/></label>:</div></th>
        <td><input name="confirm-password" id="<portlet:namespace/>confirm-password" type="password" size="20" value="${pool.password}" autocomplete="off"></td>
      </tr>
      <tr>
        <td></td>
        <td><label for="<portlet:namespace/>password"><fmt:message key="dbwizard.common.DBPasswordExp"/></label></td>
      </tr>
    <!-- ENTRY FIELD: URL Properties -->
      <tr>
        <th colspan="2"><fmt:message key="dbwizard.basicParams.driverConnectionProperties"/></th>
      </tr>
      <tr>
        <th><div align="right"><fmt:message key="dbwizard.basicParams.typicalJDBCURL"/>:</div></th>
        <td><c:out value="${pool.urlPrototype}" /></td>
      </tr>
  <c:forEach var="prop" items="${pool.urlProperties}">
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>${prop.key}">${fn:substringAfter(prop.key,"urlproperty-")}:</label></div></th>
        <td><input name="${prop.key}" id="<portlet:namespace/>${prop.key}" type="text" size="20" value="${prop.value}"></td>
      </tr>
      <tr>
        <td></td>
        <td>
        <fmt:message key="dbwizard.basicParams.propertyExp">
        <fmt:param value="${pool.dbtype}"/>
        </fmt:message>
       </td>
      </tr>
  </c:forEach>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td><input type="submit" value='<fmt:message key="dbwizard.common.next"/>'  onClick="return <portlet:namespace/>validateForm();"/></td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>"><fmt:message key="dbwizard.common.cancel"/></a></p>
