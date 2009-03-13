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

<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>KeystoreForm";
var <portlet:namespace/>requiredFields = new Array("alias", "password", "valid", "certCN", "certO", "certOU", "certL", "certST", "certC");
var <portlet:namespace/>integerFields = new Array("valid");
var <portlet:namespace/>passwordFields = new Array("password");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="keystore.common.emptyText"/>');
        return false;    
    }
    if(!passwordElementsConfirm(<portlet:namespace/>formName, <portlet:namespace/>passwordFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="keystore.common.passwordMismatch"/>');
        return false;
    }
    for(i in <portlet:namespace/>integerFields) {
        if(!checkIntegral(<portlet:namespace/>formName, <portlet:namespace/>integerFields[i])) {
            addErrorMessage("<portlet:namespace/>", '<fmt:message key="keystore.common.integer"/>');
            return false;
        }
    }
    
    return true;
}
</script>

<div id="<portlet:namespace/>CommonMsgContainer"></div>

<p><fmt:message key="keystore.configureKey.title"/></p>

<form name="<portlet:namespace/>KeystoreForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="keystore" value="${keystore}" />
    <input type="hidden" name="mode" value="configureKey-after" />
    <table border="0">
        <tr>
            <th align="right"><label for="<portlet:namespace/>alias"><fmt:message key="keystore.configureKey.aliasForKey"/></label>:</th>
            <td>
                <input type="text" name="alias" id="<portlet:namespace/>alias" size="20" maxlength="100" />
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>password"><fmt:message key="keystore.configureKey.passwordForKey"/></label></th>
            <td>
                <input type="password" name="password" id="<portlet:namespace/>password" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>confirm-password"><fmt:message key="consolebase.common.confirmPassword"/></label>:</th>
            <td>
                <input type="password" name="confirm-password" id="<portlet:namespace/>confirm-password" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>keySize"><fmt:message key="keystore.configureKey.keySize"/></label>:</th>
            <td>
                <select name="keySize" id="<portlet:namespace/>keySize">
                    <option>512</option>
                    <option selected="true">1024</option>
                    <option>2048</option>
                </select>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>algorithm"><fmt:message key="keystore.configureKey.algorithm"/></label>:</th>
            <td>
                <select name="algorithm" id="<portlet:namespace/>algorithm">
                    <option>MD2withRSA</option>
                    <option selected="true">MD5withRSA</option>
                    <option>SHA1withRSA</option>
                </select>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>valid"><fmt:message key="keystore.configureKey.validFor"/> </label>:</th>
            <td>
                <input type="text" name="valid" id="<portlet:namespace/>valid" size="5" maxlength="8" />
            </td>
        </tr>
        <tr>
            <th colspan="2"><fmt:message key="keystore.configureKey.certificateIdentity"/></th>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>certCN"><fmt:message key="keystore.configureKey.serverHostname"/> (CN)</label>:</th>
            <td>
                <input type="text" name="certCN" id="<portlet:namespace/>certCN" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>certO"><fmt:message key="keystore.configureKey.company_Org"/> (O)</label>:</th>
            <td>
                <input type="text" name="certO" id="<portlet:namespace/>certO" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>certOU"><fmt:message key="keystore.configureKey.division_BusinessUnit"/> (OU)</label>:</th>
            <td>
                <input type="text" name="certOU" id="<portlet:namespace/>certOU" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>certL"><fmt:message key="keystore.configureKey.city_Locality"/> (L)</label>:</th>
            <td>
                <input type="text" name="certL" id="<portlet:namespace/>certL" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>certST"><fmt:message key="keystore.configureKey.state_Province"/> (ST)</label>:</th>
            <td>
                <input type="text" name="certST" id="<portlet:namespace/>certST" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>certC"><fmt:message key="keystore.configureKey.country_Code"/> (2 char) (C)</label>:</th>
            <td>
                <input type="text" name="certC" id="<portlet:namespace/>certC" size="3" maxlength="2" />
            </td>
        </tr>
    </table>
    <input type="submit" value='<fmt:message key="keystore.configureKey.reviewKeyData"/>' onClick="return <portlet:namespace/>validateForm();"/>
</form>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="viewKeystore-before" />
              <portlet:param name="id" value="${keystore}" />
            </portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
