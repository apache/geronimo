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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>HttpForm";
var <portlet:namespace/>requiredFields = new Array("host");
var <portlet:namespace/>numericFields = new Array("port", "maxThreads");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName, <portlet:namespace/>requiredFields)) {
        return false;
    }
    for(i in <portlet:namespace/>numericFields) {
        if(!checkIntegral(<portlet:namespace/>formName, <portlet:namespace/>numericFields[i])) {
            return false;
        }
    }
    return true;
}
</script>

<form name="<portlet:namespace/>HttpForm" action="<portlet:actionURL/>">
<input type="hidden" name="mode" value="${mode}">
<input type="hidden" name="protocol" value="${protocol}">
<input type="hidden" name="containerURI" value="${containerURI}">
<input type="hidden" name="managerURI" value="${managerURI}">
<c:if test="${mode eq 'save'}">
  <input type="hidden" name="connectorURI" value="${connectorURI}">
</c:if>
<table width="100%%"  border="0">

<!-- Current Task -->
<c:choose>
  <c:when test="${mode eq 'add'}">
    <tr><th colspan="2" align="left">Add new ${protocol} listener for ${containerDisplayName}</th></tr>
  </c:when>
  <c:otherwise>
    <tr><th colspan="2" align="left">Edit connector ${displayName}</th></tr>
  </c:otherwise>
</c:choose>

<!-- Name Field -->
<c:if test="${mode eq 'add'}">
  <tr>
    <td><div align="right">Unique Name: </div></td>
    <td><input name="displayName" type="text" size="30"></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>A name that is different than the name for any other web connectors in the server</td>
  </tr>
  <script language="JavaScript">
    <portlet:namespace/>requiredFields = new Array("displayName").concat(<portlet:namespace/>requiredFields);
  </script>
</c:if>
<!-- Host Field -->
  <tr>
    <td><div align="right">Host: </div></td>
    <td>
      <input name="host" type="text" size="30" value="${host}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The host name or IP to bind to.  The normal values are <tt>0.0.0.0</tt> (all interfaces) or <tt>localhost</tt> (local connections only)</td>
  </tr>
<!-- Port Field -->
  <tr>
    <td><div align="right">Port: </div></td>
    <td>
      <input name="port" type="text" size="5" value="${port}">
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The network port to bind to.</td>
  </tr>
<!-- Min Threads Field (Jetty only) -->
<c:if test="${server eq 'jetty'}">
  <tr>
    <td><div align="right">Min Threads: </div></td>
    <td>
      <input name="minThreads" type="text" size="3" value="${minThreads}">
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The minimum number of threads this connector should use to handle incoming requests</td>
  </tr>
  <script language="JavaScript">
    <portlet:namespace/>numericFields = <portlet:namespace/>numericFields.concat(new Array("minThreads"));
  </script>
</c:if>
<!-- Max Threads Field -->
  <tr>
    <td><div align="right">Max Threads: </div></td>
    <td>
      <input name="maxThreads" type="text" size="3" value="${maxThreads}">
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The maximum number of threads this connector should use to handle incoming requests</td>
  </tr>
<!-- Submit Button -->
  <tr>
    <td><div align="right"></div></td>
    <td>
      <input name="submit" type="submit" value="Save" onClick="return <portlet:namespace/>validateForm();">
      <input name="reset" type="reset" value="Reset">
      <input name="submit" type="submit" value="Cancel">
    </td>    
  </tr>
</table>
</form>
<a href='<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="list" />
         </portlet:actionURL>'>List connectors</a>
