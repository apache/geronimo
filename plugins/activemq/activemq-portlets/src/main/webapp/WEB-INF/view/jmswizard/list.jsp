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
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg"%>
<fmt:setBundle basename="activemq"/>
<portlet:defineObjects/>

<CommonMsg:commonMsg/>
<p><fmt:message key="jmswizard.list.title" /></p>

<c:choose>
  <c:when test="${empty(resources)}"><p><i><fmt:message key="jmswizard.list.noJMSResourceGroups" /></i></p></c:when>
  <c:otherwise>
    <c:forEach var="resource" items="${resources}">
      <b><c:out value="${resource.name}"/> (<c:out value="${resource.configurationName}"/>):</b>
      <table width="100%" class="TableLine" summary="JMS Resources">
        <tr class="DarkBackground">
          <th scope="col"><fmt:message key="jmswizard.common.type"/></th>
          <th scope="col"><fmt:message key="jmswizard.common.name"/></th>
          <th scope="col" align="center"><fmt:message key="jmswizard.common.deployedAs"/></th>
          <th scope="col" align="center"><fmt:message key="jmswizard.common.state"/></th>
          <th scope="col" align="center"><fmt:message key="jmswizard.common.consumerCount"/></th>
          <th scope="col" align="center"><fmt:message key="jmswizard.common.queueSize"/></th>
          <th scope="col" align="center"><fmt:message key="jmswizard.common.actions"/></th>
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
            <tr class="${backgroundClass}">
              <td><fmt:message key="jmswizard.common.connFactory" /> </td>
              <td>${factory.name}</td>
              <td>
                <c:choose>
                  <c:when test="${empty resource.parentName}">
                    <fmt:message key="jmswizard.list.serverWide" /> 
                  </c:when>
                  <c:otherwise>
                   <fmt:message key="jmswizard.list.applicationScoped" /> 
                  </c:otherwise>
                </c:choose>
              </td>
              <td>${factory.stateName}</td>
              <td>&nbsp;</td>
              <td>&nbsp;</td>
              <td>&nbsp;
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
            <tr class="${backgroundClass}">
              <td>${admin.type}</td>
              <td>${admin.name}</td>
              <td>
                <c:choose>
                  <c:when test="${empty resource.parentName}">
                    <fmt:message key="jmswizard.list.serverWide" /> 
                  </c:when>
                  <c:otherwise>
                    <fmt:message key="jmswizard.list.applicationScoped" /> 
                  </c:otherwise>
                </c:choose>
              </td>
              <td>${admin.stateName}</td>
              <c:choose>
                  <c:when test="${admin.stateName == 'running'}">
                      <td>
                          <c:if test="${admin.destinationStat != null}">
                          	${admin.destinationStat.consumerCount}
                          </c:if>
                      </td>
                      <td>
                          <c:if test="${admin.destinationStat != null}">
                          	${admin.destinationStat.queueSize}
                          </c:if>
                      </td>
                      <td>
                      <c:if test="${admin.type == 'Queue' ? admin.queueBrowserSupported : admin.topicHistoryBrowserSupported}">
                         <a href="<portlet:actionURL portletMode="view">
        	                  <portlet:param name="mode" value="viewMessages-before" />
        	                  <portlet:param name="adminObjName" value="${admin.name}" />
        	                  <portlet:param name="physicalName" value="${admin.physicalName}" />
        	                  <portlet:param name="adminObjType" value="${admin.type}" />
        	                  <portlet:param name="adapterObjectName" value="${resource.adapterObjectName}" />
                              <portlet:param name="resourceAdapterModuleName" value="${resource.resourceAdapterModuleName}" />
        	                </portlet:actionURL>"><fmt:message key="jmswizard.common.Browse" /></a>
        	         </c:if>
                     <c:if test="${admin.sendMessageSupported}">
                     <a href="<portlet:actionURL portletMode="view">
                          <portlet:param name="mode" value="sendmessage-before" />
                          <portlet:param name="adminObjName" value="${admin.name}" />
                          <portlet:param name="physicalName" value="${admin.physicalName}" />
                          <portlet:param name="adminObjType" value="${admin.type}" />
                          <portlet:param name="adapterObjectName" value="${resource.adapterObjectName}" />
                          <portlet:param name="resourceAdapterModuleName" value="${resource.resourceAdapterModuleName}" />
                        </portlet:actionURL>"><fmt:message key="jmswizard.common.Send" /></a>
                      </c:if>
                      <c:if test="${admin.purgeSupported}">
                        <a href="<portlet:actionURL portletMode="view">
                          <portlet:param name="mode" value="list-before" />
                          <portlet:param name="purge" value="purge" />
                          <portlet:param name="adminObjName" value="${admin.name}" />
                          <portlet:param name="physicalName" value="${admin.physicalName}" />
                          <portlet:param name="adminObjType" value="${admin.type}" />
                          <portlet:param name="adapterObjectName" value="${resource.adapterObjectName}" />
                          <portlet:param name="resourceAdapterModuleName" value="${resource.resourceAdapterModuleName}" />
                        </portlet:actionURL>" onclick="return confirm('Confirm message purge?');"><fmt:message key="jmswizard.common.Purge"/></a>
                      </c:if>
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
                </c:when>
            <c:otherwise>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
            </c:otherwise>
            </c:choose>    
            </tr>
        </c:forEach>
      </table><br/>
    </c:forEach>
  </c:otherwise>
</c:choose>


<p><b><fmt:message key="jmswizard.list.createJMSResourceGroup" /></b></p>
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
