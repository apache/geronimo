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

<a href="<portlet:actionURL/>" >OSGi Manager</a> > Show Bundle Manifest
<br/><br/>
<table width="100%" class="BlankTableLine" summary="OSGi install">
    <tr>
        <td>
            The Manifest of Bundle:
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
                <a href="<portlet:renderURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${bundleInfo.bundleId}'/></portlet:renderURL>">Wired Bundles</a>
                &nbsp;|&nbsp;
                <a href="<portlet:renderURL><portlet:param name='page' value='view_services'/><portlet:param name='bundleId' value='${bundleInfo.bundleId}'/></portlet:renderURL>">Services</a>
            </c:if>
        </td>
    </tr>
</table>
<br/>
<table width="100%" class="TableLine" summary="OSGi Manifest">
    <tr class="DarkBackground">
        <th scope="col" width="20%">Header</th>   
        <th scope="col" width="80%">Value</th> 
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="manifestHeader" items="${manifestHeaders}">
      <c:choose>
          <c:when test="${backgroundClass == 'MediumBackground'}" >
              <c:set var="backgroundClass" value='LightBackground'/>
          </c:when>
          <c:otherwise>
              <c:set var="backgroundClass" value='MediumBackground'/>
          </c:otherwise>
      </c:choose>
      <tr>
        <!-- bundle id -->
        <td class="${backgroundClass}">${manifestHeader.key}</td>
        <td class="${backgroundClass}">${manifestHeader.value}</td>
      </tr>
    </c:forEach>
</table>