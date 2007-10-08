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

<b>Issue New Certificate</b> - Step 3: Confirm and Issue Certificate

<p> This screen shows the CSR details and the certificate details entered in the previous screen.  Upon clicking
the <i>Issue Certificate</i> button, a certificate will be issued and stored.  The next screen will show the
details of the certificate issued.</p>

<jsp:include page="_header.jsp" />

<form name="<portlet:namespace/>confirmClientCertForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="confirmClientCert-after"/>
    <input type="hidden" name="pkcs10certreq" value="${pkcs10certreq}"/>
    <input type="hidden" name="requestId" value="${requestId}"/>
    <table border="0">
        <tr>
            <th class="DarkBackground" colspan="2" align="left">Certificate Requestor Details</th>
        </tr>
        <tr>
            <th class="LightBackground" align="right">Subject:</th>
            <td class="LightBackground">
                <input type="hidden" name="subject" value="${subject}" /> ${subject}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right">Public Key:</th>
            <td class="MediumBackground">
                <input type="hidden" name="publickey" value="${publickey}" /><pre> ${publickey} </pre>
            </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
    </table>
    <table border="0">
        <tr>
            <th class="DarkBackground" colspan="2" align="left">Details of the Certificate to be issued</th>
        </tr>
        <tr>
            <th class="LightBackground" align="right">Certificate Serial Number:</th>
            <td class="LightBackground">
                <input type="hidden" name="sNo" value="${sNo}" /> ${sNo}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right">Valid From Date (mm/dd/yyyy):</th>
            <td class="MediumBackground">
                <input type="hidden" name="validFrom" value="${validFrom}" /> ${validFrom}
            </td>
        </tr>
        <tr>
            <th class="LightBackground" align="right">Valid To Date (mm/dd/yyyy):</th>
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
    <input type="submit" value="Issue Certificate" />
</form>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before"/>
            </portlet:actionURL>">Cancel</a></p>
