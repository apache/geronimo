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
<fmt:setBundle basename="activemq"/>
<portlet:defineObjects/>

<p><fmt:message key="jmswizard.list.title" /></p>

<c:choose>
  <c:when test="${empty (resources)}"><p><i><fmt:message key="jmswizard.list.noJMSResourceGroups" /></i></p></c:when>
  <c:otherwise>
<!--
<p>For each resource listed, you can click the <b>usage</b> link to see examples of how
  to use the connection factories and destinations from your application.</p>
-->
    <table width="100%">
      <c:forEach var="resource" items="${resources}">
        <tr>
            <td colspan="5" style="padding-top: 10px"><b><c:out value="${resource.name}"/> (<c:out value="${resource.configurationName}"/>)</b></td>
        </tr>
        <tr>
          <th class="DarkBackground"><fmt:message key="jmswizard.common.type"/></th>
          <th class="DarkBackground"><fmt:message key="jmswizard.common.name"/></th>
          <th class="DarkBackground" align="center"><fmt:message key="jmswizard.common.deployedAs"/></th>
          <th class="DarkBackground" align="center"><fmt:message key="jmswizard.common.state"/></th>
          <th class="DarkBackground" align="center"><fmt:message key="jmswizard.common.actions"/></th>
        </tr>
        <c:set var="backgroundClass" value='MediumBackground'/>
        <c:forEach var="factory" items="${resource.connectionFactories}">
            <c:choose>
                <c:when test="${backgroundClass == 'MediumBackground'}" >
                    <c:set var="backgroundClass" value='LightBackground'/>
                </c:when>
                <c:otherwise>
                    <c:set var="backgroundClass" value='MediumBackground'/>
                </c:otherwise>
            </c:choose>
            <tr>
              <td class="${backgroundClass}"><fmt:message key="jmswizard.common.connFactory" /> </td>
              <td class="${backgroundClass}">${factory.name}</td>
              <td class="${backgroundClass}">
                <c:choose>
                  <c:when test="${empty resource.parentName}">
                    <fmt:message key="jmswizard.list.serverWide" /> 
                  </c:when>
                  <c:otherwise>
                   <fmt:message key="jmswizard.list.applicationScoped" /> 
                  </c:otherwise>
                </c:choose>
              </td>
              <td class="${backgroundClass}">${factory.stateName}</td>
              <td class="${backgroundClass}">
                <%--
                <a href="<portlet:actionURL portletMode="view">
                  <portlet:param name="mode" value="editExisting" />
                  <portlet:param name="adapterObjectName" value="${pool.adapterObjectName}" />
                  <portlet:param name="objectName" value="${pool.factoryObjectName}" />
                </portlet:actionURL>">edit</a>
                <a href="<portlet:actionURL portletMode="view">
                  <portlet:param name="mode" value="usage" />
                  <portlet:param name="name" value="${pool.name}" />
                  <portlet:param name="objectName" value="${pool.factoryObjectName}" />
                </portlet:actionURL>">usage</a>
                Test
                Statistics
                --%>
              </td>
            </tr>
        </c:forEach>
        <c:forEach var="admin" items="${resource.adminObjects}">
            <c:choose>
                <c:when test="${backgroundClass == 'MediumBackground'}" >
                    <c:set var="backgroundClass" value='LightBackground'/>
                </c:when>
                <c:otherwise>
                    <c:set var="backgroundClass" value='MediumBackground'/>
                </c:otherwise>
            </c:choose>
            <tr>
              <td class="${backgroundClass}">${admin.type}</td>
              <td class="${backgroundClass}">${admin.name}</td>
              <td class="${backgroundClass}">
                <c:choose>
                  <c:when test="${empty resource.parentName}">
                    <fmt:message key="jmswizard.list.serverWide" /> 
                  </c:when>
                  <c:otherwise>
                    <fmt:message key="jmswizard.list.applicationScoped" /> 
                  </c:otherwise>
                </c:choose>
              </td>
              <td class="${backgroundClass}">${admin.stateName}</td>
              <td class="${backgroundClass}">
                <%--
                <a href="<portlet:actionURL portletMode="view">
                  <portlet:param name="mode" value="editExisting" />
                  <portlet:param name="adapterObjectName" value="${pool.adapterObjectName}" />
                  <portlet:param name="objectName" value="${pool.factoryObjectName}" />
                </portlet:actionURL>">edit</a>
                <a href="<portlet:actionURL portletMode="view">
                  <portlet:param name="mode" value="usage" />
                  <portlet:param name="name" value="${pool.name}" />
                  <portlet:param name="objectName" value="${pool.factoryObjectName}" />
                </portlet:actionURL>">usage</a>
                Test
                Statistics
                --%>
              </td>
            </tr>
        </c:forEach>
      </c:forEach>
    </table>
  </c:otherwise>
</c:choose>


<p><b>Create a new JMS Resource Group:</b></p>
<ul>
<c:forEach var="provider" items="${providers}">

  <li><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list-after" />
              <portlet:param name="provider" value="${provider.name}" />
            </portlet:actionURL>">For <c:out value="${provider.name}" /></a></li>
</c:forEach>
  <li><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list-after" />
            </portlet:actionURL>"><fmt:message key="jmswizard.list.forAnotherJMSProvider" /> </a></li>
<%--
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
--%>
</ul>
