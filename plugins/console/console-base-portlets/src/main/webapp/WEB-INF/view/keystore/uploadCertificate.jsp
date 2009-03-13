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
<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>KeystoreForm";
var <portlet:namespace/>requiredFields = new Array("certificate", "alias");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName, <portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="keystore.common.emptyText"/>');
        return false;
    }
    
    return true;
}
</script>

<!-- Uploading certificate using a disk file fails on Windows.  Certificate text is used instead.
<p>This screen lets you select a certificate to import into the keystore.  Select the
certificate file and specify an alias to store it under in the keystore.  The next
step will be to review the certificate before committing it to the keystore.</p>
-->

<div id="<portlet:namespace/>CommonMsgContainer"></div>

<p><fmt:message key="keystore.uploadCertificate.title"/></p>

<form enctype="multipart/form-data" method="POST" name="<portlet:namespace/>KeystoreForm" action="<portlet:actionURL/>">
    <input type="hidden" name="id" value="${id}" />
    <input type="hidden" name="mode" value="uploadCertificate-after" />
    <table border="0">
        <th align="left"> <label for="<portlet:namespace/>certificate"><fmt:message key="keystore.common.trustedCertificate"/></label> </th>
<!-- Uploading certificate using a disk file fails on Windows.  Certificate text is used instead.
        <tr>
            <th align="right">Certificate file:</th>
            <td>
                <input type="file" name="certificate" size="40" />
            </td>
        </tr>
 -->
        <tr>
            <td colspan="2">
                <textarea rows="15" cols="80" name="certificate" id="<portlet:namespace/>certificate"><fmt:message key="keystore.uploadCertificate.pasteHere"/></textarea>
            </td>
        </tr>
        <tr>
            <th align="left"><label for="<portlet:namespace/>alias"><fmt:message key="keystore.uploadCertificate.aliasForCertificate"/></label>:</th>
            <td>
                <input type="text" name="alias" id="<portlet:namespace/>alias" size="20" maxlength="200" />
            </td>
        </tr>
    </table>
    <input type="submit" value='<fmt:message key="keystore.uploadCertificate.reviewCertificate"/>' onclick="return <portlet:namespace/>validateForm()"/>
</form>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="viewKeystore-before" />
              <portlet:param name="id" value="${id}" />
            </portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
