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
<form name="datasource_form" action="<portlet:actionURL portletMode="view"/>">
<br>
<strong>Connection Name:</strong>&nbsp;${attributeMap.name}
<br><br>
<table width="100%">
        <tr>
            <th>Property</th>
            <th>Value</th>
        </tr>
    <c:forEach var="entry" items="${attributeMap}">
        <tr>
            <td><strong>${entry.key}</strong></td>
            <td>${entry.value}</td>
        </tr>
    </c:forEach>
            <tr>
                <td colspan="2">
                    <table width="100%">
                        <tr>
                            <td width="10%">&nbsp</td>
                            <td>
                                <input type="submit" name="btnBack" value="Back to JMS Connection Factories">
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>    
</table>
<input type="hidden" name="name" value="back">
</form>
