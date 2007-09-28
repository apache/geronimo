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
<c:if test="${statsOn}">
<table width="100%">
  <tr>
    <th width="25%" class="DarkBackground"><STRONG>Statistic</STRONG></th>
    <th class="DarkBackground"><strong>Count</strong></th>
  </tr>
  <tr>
    <td align="right"  class="LightBackground"><strong>Total Request Count</strong></td>
    <td align="center" class="LightBackground">${totalRequestCount}</td>
  </tr>
  <tr>
    <td align="right"  class="MediumBackground"><strong>Total 1xx Responses</strong></td>
    <td align="center" class="MediumBackground">${response1xx}</td>
  </tr>
  <tr>
    <td align="right"  class="LightBackground"><strong>Total 2xx Responses</strong></td>
    <td align="center" class="LightBackground">${response2xx}</td>
  </tr>
  <tr>
    <td align="right"  class="MediumBackground"><strong>Total 3xx Responses</strong></td>
    <td align="center" class="MediumBackground">${response3xx}</td>
  </tr>
  <tr>
    <td align="right"  class="LightBackground"><strong>Total 4xx Responses</strong></td>
    <td align="center" class="LightBackground">${response4xx}</td>
  </tr>
  <tr>
    <td align="right"  class="MediumBackground"><strong>Total 5xx Responses</strong></td>
    <td align="center" class="MediumBackground">${response5xx}</td>
  </tr>
</table>
<table width="100%">
  <tr>
    <th WIDTH="25%" class="DarkBackground"> &nbsp; </th>
    <th class="DarkBackground"><strong>Current</strong></th>
    <th class="DarkBackground"><strong>Low</strong></th>
    <th class="DarkBackground"><strong>High</strong></th>
  </tr>
  <tr>
    <td align="right"  class="LightBackground"><strong>Active Request Count</strong></td>
    <td align="center" class="LightBackground">${activeRequestCountCurrent}</td>
    <td align="center" class="LightBackground">${activeRequestCountLow}</td>
    <td align="center" class="LightBackground">${activeRequestCountHigh}</td>
  </tr>
</table>
<table width="100%">
  <tr>
    <th width="25%" class="DarkBackground"> &nbsp; </th>
    <th class="DarkBackground"><strong>Avg Time (ms) </strong></th>
    <th class="DarkBackground"><strong>Min Time (ms)</strong></th>
    <th class="DarkBackground"><strong>Max Time (ms)</strong></th>
    <th class="DarkBackground"><strong>Total Time (ms)</strong></th>
  </tr>
  <tr>
    <td align="right"  class="LightBackground"><strong>Request Duration</strong></td>
    <td align="center" class="LightBackground">${requestDurationAvg}</td>
    <td align="center" class="LightBackground">${requestDurationMinTime}</td>
    <td align="center" class="LightBackground">${requestDurationMaxTime}</td>
    <td align="center" class="LightBackground">${requestDurationTotalTime}</td>
  </tr>
  <tr><td colspan="5" align="left">&nbsp;</td></tr>
  <tr>
    <td colspan="5" align="left"><strong>Elapsed Time Since Collection Started (hh:mm:ss:SSS):</strong>&nbsp;${elapsedTime}</td>
  </tr>
  <tr><td colspan="5" align="left">&nbsp;</td></tr>
  <tr>
    <td colspan="5" align="left"> 
      <a href="<portlet:actionURL><portlet:param name="stats" value="false"/></portlet:actionURL>">disable</a>&nbsp; 
      <a href="<portlet:renderURL/>">refresh</a>&nbsp; 
      <a href="<portlet:actionURL><portlet:param name="resetStats" value="true"/></portlet:actionURL>">reset</a> 
    </td>
  </tr>
</table>
</c:if>
<c:if test="${!statsOn}">
  ${statsMessage}<br/><br/>
  <c:if test="${statsSupported}">
    <a href="<portlet:actionURL><portlet:param name="stats" value="true"/></portlet:actionURL>">enable</a>
  </c:if>
</c:if>
