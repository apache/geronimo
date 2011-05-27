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

<a href="<portlet:actionURL/>" >OSGi Manager</a> > Find Packages 
<br/><br/>
<table width="100%" class="TableLine" summary="OSGi install">
    <tr>
        <td>
            The <b>${packageTypeValue}</b> packages with keyword "<b>${packageStringValue}</b>"
        </td>
        <form id="packageForm" method="POST" action="<portlet:actionURL><portlet:param name='page' value='find_packages'/></portlet:actionURL>">
        <td align="right">
                Find (
                <input type="radio" name="packageType" value="import" />Import
                /
                <input type="radio" name="packageType" value="export" checked="true" />Export
                ) Packages:
                <input type="text" id="packageString" name="packageString" value=""/>&nbsp;
                <input type="submit" value="Go" />
        </td>
        </form>
    </tr>
</table>
<br/>
<table width="100%" class="TableLine" summary="Wired Bundles">
    <tr class="DarkBackground">
        <c:if test="${packageTypeValue == 'import'}" >          
            <th scope="col" width="40%">Import Packages</th>   
            <th scope="col" width="60%">Imported by Bundles</th> 
        </c:if>
        <c:if test="${packageTypeValue == 'export'}" >   
            <th scope="col" width="40%">Export Packages</th>   
            <th scope="col" width="60%">Exported by Bundles</th> 
        </c:if>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="pp" items="${packagePerspectives}">
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
                ${pp.packageInfo.packageName} (version=${pp.packageInfo.packageVersion})
            </td>
            <td class="${backgroundClass}">
                <c:forEach var="info" items="${pp.bundleInfos}">
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