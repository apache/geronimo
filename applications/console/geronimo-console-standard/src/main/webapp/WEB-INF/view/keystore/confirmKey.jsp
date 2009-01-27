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
<portlet:defineObjects/>

<p>Please confirm that this is the correct information for the key I'm about to generate:</p>

<form name="<portlet:namespace/>KeystoreForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="confirmKey-after" />
    <input type="hidden" name="keystore" value="${keystore}" />
    <input type="hidden" name="alias" value="${alias}" />
    <input type="hidden" name="password" value="${password}" />
    <input type="hidden" name="keySize" value="${keySize}" />
    <input type="hidden" name="algorithm" value="${algorithm}" />
    <input type="hidden" name="valid" value="${valid}" />
    <input type="hidden" name="certCN" value="${certCN}" />
    <input type="hidden" name="certO" value="${certO}" />
    <input type="hidden" name="certOU" value="${certOU}" />
    <input type="hidden" name="certL" value="${certL}" />
    <input type="hidden" name="certST" value="${certST}" />
    <input type="hidden" name="certC" value="${certC}" />

    <table border="0">
        <tr>
            <th align="right">Alias:</th>
            <td>${alias}</td>
        </tr>
        <tr>
            <th align="right">Key Type:</th>
            <td>${algorithm} -- ${keySize} bits</td>
        </tr>
        <tr>
            <th align="right">Validity:</th>
            <td>${validFrom} to ${validTo}</td>
        </tr>
        <tr>
            <th align="right" valign="top">Identity:</th>
            <td>
                ${certCN}<br />
                ${certOU}, ${certO}<br />
                ${certL}, ${certST} ${certC}
            </td>
        </tr>
    </table>

    <input type="submit" value="Generate Key" />
</form>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="viewKeystore-before" />
              <portlet:param name="id" value="${id}" />
            </portlet:actionURL>">Cancel</a></p>
