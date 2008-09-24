<%--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="systemdatabase"/>
<portlet:defineObjects/>

<p><fmt:message key="dbwizard.selectDownload.title"/></p>

<fmt:message key="dbwizard.selectDownload.summary"/>


<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>DatabaseForm" action="<portlet:actionURL/>" method="POST" onSubmit="startProgress()">
    <input type="hidden" name="mode" value="process-download" />
    <input type="hidden" name="name" value="${pool.name}" />
    <input type="hidden" name="dbtype" value="${pool.dbtype}" />
    <input type="hidden" name="user" value="${pool.user}" />
    <input type="hidden" name="password" value="${pool.password}" />
    <input type="hidden" name="driverClass" value="${pool.driverClass}" />
    <input type="hidden" name="url" value="${pool.url}" />
    <input type="hidden" name="urlPrototype" value="${pool.urlPrototype}" />
    <c:forEach var="jar" items="${pool.jars}">
     <input type="hidden" name="jars" value="${jar}" />
    </c:forEach>    <input type="hidden" name="adapterDisplayName" value="${pool.adapterDisplayName}" />
    <input type="hidden" name="minSize" value="${pool.minSize}" />
    <input type="hidden" name="maxSize" value="${pool.maxSize}" />
    <input type="hidden" name="idleTimeout" value="${pool.idleTimeout}" />
    <input type="hidden" name="blockingTimeout" value="${pool.blockingTimeout}" />
    <input type="hidden" name="adapterDisplayName" value="${pool.adapterDisplayName}" />
    <input type="hidden" name="adapterDescription" value="${pool.adapterDescription}" />
    <input type="hidden" name="rarPath" value="${pool.rarPath}" />
    <input type="hidden" name="transactionType" value="${pool.transactionType}" />
  <c:forEach var="prop" items="${pool.properties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
  <c:forEach var="prop" items="${pool.urlProperties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
    <table border="0">
    <!-- ENTRY FIELD: DRIVER TYPE -->
      <tr>
        <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>driverName"><fmt:message key="dbwizard.selectDownload.selectDriver"/></label>:</div></th>
        <td>
          <select name="driverName" id="<portlet:namespace/>driverName">
        <c:forEach var="driver" items="${drivers}">
            <option>${driver.name}</option>
        </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.selectDownload.selectDriverExp"/></td>
      </tr>
      <tr>
        <td></td>
        <td>
          <input type="submit" value='<fmt:message key="dbwizard.common.next"/>' />
          <input type="button" value='<fmt:message key="dbwizard.common.cancel"/>' onclick="document.<portlet:namespace/>DatabaseForm.mode.value='params';document.<portlet:namespace/>DatabaseForm.submit();return false;" />
        </td>
      </tr>
    </table>
</form>

<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<%--
<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="params" />
            </portlet:actionURL>"><fmt:message key="dbwizard.selectDownload.selectPredefinedDatabase"/></a></p>
<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="edit" />
            </portlet:actionURL>"><fmt:message key="dbwizard.selectDownload.selectOtherDatabase"/></a></p>
--%>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>"><fmt:message key="dbwizard.common.returnToList"/></a></p>

<p><br /><br /><br /><fmt:message key="dbwizard.selectDownload.otherJDBCDrivers"/>:</p>
<ul>
  <li><a href="http://www.daffodildb.com/download/index.jsp">DaffodilDB</a></li>
  <li><a href="http://www.frontbase.com/cgi-bin/WebObjects/FrontBase">FrontBase</a></li>
  <li><a href="http://www.datadirect.com/products/jdbc/index.ssp">DataDirect SQL Server, DB2, Oracle, Informix, Sybase</a></li>
  <li><a href="http://www-306.ibm.com/software/data/informix/tools/jdbc/">Informix</a></li>
  <li><a href="http://www.intersystems.com/cache/downloads/index.html">InterSystems Cache</a></li>
  <li><a href="http://www.borland.com/products/downloads/download_jdatastore.html">JDataStore</a></li>
  <li><a href="http://developer.mimer.com/downloads/index.htm">Mimer</a></li>
  <li><a href="http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/index.html">Oracle</a></li>
  <li><a href="http://www.pervasive.com/developerzone/access_methods/jdbc.asp">Pervasive</a></li>
  <li><a href="http://www.pointbase.com/products/downloads/">Pointbase</a></li>
  <li><a href="http://www.progress.com/esd/index.ssp">Progress</a></li>
  <li><a href="http://msdn.microsoft.com/en-us/data/aa937724.aspx">Microsoft SQL Server</a></li>
</ul>
