<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<portlet:defineObjects/>

<%-- TODO: Check if datasource is created --%>
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
  <sql:query var="table">
    select * from ${tbl}
  </sql:query>
</sql:transaction>

<center><b>DB: <c:out value="${db}" />&nbsp;&nbsp;&nbsp;Table: <c:out value="${tbl}" /></b></center>
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

<br>
<a href="<portlet:actionURL portletMode="view">
           <portlet:param name="action" value="listTables" />
           <portlet:param name="db" value="${db}" />
           <portlet:param name="viewTables" value="${viewTables}" />
         </portlet:actionURL>">View Tables
</a>
&nbsp;&nbsp;|&nbsp;&nbsp;
<a href="<portlet:actionURL portletMode="view">
           <portlet:param name="action" value="listDatabases" />
         </portlet:actionURL>">View Databases
</a>