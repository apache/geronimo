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
<fmt:setBundle basename="activemq"/>
<portlet:defineObjects/>

<p><fmt:message key="jmswizard.plan.title" /></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>JMSForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="plan-after" />
    <input type="hidden" name="rar" value="${data.rarURI}" />
    <input type="hidden" name="dependency" value="${data.dependency}" />
    <input type="hidden" name="instanceName" value="${data.instanceName}" />
    <input type="hidden" name="workManager" value="${data.workManager}" /> <%-- todo: pick list for WorkManager --%>
    <c:forEach var="prop" items="${data.instanceProps}">
      <input type="hidden" name="${prop.key}" value="${prop.value}" />
    </c:forEach>
    <input type="hidden" name="currentFactoryID" value="${data.currentFactoryID}" />
    <input type="hidden" name="currentDestinationID" value="${data.currentDestinationID}" />
    <input type="hidden" name="factoryType" value="${data.factoryType}" />
    <input type="hidden" name="destinationType" value="${data.destinationType}" />
    <c:forEach var="factory" items="${data.connectionFactories}" varStatus="status">
      <input type="hidden" name="factory.${status.index}.factoryType" value="${factory.factoryType}" />
      <input type="hidden" name="factory.${status.index}.instanceName" value="${factory.instanceName}" />
      <input type="hidden" name="factory.${status.index}.transaction" value="${factory.transaction}" />
      <input type="hidden" name="factory.${status.index}.xaTransaction" value="${factory.xaTransactionCaching}" />
      <input type="hidden" name="factory.${status.index}.xaThread" value="${factory.xaThreadCaching}" />
      <input type="hidden" name="factory.${status.index}.poolMinSize" value="${factory.poolMinSize}" />
      <input type="hidden" name="factory.${status.index}.poolMaxSize" value="${factory.poolMaxSize}" />
      <input type="hidden" name="factory.${status.index}.poolIdleTimeout" value="${factory.poolIdleTimeout}" />
      <input type="hidden" name="factory.${status.index}.poolBlockingTimeout" value="${factory.poolBlockingTimeout}" />
      <c:forEach var="prop" items="${factory.instanceProps}">
        <input type="hidden" name="factory.${status.index}.${prop.key}" value="${prop.value}" />
      </c:forEach>
    </c:forEach>
    <c:forEach var="dest" items="${data.adminObjects}" varStatus="status">
      <input type="hidden" name="destination.${status.index}.destinationType" value="${dest.destinationType}" />
      <input type="hidden" name="destination.${status.index}.name" value="${dest.name}" />
      <c:forEach var="prop" items="${dest.instanceProps}">
        <input type="hidden" name="destination.${status.index}.${prop.key}" value="${prop.value}" />
      </c:forEach>
    </c:forEach>

    <table border="0">
    <!-- STATUS FIELD: Deployment Plan -->
      <tr>
        <th valign="top" style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>deploymentPlan"><fmt:message key="jmswizard.plan.deploymentPlan" />:</label></div></th>
        <td><textarea rows="40" cols="120" id="<portlet:namespace/>deploymentPlan" readonly>${deploymentPlan}</textarea></td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
            <input type="hidden" name="nextAction" value="deploy" />
            <input type="button" value='<fmt:message key="jmswizard.plan.edit" />' onclick="document.<portlet:namespace/>JMSForm.nextAction.value='review';document.<portlet:namespace/>JMSForm.submit();return false;" />
            <input type="submit" value='<fmt:message key="jmswizard.plan.deployJMSResource" />' />
        </td>
      </tr>
    <!-- STATUS FIELD: Command-line guidance -->
      <tr>
        <th valign="top"><div align="right"><fmt:message key="jmswizard.plan.deployCommand" />:</div></th>
        <td> <fmt:message key="jmswizard.plan.deployCommandExp" /><br />
<pre>
cd GERONIMO_HOME
java -jar bin/deployer.jar deploy plan-file.xml \
        ${rarURL}
</pre></td>
      </tr>
    <!-- STATUS FIELD: Embed in EAR guidance -->
      <tr>
        <th valign="top"><div align="right"><fmt:message key="jmswizard.plan.addToEAR" />:</div></th>
        <td><fmt:message key="jmswizard.plan.addToEARExp">
        <fmt:param value="${rarURL}"/>
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
            </portlet:actionURL>"><fmt:message key="jmswizard.common.cancel"/></a></p>
