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
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg" %>
<fmt:setBundle basename="consolebase"/>

<portlet:defineObjects/>

<CommonMsg:commonMsg/><div id="<portlet:namespace/>CommonMsgContainer"></div>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>AliasesForm" action="<portlet:actionURL/>" method="POST">

    <table border="0">
    <!-- ENTRY FIELD: NAME -->
      <tr>
        <th colspan="2" align="left"><fmt:message key="artifact.normal.name"/></th>
      </tr>
      <tr>
        <td>
          <input name="name" type="text" size="50" value="${AliasesData.name}" title='<fmt:message key="artifact.normal.name"/>'>
        </td>
      </tr>
    <!-- HEADER -->
      <tr>
        <th colspan="2" align="left"><fmt:message key="artifact.normal.aliases"/></th>
      </tr>
      <tr>
        <td>
          <input name="aliases" title='<fmt:message key="artifact.normal.aliases" />' type="text" size="50" value="${AliasesData.aliases}">
   	    </td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td>
          <input type="submit" value='<fmt:message key="artifact.actions.save"/>'>
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->



<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>"><fmt:message key="artifact.actions.cancel"/></a></p>
