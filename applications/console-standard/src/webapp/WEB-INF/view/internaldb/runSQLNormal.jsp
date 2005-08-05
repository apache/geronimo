<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<portlet:defineObjects/>

<form action="<portlet:actionURL portletMode='view'/>" method="post">

<table width="100%"  border="0">
  <tr>
    <td><div align="right">Create DB:</div></td>
    <td><input name="createDB" type="text" size="30">&nbsp;
      <input type="submit" name="action" value="Create"></td>
    </tr>
  <tr>
    <td><div align="right">Delete DB:</div></td>
    <td>
      <select name="deleteDB">
      <c:forEach var="db" items="${databases}" varStatus="status">
        <option value="${db}">${db}</option>
      </c:forEach>
      </select>&nbsp;
      <input type="submit" name="action" value="Delete" onClick="javascript:return confirm('Are you sure you want to delete this database?')">
    </td>
  </tr>
  <tr>
    <td><div align="right">Use DB:</div></td>
    <td>
      <select name="useDB">
      <c:forEach var="db" items="${databases}" varStatus="status">
        <option value="${db}">${db}</option>
      </c:forEach>
      </select>&nbsp;
      <input type="submit" name="action" value="Run SQL"></td>
  </tr>
  <tr>
    <td></td>
    <td><div align="left">SQL Command/s:</td>
  </tr>
  <tr>
    <td></td>
    <td><textarea name="sqlStmts" cols="65" rows="15"><c:out value="${sqlStmts}" /></textarea></td>
  </tr>
</table>

<%-- Display action result --%>
<c:if test="${!empty actionResult}">
  Result:
  <hr>
  <c:out value="${actionResult}" />
  <hr>
</c:if>

<table width="100%"  border="0">
  <tr>
    <td></td>
    <td>Note:</td>
  </tr>
  <tr>
    <td></td>
    <td>1) Use ';' to separate multiple statements</td>
  </tr>
  <tr>
    <td></td>
    <td>2) Query results will be displayed for single 'Select' statement</td>
  </tr>
  <tr>
    <td></td>
    <td>3) Use single quotes to encapsulate literal strings</td>
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

<center><b>Query Result</b></center>
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
        <td class="LightBackground" colspan="<c:out value='${fn:length(table.columnNames)}' />" align="center">*** Empty ***</td>
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