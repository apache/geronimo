<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p><b>Create Database Pool</b> -- Step 1: Select Name and Database</p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="process-rdbms" />
    <input type="hidden" name="user" value="${pool.user}" />
    <input type="hidden" name="password" value="${pool.password}" />
    <input type="hidden" name="driverClass" value="${pool.driverClass}" />
    <input type="hidden" name="url" value="${pool.url}" />
    <input type="hidden" name="urlPrototype" value="${pool.urlPrototype}" />
    <input type="hidden" name="jar1" value="${pool.jar1}" />
    <input type="hidden" name="jar2" value="${pool.jar2}" />
    <input type="hidden" name="jar3" value="${pool.jar3}" />
    <input type="hidden" name="minSize" value="${pool.minSize}" />
    <input type="hidden" name="maxSize" value="${pool.maxSize}" />
    <input type="hidden" name="idleTimeout" value="${pool.idleTimeout}" />
    <input type="hidden" name="blockingTimeout" value="${pool.blockingTimeout}" />
    <input type="hidden" name="adapterDisplayName" value="${pool.adapterDisplayName}" />
    <input type="hidden" name="adapterDescription" value="${pool.adapterDescription}" />
    <input type="hidden" name="rarPath" value="${pool.rarPath}" />
  <c:forEach var="prop" items="${pool.properties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
  <c:forEach var="prop" items="${pool.urlProperties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
    <table border="0">
    <!-- ENTRY FIELD: NAME -->
      <tr>
        <th><div align="right">Name of Database Pool:</div></th>
        <td><input name="name" type="text" size="30" value="${pool.name}"></td>
      </tr>
      <tr>
        <td></td>
        <td>A name that is different than the name for any other database pools in the server (no spaces in the name please).</td>
      </tr>
    <!-- ENTRY FIELD: DB TYPE -->
      <tr>
        <th><div align="right">Database Type:</div></th>
        <td>
          <select name="dbtype">
        <c:forEach var="db" items="${databases}">
            <option <c:if test="${db.name == pool.dbtype}">selected</c:if>>${db.name}</option>
        </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>The type of database the pool will connect to.</td>
      </tr>
      <tr>
        <td></td>
        <td><input type="submit" value="Next" /></td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<%--
<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="params" />
            </portlet:actionURL>">Select predefined database</a></p>
<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="edit" />
            </portlet:actionURL>">Select "other" database</a></p>
--%>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>">Cancel</a></p>
