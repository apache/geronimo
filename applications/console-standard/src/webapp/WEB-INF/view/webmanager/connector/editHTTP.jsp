<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<portlet:defineObjects/>

<form name="<portlet:namespace/>" action="<portlet:actionURL/>">
<input type="hidden" name="mode" value="${mode}">
<input type="hidden" name="protocol" value="${protocol}">
<input type="hidden" name="containerObjectName" value="${containerObjectName}">
<input type="hidden" name="managerObjectName" value="${managerObjectName}">
<c:if test="${mode eq 'save'}">
  <input type="hidden" name="objectName" value="${objectName}">
</c:if>
<table width="100%%"  border="0">

<!-- Name Field -->
<c:if test="${mode eq 'add'}">
  <tr>
    <td><div align="right">Unique Name: </div></td>
    <td><input name="name" type="text" size="30"></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>A name that is different than the name for any other web connectors in the server</td>
  </tr>
</c:if>
<!-- Host Field -->
  <tr>
    <td><div align="right">Host: </div></td>
    <td>
      <input name="host" type="text" size="30" value="${host}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The host name or IP to bind to.  The normal values are <tt>0.0.0.0</tt> (all interfaces) or <tt>localhost</tt> (local connections only)</td>
  </tr>
<!-- Port Field -->
  <tr>
    <td><div align="right">Port: </div></td>
    <td>
      <input name="port" type="text" size="5" value="${port}">
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The network port to bind to.</td>
  </tr>
<!-- Min Threads Field (Jetty only) -->
<c:if test="${server eq 'jetty'}">
  <tr>
    <td><div align="right">Min Threads: </div></td>
    <td>
      <input name="minThreads" type="text" size="3" value="${minThreads}">
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The minimum number of threads this connector should use to handle incoming requests</td>
  </tr>
</c:if>
<!-- Max Threads Field -->
  <tr>
    <td><div align="right">Max Threads: </div></td>
    <td>
      <input name="maxThreads" type="text" size="3" value="${maxThreads}">
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The maximum number of threads this connector should use to handle incoming requests</td>
  </tr>
<!-- Submit Button -->
  <tr>
    <td><div align="right"></div></td>
    <td><input name="submit" type="submit" value="Save"></td>
  </tr>
</table>
</form>
<a href='<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="list" />
         </portlet:actionURL>'>List connectors</a>