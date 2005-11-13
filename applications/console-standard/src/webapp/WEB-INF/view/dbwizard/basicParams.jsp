<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p><b>Create Database Pool</b> -- Step 2: Select Driver, JAR, Parameters</p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>DatabaseForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="process-params" />
    <input type="hidden" name="name" value="${pool.name}" />
    <input type="hidden" name="dbtype" value="${pool.dbtype}" />
    <input type="hidden" name="url" value="${pool.url}" />
    <input type="hidden" name="urlPrototype" value="${pool.urlPrototype}" />
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
    <table border="0">
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
    <!-- ENTRY FIELD: URL Properties -->
      <tr>
        <th colspan="2">Driver Connection Properties</th>
      </tr>
      <tr>
        <th><div align="right">Typical JDBC URL:</div></th>
        <td><c:out value="${pool.urlPrototype}" /></td>
      </tr>
  <c:forEach var="prop" items="${pool.urlProperties}">
      <tr>
        <th><div align="right">${fn:substringAfter(prop.key,"urlproperty-")}:</div></th>
        <td><input name="${prop.key}" type="text" size="20" value="${prop.value}"></td>
      </tr>
      <tr>
        <td></td>
        <td>A property used to connect to ${pool.dbtype}.  May be optional (see JDBC driver documentation).</td>
      </tr>
  </c:forEach>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td><input type="submit" value="Next" /></td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>">Cancel</a></p>
