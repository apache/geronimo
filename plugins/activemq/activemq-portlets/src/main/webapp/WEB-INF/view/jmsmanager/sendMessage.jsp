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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg"%>  
<fmt:setBundle basename="activemq"/> 
<portlet:defineObjects/>

<CommonMsg:commonMsg/><br>

<!-- <p><fmt:message key="jmsmanager.sendmessage.title" /></p> -->

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>JMSForm" action="<portlet:actionURL/>" method="post">
    <input type="hidden" name="mode" value="sendmessage-before" />
    <input type="hidden" name="submit" value="submit" />
    <input type="hidden" name="adapterObjectName" value="${adapterObjectName}" />
    <input type="hidden" name="resourceAdapterModuleName" value="${resourceAdapterModuleName}" />
    <input type="hidden" name="adminObjName" value="${adminObjName}" />
    <input type="hidden" name="adminObjType" value="${adminObjType}" />
    <input type="hidden" name="physicalName" value="${physicalName}" />
    <table border="0">
    <!-- ENTRY FIELD: JMS Connection RAR -->
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>destination"><fmt:message key="jmsmanager.sendmessage.destination" /></label>:</div></th>
        <td>
          <input type="text" name="destination" id="<portlet:namespace/>destination" value="${adminObjName}" readonly="readonly"/>
        </td>
      </tr>
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>correlationId"><fmt:message key="jmsmanager.sendmessage.correlationId" /></label>:</div></th>
        <td>
          <input type="text" name="correlationId" id="<portlet:namespace/>correlationId"/>
        </td>
      </tr>
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>isPersistent"><fmt:message key="jmsmanager.sendmessage.persistence" /></label>:</div></th>
        <td>
          <input type="checkbox" name="isPersistent" id="<portlet:namespace/>isPersistent"/>
        </td>
      </tr>
      
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>priority"><fmt:message key="jmsmanager.sendmessage.priority" /></label>:</div></th>
        <td>
          <input type="text" name="priority" id="<portlet:namespace/>priority"/>
        </td>
      </tr>
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>jmsType"><fmt:message key="jmsmanager.sendmessage.type" /></label>:</div></th>
        <td>
          <input type="text" name="jmsType" id="<portlet:namespace/>jmsType"/>
        </td>
      </tr>
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>message"><fmt:message key="jmsmanager.sendmessage.message" /></label>:</div></th>
        <td>
          <textarea name="message" id="<portlet:namespace/>message" cols="50" rows="8"/></textarea>
        </td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td><input type="submit" value='<fmt:message key="jmsmanager.sendmessage.send"/>' /></td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->


<p><a href="<portlet:renderURL/>"><fmt:message key="jmswizard.common.cancel"/></a></p>
