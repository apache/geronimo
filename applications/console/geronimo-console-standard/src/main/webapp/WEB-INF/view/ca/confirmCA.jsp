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
<portlet:defineObjects/>

<b>Setup Certification Authority</b> - Step 2: Review and Confirm CA details

<p>This screen lets you review the CA details entered in the previous screen.  If you need to modify any
information shown here, please go back to the previous screen.  Upon clicking the <i>Setup Certification Authority</i>
button, a keypair and a self-signed certificate will be generated and stored in a keystore.  CA's certificate serial
number is stored in a text file and the number is incremented each time a CSR is processed by the CA.</p>

<jsp:include page="_header.jsp" />

<form name="<portlet:namespace/>confirmCAForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="confirmCA-after" />
    <table border="0">
        <tr>
            <th class="DarkBackground" colspan="2" align="left">Certification Authority's Identity</th>
        </tr>
        <tr>
            <th class="LightBackground" align="right">Common Name (CN):</th>
            <td class="LightBackground">
                <input type="hidden" name="caCN" value="${caCN}" /> ${caCN}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right">Division/Business Unit (OU):</th>
            <td class="MediumBackground">
                <input type="hidden" name="caOU" value="${caOU}" /> ${caOU}
            </td>
        </tr>
        <tr>
            <th class="LightBackground" align="right">Company/Organization (O):</th>
            <td class="LightBackground">
                <input type="hidden" name="caO" value="${caO}" /> ${caO}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right">City/Locality (L):</th>
            <td class="MediumBackground">
                <input type="hidden" name="caL" value="${caL}" /> ${caL}
            </td>
        </tr>
        <tr>
            <th class="LightBackground" align="right">State/Province (ST):</th>
            <td class="LightBackground">
                <input type="hidden" name="caST" value="${caST}" /> ${caST}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right">Country Code (2 char) (C):</th>
            <td class="MediumBackground">
                <input type="hidden" name="caC" value="${caC}" /> ${caC}
            </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <tr>
            <th class="DarkBackground" colspan="2" align="left">Key Details</th>
        </tr>
        <tr>
            <th class="LightBackground" align="right">Alias for CA's keypair:</th>
            <td class="LightBackground">
                <input type="hidden" name="alias" value="${alias}" /> ${alias}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right">Key Algorithm:</th>
            <td class="MediumBackground">
                <input type="hidden" name="keyAlgorithm" value="${keyAlgorithm}" /> ${keyAlgorithm}
            </td>
        </tr>
        <tr>
            <th class="LightBackground" align="right">Key Size:</th>
            <td class="LightBackground">
                <input type="hidden" name="keySize" value="${keySize}" /> ${keySize}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right">Password:</th>
            <td class="MediumBackground">
                <input type="hidden" name="password" value="${password}" /> ********
            </td>
        </tr>
        <tr><td>&nbsp</td></tr>
        <tr>
            <th class="DarkBackground" colspan="2" align="left">Certificate Details</th>
        </tr>
        <tr>
            <th class="LightBackground" align="right">Certificate Serial Number:</th>
            <td class="LightBackground">
                <input type="hidden" name="sNo" value="${sNo}" /> ${sNo}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right">Valid From Date(mm/dd/yyyy):</th>
            <td class="MediumBackground">
                <input type="hidden" name="validFrom" value="${validFrom}" /> ${validFrom}
            </td>
        </tr>
        <tr>
            <th class="LightBackground" align="right">Valid To Date(mm/dd/yyyy):</th>
            <td class="LightBackground">
                <input type="hidden" name="validTo" value="${validTo}" /> ${validTo}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right">Signature Algorithm:</th>
            <td class="MediumBackground">
                <input type="hidden" name="algorithm" value="${algorithm}" /> ${algorithm}
            </td>
        </tr>
    </table>
    <input type="submit" value="Setup Certification Authority" />
</form>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>">Cancel</a></p>
