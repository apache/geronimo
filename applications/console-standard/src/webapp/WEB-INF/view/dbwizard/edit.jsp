<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>This page edits a new or existing database pool.</p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>DatabaseForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="process-url" />
    <input type="hidden" name="test" value="true" />

    <input type="hidden" name="dbtype" value="${pool.dbtype}" />
    <input type="hidden" name="urlPrototype" value="${pool.urlPrototype}" />
    <input type="hidden" name="jar2" value="${pool.jar2}" />
    <input type="hidden" name="jar3" value="${pool.jar3}" />
  <c:forEach var="prop" items="${pool.urlProperties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
    <input type="hidden" name="objectName" value="${pool.objectName}" />
    <input type="hidden" name="adapterDisplayName" value="${pool.adapterDisplayName}" />
    <input type="hidden" name="adapterDescription" value="${pool.adapterDescription}" />
    <input type="hidden" name="rarPath" value="${pool.rarPath}" />

    <table border="0">
    <!-- ENTRY FIELD: NAME -->
      <tr>
        <th style="min-width: 140px"><div align="right">Pool Name:</div></th>
        <td>
      <c:choose> <%-- Can't change the pool name after deployment because it's wired into all the ObjectNames --%>
        <c:when test="${empty pool.objectName}">
          <input name="name" type="text" size="30" value="${pool.name}">
        </c:when>
        <c:otherwise>
          <input name="name" type="hidden" value="${pool.name}" />
          <b><c:out value="${pool.name}" /></b>
        </c:otherwise>
      </c:choose>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>A name that is different than the name for any other database pools in the server (no spaces in the name please).</td>
      </tr>
    <!-- STATUS FIELD: Display Name -->
      <tr>
        <th><div align="right">Pool Type:</div></th>
        <td><i><c:out value="${pool.adapterDisplayName}" /></i></td>
      </tr>
      <tr>
        <td />
        <td><c:out value="${pool.adapterDescription}" /></td>
      </tr>
    <!-- HEADER -->
      <tr>
        <th colspan="2">Basic Connection Properties</th>
      </tr>
<c:choose>
  <c:when test="${pool.generic}"> <%-- This is a standard TranQL JDBC pool -- we know what parameters it wants --%>
    <!-- ENTRY FIELD: Driver Class -->
      <tr>
        <th><div align="right">JDBC Driver Class:</div></th>
        <td>
      <c:choose>
        <c:when test="${empty pool.objectName}">
          <input name="driverClass" type="text" size="30" value="${pool.driverClass}">
        </c:when>
        <c:otherwise>
          <input type="hidden" name="driverClass" value="${pool.driverClass}" />
          <i><c:out value="${pool.driverClass}" /></i>
        </c:otherwise>
      </c:choose>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>
          <c:if test="${!(empty driverError)}"><font color="red"><b>Unable to load driver from selected JARs!</b></font></c:if>
          See the documentation for your JDBC driver.
        </td>
      </tr>
    <!-- ENTRY FIELD: Driver JAR -->
  <c:choose> <%-- Can't set JAR after deployment because we don't know how to dig through dependencies yet --%>
    <c:when test="${empty pool.objectName}">
      <tr>
        <th><div align="right">Driver JAR:</div></th>
        <td>
          <select name="jar1">
            <option></option>
        <c:forEach var="jar" items="${jars}">
            <option <c:if test="${jar == pool.jar1}">selected</c:if>>${jar}</option>
        </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>The JAR holding the selected JDBC driver.  Should be installed under GERONIMO/repository/ (or
          <input type="button" value="Download a Driver" onclick="document.<portlet:namespace/>DatabaseForm.mode.value='download';document.<portlet:namespace/>DatabaseForm.submit();return false;" />)
        </td>
      </tr>
    </c:when>
    <c:otherwise>
      <input type="hidden" name="jar1" value="${pool.jar1}" />
    </c:otherwise>
  </c:choose>
    <!-- ENTRY FIELD: URL -->
      <tr>
        <th><div align="right">JDBC Connect URL:</div></th>
        <td><input name="url" type="text" size="50" value="${pool.url}"></td>
      </tr>
      <tr>
        <td></td>
        <td>Make sure the generated URL fits the syntax for your JDBC driver.</td>
      </tr>
    <!-- ENTRY FIELD: Username -->
      <tr>
        <th><div align="right">DB User Name:</div></th>
        <td><input name="user" type="text" size="20" value="${pool.user}"></td>
      </tr>
      <tr>
        <td></td>
        <td>The username used to connect to the database</td>
      </tr>
    <!-- ENTRY FIELD: Password -->
      <tr>
        <th><div align="right">DB Password:</div></th>
        <td><input name="password" type="password" size="20" value="${pool.password}"></td>
      </tr>
      <tr>
        <td></td>
        <td>The password used to connect to the database

        <%-- Just to be safe, save all the non-Generic properties since we're not going to edit them here --%>
  <c:forEach var="prop" items="${pool.properties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
        </td>
      </tr>
  </c:when>
  <c:otherwise> <%-- This is an XA or other connection factory that we don't have special parameter handling for --%>
    <c:forEach var="prop" items="${pool.properties}">
      <tr>
        <th><div align="right">${pool.propertyNames[prop.key]}:</div></th>
        <td><input name="${prop.key}" type="<c:choose><c:when test="${fn:containsIgnoreCase(prop.key, 'password')}">password</c:when><c:otherwise>text</c:otherwise></c:choose>" size="20" value="${prop.value}"></td>
      </tr>
      <tr>
        <td></td>
        <td>${ConfigParams[prop.key].description}</td>
      </tr>
    </c:forEach>
      <tr><td colspan="2">
        <%-- Just to be safe, save all the Generic properties since we're not going to edit them here --%>
        <input type="hidden" name="user" value="${pool.user}" />
        <input type="hidden" name="password" value="${pool.password}" />
        <input type="hidden" name="driverClass" value="${pool.driverClass}" />
        <input type="hidden" name="url" value="${pool.url}" />
        <input type="hidden" name="jar1" value="${pool.jar1}" />
      </td></tr>
  </c:otherwise>
