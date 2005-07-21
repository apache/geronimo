<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<portlet:defineObjects/>

<center><b>Database List</b></center>
<table width="100%">
  <tr>
    <td class="DarkBackground" colspan="1" align="center">Databases</td>
    <td class="DarkBackground" colspan="2" align="center">View Tables</td>
  </tr>
  <%-- Check if there are databases to display  --%>
  <c:choose>
    <c:when test="${fn:length(databases) == 0}">
      <tr>
        <td class="LightBackground" colspan="3" align="center">*** No databases ***</td>
      </tr>
    </c:when>
    <c:otherwise>
      <c:forEach var="db" items="${databases}" varStatus="status">
      <jsp:useBean type="javax.servlet.jsp.jstl.core.LoopTagStatus" id="status" />
      <tr>
        <c:choose>
          <c:when test="<%= status.getCount() % 2 == 1 %>">
            <c:set var="tdClass" scope="page" value="LightBackground" />
          </c:when>
      	  <c:otherwise>
      	    <c:set var="tdClass" scope="page" value="MediumBackground" />
      	  </c:otherwise>
      	</c:choose>
        <td class="<c:out value='${tdClass}' />"><c:out value="${db}" /></td>
        <td class="<c:out value='${tdClass}' />" align="center">
          <a href="<portlet:actionURL portletMode="view">
                     <portlet:param name="action" value="listTables" />
                     <portlet:param name="db" value="${db}" />
                     <portlet:param name="viewTables" value="application" />
                   </portlet:actionURL>">Application</a>
        </td>
        <td class="<c:out value='${tdClass}' />" align="center">
          <a href="<portlet:actionURL portletMode="view">
                     <portlet:param name="action" value="listTables" />
                     <portlet:param name="db" value="${db}" />
                     <portlet:param name="viewTables" value="system" />
                   </portlet:actionURL>">System</a>
        </td>
      </tr>
      </c:forEach>
    </c:otherwise>
  </c:choose>
</table>
