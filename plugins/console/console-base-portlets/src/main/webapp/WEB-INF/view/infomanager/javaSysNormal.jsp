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

<b>Java:</b>
<table width="100%" class="TableLine" summary="java">
  <tr>
    <th scope="col" class="DarkBackground" width="20%" align="center"><fmt:message key="consolebase.common.item"/></th>
    <th scope="col" class="DarkBackground" width="80%" align="center"><fmt:message key="consolebase.common.value"/></th>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap>java.awt.graphicsenv</td>
    <td class="LightBackground" width="80%">${javaSysProps['java.awt.graphicsenv']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.awt.printerjob</td>
    <td class="MediumBackground">${javaSysProps['java.awt.printerjob']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.class.path</td>
    <td class="LightBackground">
        <table>
        <c:forEach var="el" items="${javaSysProps['java.class.path']}">
            <tr><td class="LightBackground">${el}</td></tr>
        </c:forEach>
        </table>
    </td>
  </tr>
  <tr>
    <td class="MediumBackground">java.class.version</td>
    <td class="MediumBackground">${javaSysProps['java.class.version']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.endorsed.dirs</td>
    <td class="LightBackground">
        <table>
        <c:forEach var="el" items="${javaSysProps['java.endorsed.dirs']}">
            <tr><td class="LightBackground">${el}</td></tr>
        </c:forEach>
        </table>
    </td>
  </tr>
  <tr>
    <td class="MediumBackground">java.ext.dirs</td>
    <td class="MediumBackground">
        <table>
        <c:forEach var="el" items="${javaSysProps['java.ext.dirs']}">
            <tr><td class="MediumBackground">${el}</td></tr>
        </c:forEach>
        </table>
    </td>
  </tr>
  <tr>
    <td class="LightBackground">java.home</td>
    <td class="LightBackground">${javaSysProps['java.home']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.io.tmpdir</td>
    <td class="MediumBackground">${javaSysProps['java.io.tmpdir']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.library.path</td>
    <td class="LightBackground">
        <table>
        <c:forEach var="el" items="${javaSysProps['java.library.path']}">
            <tr><td class="LightBackground">${el}</td></tr>
        </c:forEach>
        </table>
    </td>
  </tr>
  <tr>
    <td class="MediumBackground">java.runtime.name</td>
    <td class="MediumBackground">${javaSysProps['java.runtime.name']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.runtime.version</td>
    <td class="LightBackground">${javaSysProps['java.runtime.version']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.specification.name</td>
    <td class="MediumBackground">${javaSysProps['java.specification.name']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.specification.vendor</td>
    <td class="LightBackground">${javaSysProps['java.specification.vendor']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.specification.version</td>
    <td class="MediumBackground">${javaSysProps['java.specification.version']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.util.prefs.PreferencesFactory</td>
    <td class="LightBackground">${javaSysProps['java.util.prefs.PreferencesFactory']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.vendor-</td>
    <td class="MediumBackground">${javaSysProps['java.vendor']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.vendor.url</td>
    <td class="LightBackground">${javaSysProps['java.vendor.url']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.vendor.url.bug</td>
    <td class="MediumBackground">${javaSysProps['java.vendor.url.bug']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.version-</td>
    <td class="LightBackground">${javaSysProps['java.version']}</td>
  </tr>
</table>
<br/>

<b><fmt:message key="infomanager.javaSysNormal.virtualMachine"/>:</b>
<table width="100%" class="TableLine" summary="Virtual Machine">
  <tr>
    <th scope="col" class="DarkBackground" width="20%" align="center"><fmt:message key="consolebase.common.item"/></th>
    <th scope="col" class="DarkBackground" width="80%" align="center"><fmt:message key="consolebase.common.value"/></th>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap>java.vm.info</td>
    <td class="LightBackground" width="80%">${javaSysProps['java.vm.info']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.vm.name</td>
    <td class="MediumBackground">${javaSysProps['java.vm.name']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.vm.specification.name</td>
    <td class="LightBackground">${javaSysProps['java.vm.specification.name']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.vm.specification.vendor</td>
    <td class="MediumBackground">${javaSysProps['java.vm.specification.vendor']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.vm.specification.version</td>
    <td class="LightBackground">${javaSysProps['java.vm.specification.version']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.vm.vendor</td>
    <td class="MediumBackground">${javaSysProps['java.vm.vendor']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.vm.version</td>
    <td class="LightBackground">${javaSysProps['java.vm.version']}</td>
  </tr>
</table>
<br/>

<b><fmt:message key="infomanager.javaSysNormal.operatingSystem"/>:</b>
<table width="100%" class="TableLine" summary="Operating System">
  <tr>
    <th scope="col" class="DarkBackground" width="20%" align="center"><fmt:message key="consolebase.common.item"/></th>
    <th scope="col" class="DarkBackground" width="80%" align="center"><fmt:message key="consolebase.common.value"/></th>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap>os.arch</td>
    <td class="LightBackground" width="80%">${javaSysProps['os.arch']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">os.name</td>
    <td class="MediumBackground">${javaSysProps['os.name']}</td>
  </tr>
  <tr>
    <td class="LightBackground">os.version</td>
    <td class="LightBackground">${javaSysProps['os.version']}</td>
  </tr>
</table>
<br/>

<b>Sun:</b>
<table width="100%" class="TableLine" summary="Sun">
  <tr>
    <th scope="col" class="DarkBackground" width="20%" align="center"><fmt:message key="consolebase.common.item"/></th>
    <th scope="col" class="DarkBackground" width="80%" align="center"><fmt:message key="consolebase.common.value"/></th>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap>sun.arch.data.model</td>
    <td class="LightBackground" width="80%">${javaSysProps['sun.arch.data.model']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">sun.boot.class.path</td>
    <td class="MediumBackground">

        <table>
        <c:forEach var="el" items="${javaSysProps['sun.boot.class.path']}">
            <tr><td class="MediumBackground">${el}</td></tr>
        </c:forEach>
        </table>

    </td>
  </tr>
  <tr>
    <td class="LightBackground">sun.boot.library.path</td>
    <td class="LightBackground">
        <table>
        <c:forEach var="el" items="${javaSysProps['sun.boot.library.path']}">
            <tr><td class="LightBackground">${el}</td></tr>
        </c:forEach>
        </table>

    </td>
  </tr>
  <tr>
    <td class="MediumBackground">sun.cpu.endian</td>
    <td class="MediumBackground">${javaSysProps['sun.cpu.endian']}</td>
  </tr>
  <tr>
    <td class="LightBackground">sun.cpu.isalist</td>
    <td class="LightBackground">${javaSysProps['sun.cpu.isalist']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">sun.io.unicode.encoding</td>
    <td class="MediumBackground">${javaSysProps['sun.io.unicode.encoding']}</td>
  </tr>
  <tr>
    <td class="LightBackground">sun.java2d.fontpath</td>
    <td class="LightBackground">${javaSysProps['sun.java2d.fontpath']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">sun.os.patch.level</td>
    <td class="MediumBackground">${javaSysProps['sun.os.patch.level']}</td>
  </tr>
</table>
<br/>

<b><fmt:message key="infomanager.javaSysNormal.user"/>:</b>
<table width="100%" class="TableLine" summary="User">
  <tr>
    <th scope="col" class="DarkBackground" width="20%" align="center"><fmt:message key="consolebase.common.item"/></th>
    <th scope="col" class="DarkBackground" width="80%" align="center"><fmt:message key="consolebase.common.value"/></th>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap>user.country</td>
    <td class="LightBackground" width="80%">${javaSysProps['user.country']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">user.dir</td>
    <td class="MediumBackground">${javaSysProps['user.dir']}</td>
  </tr>
  <tr>
    <td class="LightBackground">user.home</td>
    <td class="LightBackground">${javaSysProps['user.home']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">user.language</td>
    <td class="MediumBackground">${javaSysProps['user.language']}</td>
  </tr>
  <tr>
    <td class="LightBackground">user.name</td>
    <td class="LightBackground">${javaSysProps['user.name']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">user.timezone</td>
    <td class="MediumBackground">${javaSysProps['user.timezone']}</td>
  </tr>
  <tr>
    <td class="LightBackground">user.variant</td>
    <td class="LightBackground">${javaSysProps['user.variant']}</td>
  </tr>
</table>
<br/>

<b><fmt:message key="infomanager.javaSysNormal.etc"/>:</b>
<table width="100%" class="TableLine" summary="Etc">
  <tr>
    <th scope="col" class="DarkBackground" width="20%" align="center"><fmt:message key="consolebase.common.item"/></th>
    <th scope="col" class="DarkBackground" width="80%" align="center"><fmt:message key="consolebase.common.value"/></th>
  </tr>
<% String background = "LightBackground"; %>
<%  // Crappy workaround because apparently Jetty's JSTL can't call getters on a Map subclass?!?
    // Why doesn't ${javaSysProps.remainingItems} return the results of getRemainingItems()?
    java.util.Map map = ((org.apache.geronimo.console.infomanager.ShrinkingMap)request.getAttribute("javaSysProps")).getRemainingItems();
    request.setAttribute("results", map); %>
<c:forEach var="entry" items="${results}">
  <tr>
    <td class="<%=background%>">${entry.key}</td>
    <td class="<%=background%>">${entry.value}</td>
    <% background = background.equals("MediumBackground") ? "LightBackground" : "MediumBackground"; %>
  </tr>
</c:forEach>
</table>