</c:choose>
    <!-- HEADER -->
      <tr>
        <th colspan="2">Connection Pool Parameters</th>
      </tr>
    <!-- ENTRY FIELD: Min Size -->
      <tr>
        <th><div align="right">Pool Min Size:</div></th>
        <td><input name="minSize" type="text" size="5" value="${pool.minSize}"></td>
      </tr>
      <tr>
        <td></td>
        <td>The minimum number of connections in the pool.  The default is 0.</td>
      </tr>
    <!-- ENTRY FIELD: Max Size -->
      <tr>
        <th><div align="right">Pool Max Size:</div></th>
        <td><input name="maxSize" type="text" size="5" value="${pool.maxSize}"></td>
      </tr>
      <tr>
        <td></td>
        <td>The maximum number of connections in the pool.  The default is 10.</td>
      </tr>
    <!-- ENTRY FIELD: Blocking Timeout -->
      <tr>
        <th><div align="right">Blocking Timeout:</div></th>
        <td><input name="blockingTimeout" type="text" size="7" value="${pool.blockingTimeout}"> (in milliseconds)</td>
      </tr>
      <tr>
        <td></td>
        <td>The length of time a caller will wait for a connection.  The default is 5000.</td>
      </tr>
    <!-- ENTRY FIELD: Idle timeout -->
      <tr>
        <th><div align="right">Idle Timeout:</div></th>
        <td><input name="idleTimeout" type="text" size="5" value="${pool.idleTimeout}"> (in minutes)</td>
      </tr>
      <tr>
        <td></td>
        <td>How long a connection can be idle before being closed.  The default is 15.</td>
      </tr>

    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
<c:choose> <%-- Don't know how to test a non-generic pool, so you can only save it --%>
  <c:when test="${pool.generic}">
    <c:choose> <%-- Can't test after deployment because we don't know what JAR to put on the ClassPath, can't show plan becasue we can't update a plan --%>
      <c:when test="${empty pool.objectName}">
          <input type="submit" value="Test Connection" />
          <input type="button" value="Skip Test and Deploy" onclick="document.<portlet:namespace/>DatabaseForm.test.value='false';document.<portlet:namespace/>DatabaseForm.submit();return false;" />
          <input type="button" value="Skip Test and Show Plan" onclick="document.<portlet:namespace/>DatabaseForm.mode.value='plan';document.<portlet:namespace/>DatabaseForm.submit();return false;" />
      </c:when>
      <c:otherwise>
          <input type="button" value="Save" onclick="document.<portlet:namespace/>DatabaseForm.mode.value='save';document.<portlet:namespace/>DatabaseForm.submit();return false;" />
      </c:otherwise>
    </c:choose>
  </c:when>
  <c:otherwise> <%-- Not a generic JDBC pool --%>
    <c:choose>
      <c:when test="${empty pool.objectName}"> <%-- If it's new we can preview the plan or save/deploy --%>
          <input type="button" value="Deploy" onclick="document.<portlet:namespace/>DatabaseForm.mode.value='save';document.<portlet:namespace/>DatabaseForm.submit();return false;" />
          <input type="button" value="Show Plan" onclick="document.<portlet:namespace/>DatabaseForm.mode.value='plan';document.<portlet:namespace/>DatabaseForm.submit();return false;" />
      </c:when>
      <c:otherwise> <%-- If it's existing we can only save --%>
          <input type="button" value="Save" onclick="document.<portlet:namespace/>DatabaseForm.mode.value='save';document.<portlet:namespace/>DatabaseForm.submit();return false;" />
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->



<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>">Cancel</a></p>
