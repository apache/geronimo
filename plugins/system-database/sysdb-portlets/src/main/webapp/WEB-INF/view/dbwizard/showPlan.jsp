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
<fmt:setBundle basename="systemdatabase"/>
<portlet:defineObjects/>

<p><fmt:message key="dbwizard.showPlan.title"/></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>DatabaseForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="save" />
    <input type="hidden" name="user" value="${pool.user}" />
    <input type="hidden" name="name" value="${pool.name}" />
    <input type="hidden" name="dbtype" value="${pool.dbtype}" />
    <input type="hidden" name="password" value="${pool.password}" />
    <input type="hidden" name="driverClass" value="${pool.driverClass}" />
    <input type="hidden" name="url" value="${pool.url}" />
    <input type="hidden" name="urlPrototype" value="${pool.urlPrototype}" />
    <c:forEach var="jar" items="${pool.jars}">
     <input type="hidden" name="jars" value="${jar}" />
    </c:forEach>    <input type="hidden" name="adapterDisplayName" value="${pool.adapterDisplayName}" />
    <input type="hidden" name="minSize" value="${pool.minSize}" />
    <input type="hidden" name="maxSize" value="${pool.maxSize}" />
    <input type="hidden" name="idleTimeout" value="${pool.idleTimeout}" />
    <input type="hidden" name="blockingTimeout" value="${pool.blockingTimeout}" />
    <input type="hidden" name="adapterDisplayName" value="${pool.adapterDisplayName}" />
    <input type="hidden" name="adapterDescription" value="${pool.adapterDescription}" />
    <input type="hidden" name="rarPath" value="${pool.rarPath}" />
    <input type="hidden" name="transactionType" value="${pool.transactionType}" />
  <c:forEach var="prop" items="${pool.properties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
  <c:forEach var="prop" items="${pool.urlProperties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
    <table border="0">
    <!-- STATUS FIELD: Deployment Plan -->
      <tr>
        <th valign="top" style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>deploymentPlan"><fmt:message key="dbwizard.showPlan.deploymentPlan"/></label>:</div></th>
        <td><textarea rows="30" cols="60" id="<portlet:namespace/>deploymentPlan" readonly>${deploymentPlan}</textarea></td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
<input type="submit" value='<fmt:message key="dbwizard.showPlan.deployPool"/>' />
<input type="button" value='<fmt:message key="dbwizard.common.editSettings"/>' onclick="document.<portlet:namespace/>DatabaseForm.mode.value='edit';document.<portlet:namespace/>DatabaseForm.submit();return false;" />
        </td>
      </tr>
    <!-- STATUS FIELD: Command-line guidance -->
      <tr>
        <th valign="top"><div align="right"><fmt:message key="dbwizard.showPlan.deployCommand"/>:</div></th>
        <td><fmt:message key="dbwizard.showPlan.deployPoolFromCommandLine"/>
<pre>
cd GERONIMO_HOME
java -jar bin/deployer.jar deploy plan-file.xml \
        ${rarRelativePath}
</pre></td>
      </tr>
    <!-- STATUS FIELD: Embed in EAR guidance -->
      <tr>
        <th valign="top"><div align="right"><fmt:message key="dbwizard.showPlan.addToEAR"/>:</div></th>
        <td>
        <fmt:message key="dbwizard.showPlan.deployAsPartEAR">
        <fmt:param value="${rarRelativePath}" />
        </fmt:message>
<pre>
&lt;application
   xmlns="http://geronimo.apache.org/xml/ns/j2ee/application-1.1"&gt;
  &lt;environment&gt;
    &lt;moduleId&gt;
      &lt;artifactId&gt;MyApplication&lt;/artifactId&gt;
    &lt;/moduleId&gt;
  &lt;/environment&gt;
  &lt;module&gt;
    &lt;connector&gt;rar-file-name.rar&lt;/connector&gt;
    &lt;alt-dd&gt;plan-file-name.xml&lt;/alt-dd&gt;
  &lt;/module&gt;
&lt;/application&gt;
</pre></td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>"><fmt:message key="dbwizard.common.cancel"/></a></p>
