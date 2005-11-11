<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
  <c:forEach var="prop" items="${pool.properties}">
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
        <td>A name that is different than the name for any other database pools in the server</td>
      </tr>
    <!-- HEADER -->
      <tr>
        <th colspan="2">Basic Connection Properties</th>
      </tr>
    <!-- ENTRY FIELD: Driver Class -->
      <tr>
        <th><div align="right">JDBC Driver Class:</div></th>
        <td><input name="driverClass" type="text" size="30" value="${pool.driverClass}"></td>
      </tr>
      <tr>
        <td></td>
        <td>
          <c:if test="${!(empty driverError)}"><font color="red"><b>Unable to load driver from selected JARs!</b></font></c:if>
          See the documentation for your JDBC driver.
        </td>
      </tr>
    <!-- ENTRY FIELD: Driver JAR -->
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
        <td>The password used to connect to the database</td>
      </tr>
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
        <td>The minimum number of connections in the pool.  Leave blank for default.</td>
      </tr>
    <!-- ENTRY FIELD: Max Size -->
      <tr>
        <th><div align="right">Pool Max Size:</div></th>
        <td><input name="maxSize" type="text" size="5" value="${pool.maxSize}"></td>
      </tr>
      <tr>
        <td></td>
        <td>The maximum number of connections in the pool.  Leave blank for default.</td>
      </tr>
    <!-- ENTRY FIELD: Blocking Timeout -->
      <tr>
        <th><div align="right">Blocking Timeout:</div></th>
        <td><input name="blockingTimeout" type="text" size="7" value="${pool.blockingTimeout}"> (in milliseconds)</td>
      </tr>
      <tr>
        <td></td>
        <td>The length of time a caller will wait for a connection.  Leave blank for default.</td>
      </tr>
    <!-- ENTRY FIELD: Idle timeout -->
      <tr>
        <th><div align="right">Idle Timeout:</div></th>
        <td><input name="idleTimeout" type="text" size="5" value="${pool.idleTimeout}"> (in minutes)</td>
      </tr>
      <tr>
        <td></td>
        <td>How long a connection can be idle before being closed.  Leave blank for default.</td>
      </tr>

    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td><input type="submit" value="Test Connection" />
          <input type="button" value="Skip Test" onclick="document.<portlet:namespace/>DatabaseForm.test.value='false';document.<portlet:namespace/>DatabaseForm.submit();return false;" />
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->



<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>">Cancel</a></p>
