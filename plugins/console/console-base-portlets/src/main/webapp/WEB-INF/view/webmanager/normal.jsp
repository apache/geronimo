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

<CommonMsg:commonMsg/><br>

<c:if test="${statsOn}">
<table width="100%" class="TableLine" summary="Statistic">
  <tr>
    <th scope="col" width="25%" class="DarkBackground"><STRONG><fmt:message key="webmanager.normal.statistic"/></STRONG></th>
    <th scope="col" class="DarkBackground"><strong><fmt:message key="webmanager.normal.count"/></strong></th>
  </tr>
  <tr>
    <td align="right"  class="LightBackground"><strong><fmt:message key="webmanager.normal.totalRequestCount"/></strong></td>
    <td align="center" class="LightBackground">${totalRequestCount}</td>
  </tr>
  <tr>
    <td align="right"  class="MediumBackground"><strong><fmt:message key="webmanager.normal.total1xxResp"/></strong></td>
    <td align="center" class="MediumBackground">${response1xx}</td>
  </tr>
  <tr>
    <td align="right"  class="LightBackground"><strong><fmt:message key="webmanager.normal.total2xxResp"/></strong></td>
    <td align="center" class="LightBackground">${response2xx}</td>
  </tr>
  <tr>
    <td align="right"  class="MediumBackground"><strong><fmt:message key="webmanager.normal.total3xxResp"/></strong></td>
    <td align="center" class="MediumBackground">${response3xx}</td>
  </tr>
  <tr>
    <td align="right"  class="LightBackground"><strong><fmt:message key="webmanager.normal.total4xxResp"/></strong></td>
    <td align="center" class="LightBackground">${response4xx}</td>
  </tr>
  <tr>
    <td align="right"  class="MediumBackground"><strong><fmt:message key="webmanager.normal.total5xxResp"/></strong></td>
    <td align="center" class="MediumBackground">${response5xx}</td>
  </tr>
</table>
<br/>
<table width="100%" class="TableLine" summary="Current">
  <tr>
    <th scope="col" width="25%" class="DarkBackground"> &nbsp; </th>
    <th scope="col" class="DarkBackground"><strong><fmt:message key="webmanager.normal.current"/></strong></th>
    <th scope="col" class="DarkBackground"><strong><fmt:message key="webmanager.normal.low"/></strong></th>
    <th scope="col" class="DarkBackground"><strong><fmt:message key="webmanager.normal.high"/></strong></th>
  </tr>
  <tr>
    <td align="right"  class="LightBackground"><strong><fmt:message key="webmanager.normal.activeRequestCount"/></strong></td>
    <td align="center" class="LightBackground">${activeRequestCountCurrent}</td>
    <td align="center" class="LightBackground">${activeRequestCountLow}</td>
    <td align="center" class="LightBackground">${activeRequestCountHigh}</td>
  </tr>
</table>
<br/>
<table width="100%" class="TableLine" summary="Time">
  <tr>
    <th scope="col" width="25%" class="DarkBackground"> &nbsp; </th>
    <th scope="col" class="DarkBackground"><strong><fmt:message key="webmanager.normal.avgTime"/></strong></th>
    <th scope="col" class="DarkBackground"><strong><fmt:message key="webmanager.normal.minTime"/></strong></th>
    <th scope="col" class="DarkBackground"><strong><fmt:message key="webmanager.normal.maxTime"/></strong></th>
    <th scope="col" class="DarkBackground"><strong><fmt:message key="webmanager.normal.totalTime"/></strong></th>
  </tr>
  <tr>
    <td align="right"  class="LightBackground"><strong><fmt:message key="webmanager.normal.requestDuration"/></strong></td>
    <td align="center" class="LightBackground">${requestDurationAvg}</td>
    <td align="center" class="LightBackground">${requestDurationMinTime}</td>
    <td align="center" class="LightBackground">${requestDurationMaxTime}</td>
    <td align="center" class="LightBackground">${requestDurationTotalTime}</td>
  </tr>
  <tr><td colspan="5" align="left">&nbsp;</td></tr>
  <tr>
    <td colspan="5" align="left"><strong><fmt:message key="webmanager.normal.elapsedTime"/>:</strong>&nbsp;${elapsedTime}</td>
  </tr>
  <tr><td colspan="5" align="left">&nbsp;</td></tr>
  <tr>
    <td colspan="5" align="left"> 
      <c:if test="${statsLazy}">
        <a href="<portlet:actionURL><portlet:param name="stats" value="false"/></portlet:actionURL>"><fmt:message key="webmanager.help.disable"/></a>&nbsp; 
      </c:if>
      <a href="<portlet:renderURL/>"><fmt:message key="consolebase.common.refresh"/></a>&nbsp; 
      <a href="<portlet:actionURL><portlet:param name="resetStats" value="true"/></portlet:actionURL>"><fmt:message key="consolebase.common.reset"/></a> 
    </td>
  </tr>
</table>
</c:if>
<c:if test="${!statsOn}">
  <c:if test="${statsLazy}">
    <a href="<portlet:actionURL><portlet:param name="stats" value="true"/></portlet:actionURL>"><fmt:message key="consolebase.common.enable"/></a>
  </c:if>
</c:if>
