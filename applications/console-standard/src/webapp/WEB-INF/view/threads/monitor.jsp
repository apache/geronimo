<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>Thread pools statistics for ${poolName}:</p>

<table>
  <tr>
    <th align="right">Pool Max:</th>
    <td>${stats.threadsInUse.upperBound}</td>
  </tr>
  <tr>
    <th align="right">Lowest Recorded:</th>
    <td>${stats.threadsInUse.lowWaterMark}</td>
  </tr>
  <tr>
    <th align="right">Highest Recorded:</th>
    <td>${stats.threadsInUse.highWaterMark}</td>
  </tr>
  <tr>
    <th align="right">Threads in Use:</th>
    <td>${stats.threadsInUse.current}</td>
  </tr>
</table>

<c:if test="${! empty consumers}">
<p>Current consumers of threads in this pool:</p>

<table>
  <tr>
    <th>Description</th>
    <th># of Threads</th>
  </tr>
<c:forEach var="client" items="${consumers}">
  <tr>
    <td>${client.name}</td>
    <td>${client.threadCount}</td>
  </tr>
</c:forEach>
</table>
</c:if>