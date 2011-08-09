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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg"%>
<fmt:setBundle basename="consolebase" />
<portlet:defineObjects />

<CommonMsg:commonMsg />

<%--   Removed until a better mechanism for rebooting the server is created
<table width="100%">
<form action="<portlet:actionURL/>">
<tr><td align="center"><input type="submit" value="Reboot" name="reboot"/></td></tr>
</form>
</table>
--%>
<br />
<table width="100%">
    <tbody>
        <tr>
            <td align="center">
            <form action="<portlet:actionURL/>" method="POST">
                <input type="hidden" name="shutdown" value="shutdown" />
                <input type="submit" value='<fmt:message key="servermanager.normal.shutdown"/>' 
                       onClick="return showGlobalConfirmMessage('<fmt:message key="servermanager.normal.comfirmMsg01"/>')" />
            </form>
            </td>
        </tr>
    </tbody>
</table>
<br />
