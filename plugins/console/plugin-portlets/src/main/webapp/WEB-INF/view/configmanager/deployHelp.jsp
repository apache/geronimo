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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="pluginportlets"/>
<portlet:defineObjects/>
<p>
<fmt:message key="configmanager.deployHelp.title" />
</p>
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><strong><fmt:message key="configmanager.common.archive" /></strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px"><fmt:message key="configmanager.deployHelp.archiveExp" /> </td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><strong><fmt:message key="configmanager.common.plan" /></strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px"><fmt:message key="configmanager.deployHelp.planExp" /></td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;"> <fmt:message key="configmanager.common.startAppAfterInstall" /></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px"><fmt:message key="configmanager.deployHelp.startAppAfterInstallExp" /></td>
  </tr>
 <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;"> <fmt:message key="configmanager.common.redeployapplication" /></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px"><fmt:message key="configmanager.deployHelp.redeployapplication" /></td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><img src="/console/images/install.gif" alt="Install"/></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px"><fmt:message key="configmanager.deployHelp.installApplication" /></td>
  </tr>
</table>
