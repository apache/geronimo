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
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg" %>
<fmt:setBundle basename="systemdatabase"/>
<portlet:defineObjects/>


<p><fmt:message key="dbwizard.testConnection.title"/></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>DatabaseForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="save" />
    <input type="hidden" name="user" value="${pool.user}" />
    <input type="hidden" name="name" value="${pool.name}" />
    <input type="hidden" name="dbtype" value="${pool.dbtype}" />
    <input type="hidden" name="password" value="${pool.password}" />
    <input type="hidden" name="driverClass" value="${pool.driverClass}" />
    <input type="hidden" name="url" value="${pool.url}" />
    <input type="hidden" name="urlPrototype" value="${pool.urlPrototype}" />
    <c:forEach var="jar" items="${pool.jars}">
     <input type="hidden" name="jars" value="${jar}" />
    </c:forEach>    
    <input type="hidden" name="adapterDisplayName" value="${pool.adapterDisplayName}" />
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
        <tr>
            <td style="min-width: 140px"><div align="right"><fmt:message key="dbwizard.testConnection.testResult"/>:</div></td>
                <td>
                    <c:choose>
                        <c:when test="${connected}">
                            <fmt:message key="dbwizard.testConnection.connectedTo"/> ${targetDBInfo}              
                        </c:when>
                        <c:otherwise>
                            <font color="red"><i><fmt:message key="dbwizard.testConnection.connectionError"/></i></font>
                            <CommonMsg:commonMsg/>
                        </c:otherwise>
                    </c:choose>
                </td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td>&nbsp;</td>
        <td>
          <c:choose>
            <c:when test="${connected}">
                <input type="submit" value='<fmt:message key="dbwizard.common.deploy"/>' />
                <input type="button" value='<fmt:message key="dbwizard.common.showPlan"/>' onclick="document.<portlet:namespace/>DatabaseForm.mode.value='plan';document.<portlet:namespace/>DatabaseForm.submit();return false;" />                
            </c:when>
            <c:otherwise>               
                <input type="submit" value='<fmt:message key="dbwizard.testConnection.deployAnyway"/>' />
                <input type="button" value='<fmt:message key="dbwizard.common.editSettings"/>' onclick="document.<portlet:namespace/>DatabaseForm.mode.value='edit';document.<portlet:namespace/>DatabaseForm.submit();return false;" />
                <input type="button" value='<fmt:message key="dbwizard.testConnection.testAgain"/>' onclick="document.<portlet:namespace/>DatabaseForm.mode.value='process-url';document.<portlet:namespace/>DatabaseForm.submit();return false;" />
            </c:otherwise>
          </c:choose>
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>"><fmt:message key="dbwizard.common.cancel"/></a></p>
