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
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>KeystoreForm";
var <portlet:namespace/>requiredFields = new Array("alias", "password", "certCN", "certO", "certOU", "certL", "certST", "certC");
var <portlet:namespace/>numericFields = new Array("valid");
var <portlet:namespace/>passwordFields = new Array("password");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields))
        return false;
    for(i in <portlet:namespace/>numericFields) {
        if(!checkIntegral(<portlet:namespace/>formName, <portlet:namespace/>numericFields[i]))
            return false;
    }
    if(!passwordElementsConfirm(<portlet:namespace/>formName, <portlet:namespace/>passwordFields)) {
        return false;
    }
    return true;
}
</script>

<p>On this screen you can configure the settings to generate a new private key.
The next screen will let you review this information before generating the
private key and accompanying certificate.</p>

<form name="<portlet:namespace/>KeystoreForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="keystore" value="${keystore}" />
    <input type="hidden" name="mode" value="configureKey-after" />
    <table border="0">
        <tr>
            <th align="right">Alias for new key:</th>
            <td>
                <input type="text" name="alias" size="20" maxlength="100" />
            </td>
        </tr>
        <tr>
            <th align="right">Password for new key:</th>
            <td>
                <input type="password" name="password" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right">Confirm password:</th>
            <td>
                <input type="password" name="confirm-password" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right">Key Size:</th>
            <td>
                <select name="keySize">
                    <option>512</option>
                    <option selected="true">1024</option>
                    <option>2048</option>
                </select>
            </td>
        </tr>
        <tr>
            <th align="right">Algorithm:</th>
            <td>
                <select name="algorithm">
                    <option>MD2withRSA</option>
                    <option selected="true">MD5withRSA</option>
                    <option>SHA1withRSA</option>
                </select>
            </td>
        </tr>
        <tr>
            <th align="right">Valid for (# of days):</th>
            <td>
                <input type="text" name="valid" size="5" maxlength="8" />
            </td>
        </tr>
        <tr>
            <th colspan="2">Certificate Identity</th>
        </tr>
        <tr>
            <th align="right">Server Hostname (CN):</th>
            <td>
                <input type="text" name="certCN" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right">Company/Organization (O):</th>
            <td>
                <input type="text" name="certO" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right">Division/Business Unit (OU):</th>
            <td>
                <input type="text" name="certOU" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right">City/Locality (L):</th>
            <td>
                <input type="text" name="certL" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right">State/Province (ST):</th>
            <td>
                <input type="text" name="certST" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right">Country Code (2 char) (C):</th>
            <td>
                <input type="text" name="certC" size="3" maxlength="2" />
            </td>
        </tr>
    </table>
    <input type="submit" value="Review Key Data" onClick="return <portlet:namespace/>validateForm();"/>
</form>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="viewKeystore-before" />
              <portlet:param name="id" value="${keystore}" />
            </portlet:actionURL>">Cancel</a></p>
