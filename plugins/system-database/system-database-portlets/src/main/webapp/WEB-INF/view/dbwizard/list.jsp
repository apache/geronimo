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
<portlet:defineObjects/>

<p>This page lists all the available database pools.</p>

<c:choose>
  <c:when test="${empty(pools)}"><p><i>There are no database pools defined</i></p></c:when>
  <c:otherwise>
<p>For each pool listed, you can click the <b>usage</b> link to see examples of how
  to use the pool from your application.</p>

<table width="100%">
  <tr>
    <th class="DarkBackground" align="left">Name</th>
    <th class="DarkBackground" align="center">Deployed As</th>
    <th class="DarkBackground" align="center">State</th>
    <th class="DarkBackground" align="center">Actions</th>
  </tr>
<c:set var="backgroundClass" value='MediumBackground'/>
<c:forEach var="pool" items="${pools}">
  <c:choose>
      <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
      </c:when>
      <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
      </c:otherwise>
  </c:choose>
  <tr>
    <td class="${backgroundClass}">${pool.name}</td>
    <td class="${backgroundClass}">
      <c:choose>
        <c:when test="${empty pool.parentName}">
          Server-wide
        </c:when>
        <c:otherwise>
          ${pool.parentName}  <%-- todo: make this a link to an application portlet --%>
        </c:otherwise>
      </c:choose>
    </td>
    <td class="${backgroundClass}">${pool.stateName}</td>
    <td class="${backgroundClass}">
         <%--<c:choose>
               <c:when test="${info.stateName eq 'running'}">
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="stop" />
                 <portlet:param name="name" value="${info.objectName}" />
                 <portlet:param name="managerObjectName" value="${container.managerObjectName}" />
                 <portlet:param name="containerObjectName" value="${container.containerObjectName}" />
               </portlet:actionURL>">stop</a>
               </c:when>
               <c:otherwise>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="start" />
                 <portlet:param name="name" value="${info.objectName}" />
                 <portlet:param name="managerObjectName" value="${container.managerObjectName}" />
                 <portlet:param name="containerObjectName" value="${container.containerObjectName}" />
               </portlet:actionURL>">start</a>
               </c:otherwise>
             </c:choose>--%>
      <a href="<portlet:actionURL portletMode="view">
        <portlet:param name="mode" value="editExisting" />
        <portlet:param name="adapterAbstractName" value="${pool.adapterAbstractName}" />
        <portlet:param name="abstractName" value="${pool.factoryAbstractName}" />
      </portlet:actionURL>">edit</a>
      <a href="<portlet:actionURL portletMode="view">
        <portlet:param name="mode" value="usage" />
        <portlet:param name="name" value="${pool.name}" />
        <portlet:param name="abstractName" value="${pool.factoryAbstractName}" />
      </portlet:actionURL>">usage</a>
       <a href="<portlet:actionURL portletMode="view">
         <portlet:param name="mode" value="delete" />
         <portlet:param name="name" value="${info.objectName}" />
         <portlet:param name="adapterAbstractName" value="${pool.adapterAbstractName}" />
         <portlet:param name="abstractName" value="${pool.factoryAbstractName}" />
       </portlet:actionURL>">delete</a>
    </td>
  </tr>
</c:forEach>
</table>
  </c:otherwise>
</c:choose>

<p>Create a new database pool:</p>
<ul>
  <li><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="rdbms" />
            </portlet:actionURL>">Using the Geronimo database pool wizard</a></li>
  <li><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="startImport" />
              <portlet:param name="importSource" value="JBoss 4" />
              <portlet:param name="from" value="<tt>*-ds.xml</tt> file from the <tt>jboss4/server/name/deploy</tt> directory" />
            </portlet:actionURL>">Import from JBoss 4</a></li>
  <li><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="startImport" />
              <portlet:param name="importSource" value="WebLogic 8.1" />
              <portlet:param name="from" value="<tt>config.xml</tt> file from the WebLogic domain directory" />
            </portlet:actionURL>">Import from WebLogic 8.1</a></li>
</ul>
