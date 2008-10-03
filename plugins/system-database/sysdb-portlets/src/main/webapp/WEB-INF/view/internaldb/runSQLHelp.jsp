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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="systemdatabase"/>
<p><fmt:message key="internaldb.runSQLHelp.summary"/><br>
  </p>
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
<c:choose>
 <c:when test="${connectionMode == 'datasource'}">
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top"><fmt:message key="internaldb.common.useDS"/>:</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top"><fmt:message key="internaldb.runSQLHelp.useDSExp"/></td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top">
      <img src="/console/images/run_sql.gif" alt="Run SQL"/></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top"><fmt:message key="internaldb.runSQLHelp.runSQLExp"/></td>
  </tr>
 </c:when>
 <c:otherwise>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top"><fmt:message key="internaldb.common.createDB"/>: / <br>      
      <img src="/console/images/create.gif" alt="Create"/></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top"><fmt:message key="internaldb.runSQLHelp.createDBExp"/></td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top"><fmt:message key="internaldb.common.deleteDB"/>: / <br>      
      <img src="/console/images/delete.gif" alt="Delete"/></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top"><fmt:message key="internaldb.runSQLHelp.deleteDBExp"/></td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top"><fmt:message key="internaldb.common.useDB"/>:</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top"><fmt:message key="internaldb.runSQLHelp.useDBExp"/></td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top">
      <img src="/console/images/run_sql.gif" alt="Run SQL"/></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top"><fmt:message key="internaldb.runSQLHelp.runSQLExp"/></td>
  </tr>
 </c:otherwise>
</c:choose>
</table>
