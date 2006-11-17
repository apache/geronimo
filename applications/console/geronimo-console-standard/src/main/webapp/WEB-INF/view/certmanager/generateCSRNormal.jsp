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

alias: <c:out value="${alias}"/><br/>

<table>
<th>PKCS10 Certification Request</th>
<tr>
<td>
<form action=>
<textarea rows="15" cols="80" readonly>
<c:out value="${requestScope['org.apache.geronimo.console.cert.csr']}"/>
</textarea>
</td>
</tr>
<tr>
<td><a href="javascript:history.back();">back</a></td>
</tr>
</table>
