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
<fmt:setBundle basename="activemq"/>

<p><fmt:message key="jmsmanager.activemq.help.title" /></p>

<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><span style="font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;"><fmt:message key="jmsmanager.common.detail"/></span></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top"><fmt:message key="jmsmanager.activemq.help.detailExp" /></td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><strong><fmt:message key="jmsmanager.common.name"/></strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top"><fmt:message key="jmsmanager.activemq.help.nameExp" /></td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><strong><fmt:message key="jmsmanager.common.state"/></strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top"><fmt:message key="jmsmanager.activemq.help.stateExp" /></td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><span style="font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;"><fmt:message key="jmsmanager.common.testConn"/></span></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top"><fmt:message key="jmsmanager.activemq.help.testConnExp" /></td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><span style="font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;"><fmt:message key="jmsmanager.activemq.common.addNewJMSConnFactory" /></span></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top"><fmt:message key="jmsmanager.activemq.help.addNewJMSConnFactoryExp" /></td>
  </tr>
</table>
