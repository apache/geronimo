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
<table width="100%" class="BlankTableLine" summary="Find Packages">
    <tr>
        <td>
            The Packages with Keyword: "<b>${packageStringValue}</b>"
        </td>
        <form id="packageForm" method="POST" action="<portlet:actionURL><portlet:param name='page' value='find_packages'/></portlet:actionURL>">
        <td align="right">
                Find Packages:
                <input type="text" id="packageString" name="packageString" value="" title="Input nothing to list all packages"/>&nbsp;
                <input type="submit" value="Go" />
        </td>
        </form>
    </tr>
</table>
<br/>

<table width="100%" class="TableLine" summary="Find Packages Result">
    <tr class="DarkBackground">
        <th scope="col">Search Result (Click to see package's exporter and importer)</th>   
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="pwb" items="${packageWiredBundlesList}" varStatus="status">
        <c:choose>
            <c:when test="${backgroundClass == 'MediumBackground'}" >
                <c:set var="backgroundClass" value='LightBackground'/>
            </c:when>
            <c:otherwise>
                <c:set var="backgroundClass" value='MediumBackground'/>
            </c:otherwise>
        </c:choose>
        <tr class="${backgroundClass}">
            <td>
                <div style="cursor:pointer;clear:both;" onmouseover="highlightBgColor(this)" onmouseout="recoverBgColor(this)" onclick="showHideById('eiDiv-'+${status.index})">
                    ${pwb.packageInfo.packageName} (version=${pwb.packageInfo.packageVersion})
                </div>
                <div id="eiDiv-${status.index}" style="background-color:#F0F8FF;display:none">
                    <table width="100%" cellpadding="3">
                        <tr>
                            <td valign="top" width="100px">
                                - exporters:
                            </td>
                            <td>
                                <c:forEach var="info" items="${pwb.exportBundleInfos}">
                                    ${info.symbolicName} (id=${info.bundleId}) (version=${info.bundleVersion})
                                    <a href="<portlet:renderURL><portlet:param name='page' value='view_manifest'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_mf.png" alt="icon_mf.png" title="View Manifest" style="vertical-align:middle"/></a>
                                    <a href="<portlet:renderURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_wb.png" alt="icon_wb.png" title="View Wired Bundles" style="vertical-align:middle"/></a>
                                    <a href="<portlet:renderURL><portlet:param name='page' value='view_services'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_serv.png" alt="icon_serv.png" title="View Services" style="vertical-align:middle"/></a>
                                    <br/>
                                </c:forEach>
                            </td>
                        </tr>
                        <tr>
                            <td valign="top" width="100px">
                                - importers:
                            </td>
                            <td>
                                <c:forEach var="info" items="${pwb.importBundleInfos}">
                                    ${info.symbolicName} (id=${info.bundleId}) (version=${info.bundleVersion})
                                    <a href="<portlet:renderURL><portlet:param name='page' value='view_manifest'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_mf.png" alt="icon_mf.png" title="View Manifest" style="vertical-align:middle"/></a>
                                    <a href="<portlet:renderURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_wb.png" alt="icon_wb.png" title="View Wired Bundles" style="vertical-align:middle"/></a>
                                    <a href="<portlet:renderURL><portlet:param name='page' value='view_services'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_serv.png" alt="icon_serv.png" title="View Services" style="vertical-align:middle"/></a>
                                    <br/>
                                </c:forEach>
                            </td>
                        </tr>
                    </table>
                </div>
            </td>
        </tr>
    </c:forEach>
</table>
