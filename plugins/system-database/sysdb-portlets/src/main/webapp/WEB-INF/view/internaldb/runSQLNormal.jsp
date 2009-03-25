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
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg" %>
<fmt:setBundle basename="systemdatabase"/>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>DBForm";
var <portlet:namespace/>requiredFields = new Array("createDB");
var <portlet:namespace/>requiredFields2 = new Array("sqlStmts");

function <portlet:namespace/>validateForm1(){
    var illegalChars= /[\.]{2}|[()<>,;:\\/"'\|]/ ;
    var action = document.forms[<portlet:namespace/>formName].elements['action'];
    action.value="Create";
    if (!textElementsNotEmpty(<portlet:namespace/>formName, <portlet:namespace/>requiredFields)) 
    {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="internaldb.common.emptyText"/>');
        return false;
    } else if (document.forms[<portlet:namespace/>formName].createDB.value.match(illegalChars)) {
        alert("Database name contains illegal characters");
        return false;
    }
    else
        return true;
}
function <portlet:namespace/>validateForm2(){
    var action = document.forms[<portlet:namespace/>formName].elements['action'];
    action.value="Delete";
    return confirm('<fmt:message key="internaldb.runSQLNormal.reallyDeleteDatabase"/>')
}
function <portlet:namespace/>validateForm3(){
    var action = document.forms[<portlet:namespace/>formName].elements['action'];
    action.value="Run SQL";
    return true;
}
</script>

<CommonMsg:commonMsg/><div id="<portlet:namespace/>CommonMsgContainer"></div><br>

<form name="<portlet:namespace/>DBForm" action="<portlet:actionURL portletMode='view'/>" method="post">
<input type="hidden" name="action" value="" />
<table width="100%"  border="0">
<c:choose>
 <c:when test="${connectionMode == 'database'}">
  <tr>
    <td><div align="right"><label for="<portlet:namespace/>createDB"><fmt:message key="internaldb.common.createDB"/></label>:</div></td>
    <td><input name="createDB" id="<portlet:namespace/>createDB" type="text" size="30">&nbsp;
      <input type="submit" value='<fmt:message key="internaldb.common.create"/>' onClick="return <portlet:namespace/>validateForm1();"></td>
    </tr>
  <tr>
    <td><div align="right"><label for="<portlet:namespace/>deleteDB"><fmt:message key="internaldb.common.deleteDB"/></label>:</div></td>
    <td>
      <select name="deleteDB" id="<portlet:namespace/>deleteDB">
      <c:forEach var="db" items="${databases}" varStatus="status">
        <option value="${db}">${db}</option>
      </c:forEach>
      </select>&nbsp;
      <input type="submit" value='<fmt:message key="internaldb.common.delete"/>' onClick="return <portlet:namespace/>validateForm2();">
    </td>
  </tr>
  <tr>
    <td><div align="right"><label for="<portlet:namespace/>useDB"><fmt:message key="internaldb.common.useDB"/></label>:</div></td>
    <td>
      <select name="useDB" id="<portlet:namespace/>useDB">
      <c:forEach var="db" items="${databases}" varStatus="status">
        <option value="${db}"<c:if test="${useDB==db}"> selected="selected"</c:if>>${db}</option>
      </c:forEach>
      </select>&nbsp;
      <input type="submit" value="<fmt:message key="internaldb.runSQLNormal.runSQL"/>" onClick="return <portlet:namespace/>validateForm3();"></td>
  </tr>
 </c:when>
 <c:otherwise>
  <tr>
    <td><div align="right"><label for="<portlet:namespace/>useDB"><fmt:message key="internaldb.common.useDS"/></label>:</div></td>
    <td>
      <select name="useDB" id="<portlet:namespace/>useDB">
      <c:forEach var="dsName" items="${dataSourceNames}" varStatus="status">
        <option value="${dsName}"<c:if test="${useDB==dsName}"> selected="selected"</c:if>>${dsName}</option>
      </c:forEach>
      </select>&nbsp;
      <input type="submit" value="<fmt:message key="internaldb.runSQLNormal.runSQL"/>" onClick="return <portlet:namespace/>validateForm3();"></td>
  </tr>
 </c:otherwise>
</c:choose>
  <tr>
    <td></td>
    <td><div align="left"><label for="<portlet:namespace/>sqlStmts"><fmt:message key="internaldb.common.SQLCommands"/></label>:</div></td>
  </tr>
  <tr>
    <td></td>
    <td><textarea name="sqlStmts" id="<portlet:namespace/>sqlStmts" cols="65" rows="15"><c:out value="${sqlStmts}" /></textarea></td>
  </tr>
</table>

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
    <%-- If in Database mode, make sure we have a Derby connection --%>
    <c:if test="${connectionMode == 'database'}">
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
