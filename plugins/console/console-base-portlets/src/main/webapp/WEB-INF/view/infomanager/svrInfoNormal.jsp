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
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<script type='text/javascript' src='/console/dwr/interface/Jsr77Lookup.js'></script>
<script type='text/javascript' src='/console/dwr/engine.js'></script>
<script type='text/javascript' src='/console/dwr/util.js'></script>

<b><fmt:message key="infomanager.svrInfoNormal.server"/>:</b>
<table width="100%" class="TableLine" summary="Server">
  <tr>
    <th scope="col" class="DarkBackground" width="20%" align="center"><fmt:message key="consolebase.common.item"/></th>
    <th scope="col" class="DarkBackground" width="80%" align="center"><fmt:message key="consolebase.common.value"/></th>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap><fmt:message key="infomanager.svrInfoNormal.version"/></td>
    <td class="LightBackground" width="80%">${svrProps['Geronimo Version']}</td>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap><fmt:message key="infomanager.svrInfoNormal.build"/></td>
    <td class="LightBackground" width="80%">${svrProps['Build']}</td>
  </tr>
  <tr>
    <td class="MediumBackground" width="20%" nowrap><fmt:message key="infomanager.svrInfoNormal.startTime"/></td>
    <td class="MediumBackground" width="80%">${svrProps['Kernel Boot Time']}</td>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap><fmt:message key="infomanager.svrInfoNormal.upTime"/></td>
    <td class="LightBackground"><div id="<portlet:namespace/>UpTime"><fmt:message key="infomanager.svrInfoNormal.notAvailable"/></div></td>
  </tr>
</table>
<br/>

<b><fmt:message key="infomanager.svrInfoNormal.os"/>:</b>
<table width="100%" class="TableLine" summary="OS">
  <tr>
    <th scope="col" class="DarkBackground" width="20%" align="center"><fmt:message key="consolebase.common.item"/></th>
    <th scope="col" class="DarkBackground" width="80%" align="center"><fmt:message key="consolebase.common.value"/></th>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap><fmt:message key="infomanager.svrInfoNormal.os.arch"/></td>
    <td class="LightBackground" width="80%">${svrProps['os.arch']}</td>
  </tr>
  <tr>
    <td class="MediumBackground" width="20%" nowrap><fmt:message key="infomanager.svrInfoNormal.os.name"/></td>
    <td class="MediumBackground" width="80%">${svrProps['os.name']}</td>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap><fmt:message key="infomanager.svrInfoNormal.os.version"/></td>
    <td class="LightBackground" width="80%">${svrProps['os.version']}</td>
  </tr>
  <tr>
    <td class="MediumBackground" width="20%" nowrap><fmt:message key="infomanager.svrInfoNormal.os.patchlevel"/></td>
    <td class="MediumBackground" width="80%">${svrProps['sun.os.patch.level']}</td>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap><fmt:message key="infomanager.svrInfoNormal.os.locale"/></td>
    <td class="LightBackground" width="80%">${svrProps['os.locale']}</td>
  </tr>
</table>
<br/>

<b><fmt:message key="infomanager.svrInfoNormal.jvm"/>:</b>
<table width="100%" class="TableLine" summary="JVM">
  <tr>
    <th scope="col" class="DarkBackground" width="20%" align="center"><fmt:message key="consolebase.common.item"/></th>
    <th scope="col" class="DarkBackground" width="80%" align="center"><fmt:message key="consolebase.common.value"/></th>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap><fmt:message key="infomanager.svrInfoNormal.javaVersion"/></td>
    <td class="LightBackground" width="80%">${jvmProps['Java Version']}</td>
  </tr>
  <tr>
    <td class="MediumBackground"><fmt:message key="infomanager.svrInfoNormal.javaVendor"/></td>
    <td class="MediumBackground">${jvmProps['Java Vendor']}</td>
  </tr>
  <tr>
    <td class="LightBackground"><fmt:message key="infomanager.svrInfoNormal.node"/></td>
    <td class="LightBackground">${jvmProps['Node']}</td>
  </tr>
  <tr>
    <td class="MediumBackground"><fmt:message key="infomanager.svrInfoNormal.currentMemoryUsed"/></td>
    <td class="MediumBackground"><div id="<portlet:namespace/>CurrentMemory"><fmt:message key="infomanager.svrInfoNormal.notAvailable"/></div></td>
  </tr>
  <tr>
    <td class="LightBackground"><fmt:message key="infomanager.svrInfoNormal.mostMemoryUsed"/></td>
    <td class="LightBackground"><div id="<portlet:namespace/>MostMemory"><fmt:message key="infomanager.svrInfoNormal.notAvailable"/></div></td>
  </tr>
  <tr>
    <td class="MediumBackground"><fmt:message key="infomanager.svrInfoNormal.totalMemoryAllocated"/></td>
    <td class="MediumBackground"><div id="<portlet:namespace/>AvailableMemory"><fmt:message key="infomanager.svrInfoNormal.notAvailable"/></div></td>
  </tr>
  <tr>
    <td class="LightBackground"><fmt:message key="infomanager.svrInfoNormal.availableProcessors"/></td>
    <td class="LightBackground">${jvmProps['Available Processors']}</td>
  </tr>
  <tr>
    <td colspan="2" align="center"><div id="<portlet:namespace/>ErrorArea"></div></td>
  </tr>
</table>

<script>
dwr.engine.setErrorHandler(null);
<portlet:namespace/>stopped=false;
function <portlet:namespace/>callServer() {
    metadata = {};
    metadata.callback=<portlet:namespace/>updateValues;
    metadata.errorHandler=<portlet:namespace/>onError;
    Jsr77Lookup.getJavaVMStatistics(metadata);
}
function <portlet:namespace/>updateValues(serverStats) {
    dwr.util.setValue("<portlet:namespace/>CurrentMemory", serverStats.memoryCurrent);
    dwr.util.setValue("<portlet:namespace/>MostMemory", serverStats.memoryMost);
    dwr.util.setValue("<portlet:namespace/>AvailableMemory", serverStats.memoryAllocated);
    dwr.util.setValue("<portlet:namespace/>UpTime", serverStats.upTime);
    if(!<portlet:namespace/>stopped) {
        setTimeout("<portlet:namespace/>callServer()", 5000);
    }
}
function <portlet:namespace/>onError() {
    <portlet:namespace/>stopped=true;
    dwr.util.setValue("<portlet:namespace/>ErrorArea", '<form name="<portlet:namespace/>Refresh" action="<portlet:actionURL/>" method="POST"><input type="submit" value="<fmt:message key="consolebase.common.refresh"/>"/></form>',{escapeHtml:false});
}
<portlet:namespace/>callServer();
</script>
