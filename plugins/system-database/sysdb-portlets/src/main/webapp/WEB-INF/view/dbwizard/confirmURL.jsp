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
<fmt:setBundle basename="systemdatabase"/>
<portlet:defineObjects/>

<p><fmt:message key="dbwizard.confirmURL.title"/></p>

<script language="JavaScript">
function <portlet:namespace/>validate() {

   if (document.<portlet:namespace/>DatabaseForm.minSize.value == "") {
      document.<portlet:namespace/>DatabaseForm.minSize.value = 0;
   }
   if (document.<portlet:namespace/>DatabaseForm.maxSize.value == "") {
      document.<portlet:namespace/>DatabaseForm.maxSize.value = 10;
   }

   var min = parseInt(document.<portlet:namespace/>DatabaseForm.minSize.value); 
   var max = parseInt(document.<portlet:namespace/>DatabaseForm.maxSize.value); 
   result = true;

   if (isNaN(min)) {
      alert("Min pool size must be a number. Defaulted to 0");
      min = document.<portlet:namespace/>DatabaseForm.minSize.value = 0;
      result = false;
   }
   if (min < 0)
   {
      alert("Min pool size must be non-negative. Defaulted to 0");
      min = document.<portlet:namespace/>DatabaseForm.minSize.value = 0;
      result = false;
   }

   if (isNaN(max)) {
      alert("Max pool size must be a number. Defaulted to 10");
      max = document.<portlet:namespace/>DatabaseForm.maxSize.value = 10;
      result = false;
   }
   if (max <= 0)
   {
      alert("Max pool size must be greater than zero. Defaulted to 10" );
      max = document.<portlet:namespace/>DatabaseForm.maxSize.value = 10;
      result = false;
   } 

   if (min > max) {
      alert("Max pool size must be greater than Min pool size." );
      return false;
   }

   return result;
}
</script>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>DatabaseForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="process-url" />
    <input type="hidden" name="test" value="true" />
    <input type="hidden" name="name" value="${pool.name}" />
    <input type="hidden" name="dbtype" value="${pool.dbtype}" />
    <input type="hidden" name="user" value="${pool.user}" />
    <input type="hidden" name="password" value="${pool.password}" />
    <input type="hidden" name="urlPrototype" value="${pool.urlPrototype}" />
    <input type="hidden" name="driverClass" value="${pool.driverClass}" />
    <c:forEach var="jar" items="${pool.jars}">
     <input type="hidden" name="jars" value="${jar}" />
    </c:forEach>    <input type="hidden" name="adapterDisplayName" value="${pool.adapterDisplayName}" />
    <input type="hidden" name="adapterDescription" value="${pool.adapterDescription}" />
    <input type="hidden" name="rarPath" value="${pool.rarPath}" />
  <c:forEach var="prop" items="${pool.properties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
  <c:forEach var="prop" items="${pool.urlProperties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
    <table border="0">
    <!-- ENTRY FIELD: URL -->
      <tr>
        <th style="min-width: 140px"><div align="right"><fmt:message key="dbwizard.common.JDBCConnectURL"/>:</div></th>
        <td><input name="url" type="text" size="50" value="${pool.url}"></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.common.URLFits"/></td>
      </tr>
    <!-- STATUS FIELD: Driver Load -->
      <tr>
        <th><div align="right"><fmt:message key="dbwizard.confirmURL.driverStatus"/>:</div></th>
        <td><i><fmt:message key="dbwizard.confirmURL.loadedSuccessfully"/></i></td>
      </tr>
    <!-- HEADER -->
      <tr>
        <th colspan="2"><fmt:message key="dbwizard.common.connectionPoolParameters"/></th>
      </tr>
    <!-- ENTRY FIELD: Min Size -->
      <tr>
        <th><div align="right"><fmt:message key="dbwizard.common.poolMinSize"/>:</div></th>
        <td><input name="minSize" type="text" size="5" value="${pool.minSize}"></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.confirmURL.minimumNoOfCon"/></td>
      </tr>
    <!-- ENTRY FIELD: Max Size -->
      <tr>
        <th><div align="right"><fmt:message key="dbwizard.common.poolMaxSize"/>:</div></th>
        <td><input name="maxSize" type="text" size="5" value="${pool.maxSize}"></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.confirmURL.maxNoOfCon"/></td>
      </tr>
    <!-- ENTRY FIELD: Blocking Timeout -->
      <tr>
        <th><div align="right"><fmt:message key="dbwizard.common.blockingTimeout"/>:</div></th>
        <td><input name="blockingTimeout" type="text" size="7" value="${pool.blockingTimeout}"> (in milliseconds)</td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.confirmURL.blockingTimeoutExp"/></td>
      </tr>
    <!-- ENTRY FIELD: Idle timeout -->
      <tr>
        <th><div align="right"><fmt:message key="dbwizard.common.idleTimeout"/>:</div></th>
        <td><input name="idleTimeout" type="text" size="5" value="${pool.idleTimeout}"> (<fmt:message key="dbwizard.common.inMinutes"/>)</td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.confirmURL.idleTimeoutExp"/></td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
          <input type="button" value='<fmt:message key="dbwizard.common.testConnection"/>' onclick="if (<portlet:namespace/>validate()){document.<portlet:namespace/>DatabaseForm.test.value='true';document.<portlet:namespace/>DatabaseForm.submit();}" />
          <input type="button"  value='<fmt:message key="dbwizard.common.skipTestAndDeploy"/>' onclick="if (<portlet:namespace/>validate()){document.<portlet:namespace/>DatabaseForm.test.value='false';document.<portlet:namespace/>DatabaseForm.submit();return false;}" />
          <input type="button" value='<fmt:message key="dbwizard.common.skipTestAndShowPlan"/>' onclick="if (<portlet:namespace/>validate()){document.<portlet:namespace/>DatabaseForm.mode.value='plan';document.<portlet:namespace/>DatabaseForm.submit();return false;}" />
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>"><fmt:message key="dbwizard.common.cancel"/></a></p>
