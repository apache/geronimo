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

<%--
Choose DB: &nbsp; 
<c:choose>
  <c:when test="${(param.rdbms == '1') || (empty param.rdbms)}">
    Derby
  </c:when>
  <c:otherwise>
    <a href="<portlet:actionURL portletMode="view">
               <portlet:param name="rdbms" value="1" />
             </portlet:actionURL>">Derby
    </a>
  </c:otherwise>
</c:choose>

&nbsp;|&nbsp;

<c:choose>
  <c:when test="${param.rdbms == '2'}">
    MS SQL
  </c:when>
  <c:otherwise>
    <a href="<portlet:actionURL portletMode="view">
               <portlet:param name="rdbms" value="2" />
             </portlet:actionURL>">MS SQL
    </a>
  </c:otherwise>
</c:choose>
--%>
<b><fmt:message key="internaldb.common.DB"/>:</b>
<table width="100%" class="TableLine" summary="DB">
  <tr> 
    <th scope="col" class="DarkBackground" width="20%" align="center"><fmt:message key="internaldb.common.Item"/></th>
    <th scope="col" class="DarkBackground" width="80%" align="center"><fmt:message key="internaldb.common.Value"/></th>
  </tr> 
  <tr> 
    <td class="LightBackground" width="20%" nowrap><fmt:message key="internaldb.common.DBProductName"/></td> 
    <td class="LightBackground" width="80%">${internalDB['DB Product Name']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground"><fmt:message key="internaldb.common.DBProductVersion"/></td> 
    <td class="MediumBackground">${internalDB['DB Product Version']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground"><fmt:message key="internaldb.common.DBMajorVersion" /></td> 
    <td class="LightBackground">${internalDB['DB Major Version']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground"><fmt:message key="internaldb.common.DBMinorVersion"/></td> 
    <td class="MediumBackground">${internalDB['DB Minor Version']}</td> 
  </tr> 
</table>
<br/>

<b><fmt:message key="internaldb.common.driver"/>:</b>
<table width="100%" class="TableLine" summary="Driver">
  <tr> 
    <th scope="col" class="DarkBackground" width="20%" align="center"><fmt:message key="internaldb.common.Item"/></th>
    <th scope="col" class="DarkBackground" width="80%" align="center"><fmt:message key="internaldb.common.Value"/></th>
  </tr> 
  <tr> 
    <td class="LightBackground" width="20%" nowrap><fmt:message key="internaldb.common.driverName"/></td> 
    <td class="LightBackground" width="80%">${internalDB['Driver Name']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground"><fmt:message key="internaldb.common.driverVersion"/></td> 
    <td class="MediumBackground">${internalDB['Driver Version']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground"><fmt:message key="internaldb.common.driverMajorVersion"/></td> 
    <td class="LightBackground">${internalDB['Driver Major Version']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground"><fmt:message key="internaldb.common.driverMinorVersion"/></td> 
    <td class="MediumBackground">${internalDB['Driver Minor Version']}</td> 
  </tr> 
</table>
<br/>
  
<b>JDBC:</b>
<table width="100%" class="TableLine" summary="JDBC">
  <tr> 
    <th scope="col" class="DarkBackground" width="20%" align="center"><fmt:message key="internaldb.common.Item"/></th>
    <th scope="col" class="DarkBackground" width="80%" align="center"><fmt:message key="internaldb.common.Value"/></th>
  </tr>
  <tr> 
    <td class="LightBackground" width="20%" nowrap><fmt:message key="internaldb.common.JDBCMajorVersion"/></td> 
    <td class="LightBackground" width="80%">${internalDB['JDBC Major Version']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground"><fmt:message key="internaldb.common.JDBCMinorVersion"/></td> 
    <td class="MediumBackground">${internalDB['JDBC Minor Version']}</td> 
  </tr> 
</table>
<br/>

<b><fmt:message key="internaldb.common.etc"/>:</b>
<table width="100%" class="TableLine" summary="ETC">
  <tr> 
    <th scope="col" class="DarkBackground" width="20%" align="center"><fmt:message key="internaldb.common.Item"/></th>
    <th scope="col" class="DarkBackground" width="80%" align="center"><fmt:message key="internaldb.common.Value"/></th>
  </tr>
  <tr> 
    <td class="LightBackground" width="20%" nowrap>URL</td> 
    <td class="LightBackground" width="80%">${internalDB['URL']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground"><fmt:message key="internaldb.common.userName"/></td> 
    <td class="MediumBackground">${internalDB['Username']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground"><fmt:message key="internaldb.common.readOnly"/></td> 
    <td class="LightBackground">${internalDB['Read Only']}</td> 
  </tr> 
</table>
<br/>

<b><fmt:message key="internaldb.common.functions"/>:</b>
<table width="100%" class="TableLine" summary="Functions">
  <tr> 
    <th scope="col" class="DarkBackground" width="20%" align="center"><fmt:message key="internaldb.common.Item"/></th>
    <th scope="col" class="DarkBackground" width="80%" align="center"><fmt:message key="internaldb.common.Value"/></th>
  </tr> 
  <tr> 
    <td class="LightBackground" width="20%" nowrap><fmt:message key="internaldb.common.numericFunctions"/></td> 
    <td class="LightBackground" width="80%">${internalDB['Numeric Functions']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground"><fmt:message key="internaldb.common.stringFunctions"/></td> 
    <td class="MediumBackground">${internalDB['String Functions']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground"><fmt:message key="internaldb.common.systemFunctions"/></td> 
    <td class="LightBackground">${internalDB['System Functions']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground"><fmt:message key="internaldb.common.timeDateFunctions"/></td> 
    <td class="MediumBackground">${internalDB['Time Date Functions']}</td> 
  </tr> 
</table>
<br/>
  
<b>SQL:</b>
<table width="100%" class="TableLine" summary="SQL">
  <tr> 
    <th scope="col" class="DarkBackground" width="20%" align="center"><fmt:message key="internaldb.common.Item"/></th>
    <th scope="col" class="DarkBackground" width="80%" align="center"><fmt:message key="internaldb.common.Value"/></th>
  </tr> 
  <tr>
    <td class="LightBackground" width="20%" nowrap><fmt:message key="internaldb.common.supportedSQLKeywords"/></td> 
    <td class="LightBackground" width="80%">${internalDB['Supported SQL Keywords']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground"><fmt:message key="internaldb.common.supportedTypes"/></td> 
    <td class="MediumBackground">${internalDB['Supported Types']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground"><fmt:message key="internaldb.common.tableTypes"/></td> 
    <td class="LightBackground">${internalDB['Table Types']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground"><fmt:message key="internaldb.common.schemas"/></td> 
    <td class="MediumBackground">${internalDB['Schemas']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground"><fmt:message key="internaldb.common.SQLStateType"/></td> 
    <td class="LightBackground">${internalDB['SQL State Type']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground"><fmt:message key="internaldb.common.defaultTransactionIsolation"/></td> 
    <td class="MediumBackground">${internalDB['Default Transaction Isolation']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground"><fmt:message key="internaldb.common.resultSetHoldability"/></td> 
    <td class="LightBackground">${internalDB['Result Set Holdability']}</td> 
  </tr> 
</table>
