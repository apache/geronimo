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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<input type="hidden" name="mode" value="results-after"/>
<input type="hidden" name="os" value="${model.os}"/>
<input type="hidden" name="addAjpPort" value="${model.addAjpPort}"/>
<input type="hidden" name="workersPath" value="${model.workersPath}"/>
<input type="hidden" name="logFilePath" value="${model.logFilePath}"/>
<p><fmt:message key="apache.jk.results.title"/></p>

<p><fmt:message key="apache.jk.results.summary"/></p>

<h2><fmt:message key="apache.jk.results.step1"/></h2>


<c:choose>
    <c:when test="${empty model.addAjpPort}">
        <p><fmt:message key="apache.jk.results.alreadyHasAJPListener"><fmt:param value="${ajpPort}"/></fmt:message>
        </p>
    </c:when>
    <c:otherwise>
        <p><fmt:message key="apache.jk.results.anAJPLisstnerAdded"><fmt:param value="${ajpPort}"/></fmt:message>
        </p>
    </c:otherwise>
</c:choose>
<h2><fmt:message key="apache.jk.results.step2"/></h2>
<c:choose>
    <c:when test="${model.os != '' && model.os != 'Other'}">
        <p>
    <fmt:message key="apache.jk.results.step2_installUsingRPM">
    <fmt:param value="${model.os}" />
    </fmt:message>
    </p>
        <pre>
rpm -Uvh <c:choose><c:when test="${model.os == 'Fedora Core 4'}">mod_jk-1.2.6-3jpp_4fc.i586.rpm</c:when><c:when test="${model.os == 'SuSE Pro 9.0'}">apache2-jakarta-tomcat-connectors-4.1.27-32.i586.rpm</c:when><c:when test="${model.os == 'SuSE Pro 9.1'}">apache2-jakarta-tomcat-connectors-5.0.19-13.i586.rpm</c:when><c:when test="${model.os == 'SuSE Pro 9.2'}">mod_jk-ap20-4.1.30-3.i586.rpm</c:when><c:when test="${model.os == 'SuSE Pro 9.3'}">mod_jk-ap20-4.1.30-3.i586.rpm</c:when><c:when test="${model.os == 'SuSE Linux 10.0'}">mod_jk-ap20-4.1.30-3.i586.rpm</c:when></c:choose>
        </pre>
        <p><fmt:message key="apache.jk.results.canActivateIt"/>        
   <c:choose>
     <c:when test="${fn:startsWith(model.os, 'SuSE')}">
      <fmt:message key="apache.jk.results.howToActivateifOsSuSE"/>
     </c:when>
     <c:otherwise>
     <fmt:message key="apache.jk.results.howToActivateOthers"/>
     </c:otherwise>
   </c:choose>
        </p>
    </c:when><c:otherwise>
    <fmt:message key="apache.jk.results.noInstructionsToInstall"/>
    </c:otherwise>
</c:choose>
<h2><fmt:message key="apache.jk.results.step3"/></h2>
<p>
<fmt:message key="apache.jk.results.saveFollowingToFile">
    <fmt:param value="${model.workersPath}" />
    </fmt:message>
</p>
<pre>
worker.list=geronimo_ajp13
worker.geronimo_ajp13.port=${ajpPort}
worker.geronimo_ajp13.host=localhost
worker.geronimo_ajp13.type=ajp13
</pre>
<h2><fmt:message key="apache.jk.results.step4"/></h2>

<p><fmt:message key="apache.jk.results.followingInfoToConfig"/></p>


<c:choose>
  <c:when test="${fn:startsWith(model.os, 'SuSE')}">
  <p><fmt:message key="apache.jk.results.saveWhereIfSuSE"/></p>
  </c:when>
  <c:otherwise>
  <p><fmt:message key="apache.jk.results.saveWhereOthwise"/></p>
  </c:otherwise>
</c:choose>

<pre>
&lt;IfModule mod_jk.c&gt;
    JkWorkersFile ${model.workersPath}
    JkLogFile ${model.logFilePath}
    JkLogLevel error
<c:forEach var="web" items="${model.webApps}"><c:if test="${web.enabled}">
    JkMount ${web.contextRoot}/* geronimo_ajp13<c:if test="${web.serveStaticContent}">
    Alias ${web.contextRoot} "${web.webAppDir}"
    &lt;Directory "${web.webAppDir}"&gt;
        Options Indexes FollowSymLinks
        allow from all
    &lt;/Directory&gt;
    &lt;Location "${web.contextRoot}/WEB-INF/"&gt;
        AllowOverride None
        deny from all
    &lt;/Location&gt;
</c:if></c:if></c:forEach>
&lt;/IfModule&gt;
</pre>
<h2><fmt:message key="apache.jk.results.step5"/></h2>
<p>
<fmt:message key="apache.jk.results.step5Content">
<fmt:param  value="${model.logFilePath}" />
</fmt:message>
</p>
<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>"><fmt:message key="consolebase.common.done"/></a></p>
