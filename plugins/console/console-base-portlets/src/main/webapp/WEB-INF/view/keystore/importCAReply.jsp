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
keystore: ${id}<br/>
alias: ${alias}<br/>

<form method="post"
action="<portlet:actionURL>
<portlet:param name="mode" value="importCAReply-after" />
<portlet:param name="id" value="${id}" />
<portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
<table>
<th>PKCS7 Certificate Reply</th>
<tr>
<td>
<textarea rows="20" cols="80" name="pkcs7cert">
...paste pkcs7 encoded certificate reply here...
</textarea>
</td>
</tr>
</table>
<table>
<tr>
<td><input type="submit" name="submit" value="Save"/></td>
<td><input type="submit" name="submit" value="Cancel"/></td>
</tr>
</table>
</form>
