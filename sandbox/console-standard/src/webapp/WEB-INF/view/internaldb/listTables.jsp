<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
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
        select TABLENAME from SYS.SYSTABLES where TABLETYPE='T'
      </c:when>
      <c:when test="${viewTables == 'system'}">
        select TABLENAME from SYS.SYSTABLES where TABLETYPE='S' and TABLENAME!='SYSDUMMY1'
      </c:when>
    </c:choose>
  </sql:query>
</sql:transaction>

<center><b>DB: <c:out value="${db}" /></b></center>
<table width="100%">
  <tr>
    <td class="DarkBackground" colspan="2" align="center">Tables</td>
  </tr>
  <%-- Check if there are tables to display  --%>
  <c:choose>
    <c:when test="${tables.rowCount == 0}">
      <tr>
        <td class="LightBackground" colspan="2" align="center">*** No tables ***</td>
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
        <c:forEach var="column" items="${row}">
        <td class="<c:out value='${tdClass}' />"><c:out value="${column}" /></td>
        <td class="<c:out value='${tdClass}' />" align="center">
          <%-- Select table prefix --%>
          <c:choose>
            <c:when test="${viewTables == 'application'}">
              <c:set var="tblPrefix" scope="page" value="" />
            </c:when>
            <c:when test="${viewTables == 'system'}">
              <c:set var="tblPrefix" scope="page" value="SYS." />
            </c:when>
          </c:choose>
          <a href="<portlet:actionURL portletMode="view">
                     <portlet:param name="action" value="viewTableContents" />
                     <portlet:param name="db" value="${db}" />
                     <portlet:param name="tbl" value="${tblPrefix}${column}" />
                     <portlet:param name="viewTables" value="${viewTables}" />
                   </portlet:actionURL>">View Contents</a>
        </td>
        </c:forEach>
      </tr>
      </c:forEach>
    </c:otherwise>
  </c:choose>
</table>

<br>
<a href="<portlet:actionURL portletMode="view">
           <portlet:param name="action" value="listDatabases" />
         </portlet:actionURL>">View Databases
</a>