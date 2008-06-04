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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="systemdatabase"/>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>DBForm";
var <portlet:namespace/>requiredFields = new Array("createDB");
var <portlet:namespace/>requiredFields2 = new Array("sqlStmts");

function <portlet:namespace/>validateForm1(){
    var action = eval("document.forms[<portlet:namespace/>formName].elements['action']");
    action.value="Create";
    return textElementsNotEmpty(<portlet:namespace/>formName, <portlet:namespace/>requiredFields);
}
function <portlet:namespace/>validateForm2(){
    var action = eval("document.forms[<portlet:namespace/>formName].elements['action']");
    action.value="Delete";
    return confirm('<fmt:message key="internaldb.runSQLNormal.reallyDeleteDatabase"/>')
}
function <portlet:namespace/>validateForm3(){
    var action = eval("document.forms[<portlet:namespace/>formName].elements['action']");
    action.value="Run SQL";
    return textElementsNotEmpty(<portlet:namespace/>formName, <portlet:namespace/>requiredFields2);
}
</script>

<form name="<portlet:namespace/>DBForm" action="<portlet:actionURL portletMode='view'/>" method="post">
<input type="hidden" name="action" value="" />
<table width="100%"  border="0">
  <tr>
    <td><div align="right"><fmt:message key="internaldb.common.createDB"/>:</div></td>
    <td><input name="createDB" type="text" size="30">&nbsp;
      <input type="submit" value='<fmt:message key="internaldb.common.create"/>' onClick="return <portlet:namespace/>validateForm1();"></td>
    </tr>
  <tr>
    <td><div align="right"><fmt:message key="internaldb.common.deleteDB"/>:</div></td>
    <td>
      <select name="deleteDB">
      <c:forEach var="db" items="${databases}" varStatus="status">
        <option value="${db}">${db}</option>
      </c:forEach>
      </select>&nbsp;
      <input type="submit" value='<fmt:message key="internaldb.common.delete"/>' onClick="return <portlet:namespace/>validateForm2();">
    </td>
  </tr>
  <tr>
    <td><div align="right"><fmt:message key="internaldb.common.useDB"/>:</div></td>
    <td>
      <select name="useDB">
      <c:forEach var="db" items="${databases}" varStatus="status">
        <option value="${db}">${db}</option>
      </c:forEach>
      </select>&nbsp;
      <input type="submit" value="Run SQL" onClick="return <portlet:namespace/>validateForm3();"></td>
  </tr>
  <tr>
    <td></td>
    <td><div align="left"><fmt:message key="internaldb.common.SQLCommands"/>:</td>
  </tr>
  <tr>
    <td></td>
    <td><textarea name="sqlStmts" cols="65" rows="15"><c:out value="${sqlStmts}" /></textarea></td>
  </tr>
</table>

<%-- Display action result --%>
<c:if test="${!empty actionResult}">
  <fmt:message key="internaldb.common.result"/>:
  <hr>
  <c:out value="${actionResult}" />
  <hr>
</c:if>

<table width="100%"  border="0">
  <tr>
    <td></td>
    <td><fmt:message key="internaldb.common.note"/>:</td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="internaldb.runSQLNormal.note1"/></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="internaldb.runSQLNormal.note2"/></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="internaldb.runSQLNormal.note3"/></td>
  </tr>
</table>
<br>

<%-- Display query result from single select statement --%>
<c:if test="${!empty singleSelectStmt}">
<%-- Datasource --%>
<c:if test="${ds == null}">
    <%-- Create the connection manually --%>
    <sql:setDataSource
      var="ds"
      driver="org.apache.derby.jdbc.EmbeddedDriver"
      url="jdbc:derby:${useDB};create=true"
      user=""
      password=""
    />
</c:if>

<%-- Select statement --%>
<sql:transaction dataSource="${ds}">
  <sql:query var="table">
    <%= request.getAttribute("singleSelectStmt") %>
  </sql:query>
</sql:transaction>

<center><b><fmt:message key="internaldb.common.queryResult"/></b></center>
<table width="100%">
  <tr>
  <%-- Get the column names for the header of the table --%>
  <c:forEach var="columnName" items="${table.columnNames}">
    <td class="DarkBackground"><c:out value="${columnName}" /></td>
  </c:forEach>
  </tr>
  
  <%-- Check if there are table data to display --%>
  <c:choose>
    <c:when test="${table.rowCount == 0}">
      <tr>
        <td class="LightBackground" colspan="<c:out value='${fn:length(table.columnNames)}' />" align="center">*** <fmt:message key="internaldb.common.empty"/> ***</td>
      </tr>
    </c:when>
    <c:otherwise>
      <%-- Get the value of each column while iterating over rows --%>
      <c:forEach var="row" items="${table.rowsByIndex}" varStatus="status">
        <jsp:useBean type="javax.servlet.jsp.jstl.core.LoopTagStatus" id="status" />
        <tr>
        <c:choose>
          <c:when test="<%= status.getCount() % 2 == 1 %>">
            <c:forEach var="column" items="${row}">
              <td class="LightBackground"><c:out value="${column}" /></td>
            </c:forEach>
          </c:when>
            <c:otherwise>
            <c:forEach var="column" items="${row}">
              <td class="MediumBackground"><c:out value="${column}" /></td>
            </c:forEach>
            </c:otherwise>
          </c:choose>
        </tr>
      </c:forEach>
    </c:otherwise>
  </c:choose>
</table>
</c:if>

</form>
