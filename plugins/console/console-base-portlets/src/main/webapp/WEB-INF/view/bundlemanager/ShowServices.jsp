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
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<a href="<portlet:actionURL/>" >OSGi Manager</a> > Show Services
<br/><br/>
<table width="100%" class="BlankTableLine" summary="OSGi install">
    <tr>
        <td>
            The Services related to Bundle:
            &nbsp;
            <b>
            ${bundleInfo.symbolicName}
            </b>
            (id=${bundleInfo.bundleId})
            (version=${bundleInfo.bundleVersion})
            [${bundleInfo.state}]
        </td>
        </td>
        <td align="right">
            <c:if test="${bundleInfo.state.running}" >          
                View:&nbsp;
                <a href="<portlet:renderURL><portlet:param name='page' value='view_manifest'/><portlet:param name='bundleId' value='${bundleInfo.bundleId}'/></portlet:renderURL>">Manifest</a>
                &nbsp;|&nbsp;
                <a href="<portlet:renderURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${bundleInfo.bundleId}'/></portlet:renderURL>">Wired Bundles</a>
            </c:if>
        </td>
    </tr>
</table>
<br/>

<!--  Using Services  -->
Services in use:<br/>
<table width="100%" class="TableLine" summary="Wired Bundles">
    <tr class="DarkBackground">
        <th scope="col" width="40%">Reference Services (Object Class)</th>   
        <th scope="col" width="60%">From Bundles</th> 
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
      <c:forEach var="usp" items="${usingServicePerspectives}">
        <c:choose>
          <c:when test="${backgroundClass == 'MediumBackground'}" >
              <c:set var="backgroundClass" value='LightBackground'/>
          </c:when>
          <c:otherwise>
              <c:set var="backgroundClass" value='MediumBackground'/>
          </c:otherwise>
        </c:choose>
        <tr>
            <td class="${backgroundClass}">
                <c:forEach var="objectclass" items="${usp.serviceInfo.objectClass}">
                    ${objectclass}<br/>
                </c:forEach>
                    &nbsp;&nbsp;(service.id=${usp.serviceInfo.serviceId})
                <c:if test="${usp.serviceInfo.servicePid}" > 
                    <br/>
                    &nbsp;&nbsp;(service.pid=${usp.serviceInfo.servicePid})
                </c:if>
            </td>
            <td class="${backgroundClass}">
                <c:forEach var="info" items="${usp.bundleInfos}">
                    ${info.symbolicName} (id=${info.bundleId}) (version=${info.bundleVersion})
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_manifest'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_mf.png" alt="icon_mf.png" title="View Manifest" style="vertical-align:middle"/></a>
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_wb.png" alt="icon_wb.png" title="View Wired Bundles" style="vertical-align:middle"/></a>
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_services'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_serv.png" alt="icon_serv.png" title="View Services" style="vertical-align:middle"/></a>
                    <br/>
                </c:forEach>
            </td>
        </tr>
      </c:forEach>
</table>
<br/>
<!--  Registered Services  -->
Registered Services:<br/>
<table width="100%" class="TableLine" summary="Wired Bundles">
    <tr class="DarkBackground">
        <th scope="col" width="40%">Registered Services (Object Class)</th>   
        <th scope="col" width="60%">Using Bundles</th> 
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
      <c:forEach var="rsp" items="${registeredServicePerspectives}">
        <c:choose>
          <c:when test="${backgroundClass == 'MediumBackground'}" >
              <c:set var="backgroundClass" value='LightBackground'/>
          </c:when>
          <c:otherwise>
              <c:set var="backgroundClass" value='MediumBackground'/>
          </c:otherwise>
        </c:choose>
        <tr>
            <td class="${backgroundClass}">
                <c:forEach var="objectclass" items="${rsp.serviceInfo.objectClass}">
                    ${objectclass}<br/>
                </c:forEach>
                    &nbsp;&nbsp;(service.id=${rsp.serviceInfo.serviceId})
                <c:if test="${rsp.serviceInfo.servicePid}" > 
                    <br/>
                    &nbsp;&nbsp;(service.pid=${rsp.serviceInfo.servicePid})
                </c:if>
            </td>
            <td class="${backgroundClass}">
                <c:forEach var="info" items="${rsp.bundleInfos}">
                    ${info.symbolicName} (id=${info.bundleId}) (version=${info.bundleVersion})
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_manifest'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_mf.png" alt="icon_mf.png" title="View Manifest" style="vertical-align:middle"/></a>
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_wb.png" alt="icon_wb.png" title="View Wired Bundles" style="vertical-align:middle"/></a>
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_services'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_serv.png" alt="icon_serv.png" title="View Services" style="vertical-align:middle"/></a>
                    <br/>
                </c:forEach>
            </td>
        </tr>
      </c:forEach>
</table>
