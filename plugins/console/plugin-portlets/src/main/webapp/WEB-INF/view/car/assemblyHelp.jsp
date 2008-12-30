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

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="pluginportlets"/>

<portlet:defineObjects/>


<%--<fmt:message key="car.index.assembleServer" />--%>
<p><fmt:message key="car.index.assemblyHelp.desp" /></p>

<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="180" align="left" valign="top" class="MediumBackground"><strong><fmt:message key="car.index.assemblyHelp.FuncCentric.title" /></strong></td>
    <td class="LightBackground"><fmt:message key="car.index.assemblyHelp.FuncCentric.desp" /></td>
  </tr>
  <tr>
    <td width="180" align="left" valign="top" class="MediumBackground"><strong><fmt:message key="car.index.assemblyHelp.AppCentric.title" /></strong></td>
    <td class="LightBackground"><fmt:message key="car.index.assemblyHelp.AppCentric.desp" /></td>
  </tr>
  <tr>
    <td width="180" align="left" valign="top" class="MediumBackground"><strong><fmt:message key="car.index.assemblyHelp.ExpertUser.title" /></strong></td>
    <td class="LightBackground"><fmt:message key="car.index.assemblyHelp.ExpertUser.desp" /></td>
  </tr>
</table>

<p><fmt:message key="car.index.assemblyHelp.note" /></p>

<p><fmt:message key="car.help.return" /></p>