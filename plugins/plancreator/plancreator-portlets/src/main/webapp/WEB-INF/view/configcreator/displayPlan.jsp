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

<%-- $Rev$ $Date$ --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="plancreator-portlet"/>
<portlet:defineObjects />

<p><fmt:message key="plancreator.deploy.title"/></p>

<p><fmt:message key="plancreator.deploy.desc"/></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>DisplayPlan" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="displayPlan-after" />
    <table border="0">
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
            <input type="submit" value="<fmt:message key="plancreator.deploy.deploy"/>" />
        </td>
      </tr>
    <!-- STATUS FIELD: Deployment Plan -->
      <tr>
        <th valign="top" style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>deploymentPlan"><fmt:message key="plancreator.deploy.plan"/></label>:</div></th>
        <td><textarea rows="30" cols="60" name="deploymentPlanXml" id="<portlet:namespace/>deploymentPlan">${data.deploymentPlan}</textarea></td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
            <input type="submit" value="<fmt:message key="plancreator.deploy.deploy"/>" />
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>"><fmt:message key="plancreator.common.cancel"/></a></p>
