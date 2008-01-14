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
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="systemdatabase"/>
<portlet:defineObjects/>

<%-- TODO: Check if datasource is created --%>
<%-- Datasource --%>
<%-- Datasource --%>
<c:if test="${ds == null}">
    <%-- Create the connection manually --%>
    <sql:setDataSource
      var="ds"
      driver="org.apache.derby.jdbc.EmbeddedDriver"
      url="jdbc:derby:${db};create=true"
      user=""
      password=""
    />
</c:if>

<%-- Select statement --%>
<sql:transaction dataSource="${ds}">
  <sql:query var="tables">
    <%-- Set select statement depending on the view table type --%>
    <c:choose>
      <c:when test="${viewTables == 'application'}">
        select s.schemaname, t.tablename from sys.sysschemas s join sys.systables t on s.schemaid = t.schemaid
          where TABLETYPE='T'
          order by s.schemaname, t.tablename
      </c:when>
      <c:when test="${viewTables == 'system'}">
          select s.schemaname, t.tablename from sys.sysschemas s join sys.systables t on s.schemaid = t.schemaid
            where s.schemaname='SYS'
            order by s.schemaname, t.tablename
      </c:when>
    </c:choose>
  </sql:query>
</sql:transaction>

<center><b><fmt:message key="internaldb.common.DB"/>: <c:out value="${db}" /></b></center>
<table width="100%">
  <tr>
    <td class="DarkBackground" colspan="3" align="center"><fmt:message key="internaldb.common.tables"/></td>
  </tr>
  <%-- Check if there are tables to display  --%>
  <c:choose>
    <c:when test="${tables.rowCount == 0}">
      <tr>
        <td class="LightBackground" colspan="2" align="center">*** <fmt:message key="internaldb.listTables.noTables"/> ***</td>
      </tr>
    </c:when>
    <c:otherwise>
      <%-- Get the value of each column while iterating over rows --%>
      <c:forEach var="row" items="${tables.rowsByIndex}" varStatus="status">
      <jsp:useBean type="javax.servlet.jsp.jstl.core.LoopTagStatus" id="status" />
      <tr>
        <%-- Select table data class --%>
        <c:choose>
          <c:when test="<%= status.getCount() % 2 == 1 %>">
            <c:set var="tdClass" scope="page" value="LightBackground" />
          </c:when>
            <c:otherwise>
              <c:set var="tdClass" scope="page" value="MediumBackground" />
            </c:otherwise>
          </c:choose>
            <td class="<c:out value='${tdClass}' />"><c:out value="${row[0]}" /></td>
            <td class="<c:out value='${tdClass}' />"><c:out value="${row[1]}" /></td>
            <td class="<c:out value='${tdClass}' />" align="center">
              <a href="<portlet:actionURL portletMode="view">
                         <portlet:param name="action" value="viewTableContents" />
                         <portlet:param name="db" value="${db}" />
                         <portlet:param name="tbl" value="${row[0]}.${row[1]}" />
                         <portlet:param name="viewTables" value="${viewTables}" />
                       </portlet:actionURL>"><fmt:message key="internaldb.common.viewContents"/></a>
            </td>
      </tr>
      </c:forEach>
    </c:otherwise>
  </c:choose>
</table>

<br>
<a href="<portlet:actionURL portletMode="view">
           <portlet:param name="action" value="listDatabases" />
         </portlet:actionURL>"><fmt:message key="internaldb.common.viewDatabases"/>
</a>
