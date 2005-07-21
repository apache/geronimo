<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<% String mode = request.getParameter("mode");
if(mode != null && mode.equals("edit")){%>
    <form method="POST" action="<portlet:actionURL/>">
    <input type="hidden" name="gbeanId" value="${connector.id}">
    <table width="100%">
      <tr><td colspan="2"><b>${connector.name}</b></tr>
      <tr>
        <td style="padding: 0 20px">
            <table>
              <tr><td>port</td><td><input type="text" name="port" value="${connector.port}"></td></tr>
              <tr><td colspan="2">&nbsp;</td></tr>
              <tr><td colspan="2">Keystore configuration:</td></tr>
              <tr><td>Keystore Type&nbsp;</td><td>JKS</td></tr>
              <tr>
                <td>Needs client authentication&nbsp;</td>
                <td><select name="needClientAuth">
                        <c:choose>
                        <c:when test="${connector.needClientAuth}">
                        <option value="true" selected>Yes</option>
                        <option value="false">No</option>
                        </c:when>
                        <c:otherwise>
                        <option value="true">Yes</option>
                        <option value="false" selected>No</option>
                        </c:otherwise>
                        </c:choose>
                    </select>
                </td>
              </tr>
              <tr><td colspan="2">&nbsp;</td></tr>
              <tr><td colspan="2" align="center"><input type="submit" value="Update" /></td></tr>
          </table>
        </td>
      </tr>
    </table>
    </form>
<%}else{%>
<c:if test="${empty(connectors)}">There are no HTTPS Connectors defined</c:if>
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
