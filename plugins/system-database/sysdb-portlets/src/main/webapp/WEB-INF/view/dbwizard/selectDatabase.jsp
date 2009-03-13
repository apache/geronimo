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
<fmt:setBundle basename="systemdatabase"/>

<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>DatabaseForm";
var <portlet:namespace/>requiredFields = new Array("name");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="dbwizard.common.emptyText"/>');
        return false;
    }
    return true;
}
</script>

<div id="<portlet:namespace/>CommonMsgContainer"></div>

<p><fmt:message key="dbwizard.selectDatabase.title"/></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>DatabaseForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="process-rdbms" />
    <input type="hidden" name="user" value="${pool.user}" />
    <input type="hidden" name="password" value="${pool.password}" />
    <input type="hidden" name="driverClass" value="${pool.driverClass}" />
    <input type="hidden" name="url" value="${pool.url}" />
    <input type="hidden" name="urlPrototype" value="${pool.urlPrototype}" />
    <c:forEach var="jar" items="${pool.jars}">
     <input type="hidden" name="jars" value="${jar}" />
    </c:forEach>    <input type="hidden" name="adapterDisplayName" value="${pool.adapterDisplayName}" />
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
  <c:forEach var="prop" items="${pool.urlProperties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
    <table border="0">
    <!-- ENTRY FIELD: NAME -->
      <tr>
        <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>name"><fmt:message key="dbwizard.selectDatabase.nameOfPool"/></label>:</div></th>
        <td><input name="name" id="<portlet:namespace/>name" type="text" size="30" value="${pool.name}"></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.selectDatabase.nameOfPoolExplanation"/></td>
      </tr>
    <!-- ENTRY FIELD: DB TYPE -->
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>dbtype"><fmt:message key="dbwizard.selectDatabase.databaseType"/></label>:</div></th>
        <td>
          <select name="dbtype" id="<portlet:namespace/>dbtype">
        <c:forEach var="db" items="${databases}">
            <option <c:if test="${db.name == pool.dbtype}">selected</c:if>>${db.name}</option>
        </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.selectDatabase.databaseTypeExp"/></td>
      </tr>
      <tr>
        <td></td>
        <td><input type="submit" value='<fmt:message key="dbwizard.common.next"/>' onClick="return <portlet:namespace/>validateForm();"/></td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<%--
<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="params" />
            </portlet:actionURL>">Select predefined database</a></p>
<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="edit" />
            </portlet:actionURL>">Select "other" database</a></p>
--%>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>"><fmt:message key="dbwizard.common.cancel"/></a></p>
