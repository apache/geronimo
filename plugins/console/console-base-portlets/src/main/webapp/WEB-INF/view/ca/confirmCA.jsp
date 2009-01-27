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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<jsp:include page="_header.jsp" /><br>

<fmt:message key="ca.confirmCA.title"/>
<p>
<fmt:message key="ca.confirmCA.summary"/>
</p>

<form name="<portlet:namespace/>confirmCAForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="confirmCA-after" />
    <table border="0">
        <tr>
            <th class="DarkBackground" colspan="2" align="left"><fmt:message key="ca.common.certAuthorityIdentity"/>
            </th>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="ca.common.commonName"/> (CN):</th>
            <td class="LightBackground">
                <input type="hidden" name="caCN" value="${caCN}" /> ${caCN}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right"><fmt:message key="ca.common.Division_BusinessUnit"/> (OU):</th>
            <td class="MediumBackground">
                <input type="hidden" name="caOU" value="${caOU}" /> ${caOU}
            </td>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="ca.common.company_Organization"/> (O):</th>
            <td class="LightBackground">
                <input type="hidden" name="caO" value="${caO}" /> ${caO}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right"><fmt:message key="ca.common.city_Locality"/> (L):</th>
            <td class="MediumBackground">
                <input type="hidden" name="caL" value="${caL}" /> ${caL}
            </td>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="ca.common.state_Province"/> (ST):</th>
            <td class="LightBackground">
                <input type="hidden" name="caST" value="${caST}" /> ${caST}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right"><fmt:message key="ca.common.countryCode"/>(2 char) (C):</th>
            <td class="MediumBackground">
                <input type="hidden" name="caC" value="${caC}" /> ${caC}
            </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <tr>
            <th class="DarkBackground" colspan="2" align="left"><fmt:message key="ca.common.keyDetails"/></th>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="ca.common.aliasForCAKeypair"/>:</th>
            <td class="LightBackground">
                <input type="hidden" name="alias" value="${alias}" /> ${alias}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right"><fmt:message key="ca.common.keyAlgorithm"/>:</th>
            <td class="MediumBackground">
                <input type="hidden" name="keyAlgorithm" value="${keyAlgorithm}" /> ${keyAlgorithm}
            </td>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="ca.common.keySize"/>:</th>
            <td class="LightBackground">
                <input type="hidden" name="keySize" value="${keySize}" /> ${keySize}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right"><fmt:message key="consolebase.common.password"/>:</th>
            <td class="MediumBackground">
                <input type="hidden" name="password" value="${password}" /> ********
            </td>
        </tr>
        <tr><td>&nbsp</td></tr>
        <tr>
            <th class="DarkBackground" colspan="2" align="left"><fmt:message key="ca.common.certificateDetails"/></th>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="ca.common.certSerialNumber"/>:</th>
            <td class="LightBackground">
                <input type="hidden" name="sNo" value="${sNo}" /> ${sNo}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right"><fmt:message key="ca.common.validFromDate"/>(mm/dd/yyyy):</th>
            <td class="MediumBackground">
                <input type="hidden" name="validFrom" value="${validFrom}" /> ${validFrom}
            </td>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="ca.common.validToDate"/>(mm/dd/yyyy):</th>
            <td class="LightBackground">
                <input type="hidden" name="validTo" value="${validTo}" /> ${validTo}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right"><fmt:message key="ca.common.signatureAlgorithm"/>:</th>
            <td class="MediumBackground">
                <input type="hidden" name="algorithm" value="${algorithm}" /> ${algorithm}
            </td>
        </tr>
    </table>
    <input type="submit" value='<fmt:message key="ca.common.setupCertAuthority"/>' />
</form>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
