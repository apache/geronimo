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

<br>
<a href="<portlet:renderURL portletMode="view"><portlet:param name="processAction" value="viewDestinations"/></portlet:renderURL>">Back To Destination List </a>
<br><br>
 <table width="100%">
        <tr>
           <th colspan="2"> Statisctic for <c:out value="${statistics.destinationName}"/></th>
        </tr>
        <tr>
            <td width="250">Description</td>
            <td width="200"><c:out value="${statistics.description}"/></td>
        </tr>
        <tr>
            <td width="250">Current Depth</td>
            <td width="200"><c:out value="${statistics.currentDepth}"/></td>
        </tr>
        <tr>
            <td width="250">Open Output Count</td>
            <td width="200"><c:out value="${statistics.openOutputCount}"/></td>
        </tr>
        <tr>
            <td width="250">Open Input Count</td>
            <td width="200"><c:out value="${statistics.openInputCount}"/></td>
        </tr>
        <tr>
            <td width="250">Inhibit Get</td>
            <td width="200"><c:out value="${statistics.inhibitGet}"/></td>
        </tr>
        <tr>
            <td width="250">Inhibit Put</td>
            <td width="200"><c:out value="${statistics.inhibitPut}"/></td>
        </tr>
        <tr>
            <td width="250">Sharable</td>
            <td width="200"><c:out value="${statistics.sharable}"/></td>
        </tr>
        <tr>
            <td width="250">Maximum Depth</td>
            <td width="200"><c:out value="${statistics.maximumDepth}"/></td>
        </tr>
        <tr>
            <td width="250">Trigger Control</td>
            <td width="200"><c:out value="${statistics.triggerControl}"/></td>
        </tr>
        <tr>
            <td width="250">Maximum Message Length</td>
            <td width="200"><c:out value="${statistics.maximumMessageLength}"/></td>
        </tr>
</table>
