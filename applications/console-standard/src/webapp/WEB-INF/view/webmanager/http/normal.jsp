<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<% String mode = request.getParameter("mode");
if(mode != null && mode.equals("edit")){%>
<form method="POST" action="<portlet:actionURL/>">
<input type="hidden" name="gbeanId" value="${connector.id}">
<table width="100%">
  <tr>
    <td style="padding: 0 20px">
        <table width="50%">
          <tr><td class="DarkBackground">Name</td><td class="DarkBackground" align="center">Port</td></tr>
          <tr>
            <td>${connector.name}</td>
            <td align="center" width="0%"><input type="text" name="${connector.id}" value="${connector.port}"></td>
              <tr><td colspan="2">&nbsp;</td></tr>
              <tr><td colspan="2" align="center"><input type="submit" value="Update" /></td></tr>
          </tr>
      </table>
    </td>
  </tr>
</table>
</form>
<%}else{%>
<c:if test="${empty(connectors)}">There are no HTTP Connectors defined</c:if>
<c:if test="${!empty(connectors)}">
<table width="100%">
  <tr>
    <td style="padding: 0 20px">
        <table width="50%">
          <tr><td class="DarkBackground">Name</td><td class="DarkBackground" align="center">Port</td></tr>
        <c:forEach var="connector" items="${connectors}">
          <tr>
            <td>${connector.name}</td>
            <td align="center">${connector.port}</td>
            <td style="padding: 0 20px"><a href="<portlet:renderURL><portlet:param name="gbeanId" value="${connector.id}"/><portlet:param name="mode" value="edit"/></portlet:renderURL>">edit</a></td>
          </tr>
        </c:forEach>
      </table>
    </td>
  </tr>
</table>
</c:if>
<%}%>
