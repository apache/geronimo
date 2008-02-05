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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="pluginportlets"/>
<portlet:defineObjects/>

<form name="<portlet:namespace/>ContinueForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="downloadStatus-after" />
    <input type="hidden" name="repository" value="${repository}" />
    <input type="hidden" name="repo-user" value="${repouser}" />
    <input type="hidden" name="repo-pass" value="${repopass}" />
    <input type="hidden" name="download-key" value="${downloadKey}" /> 
    
    <%  String[] configIds = request.getParameterValues("configIds");
        for(int i=0;i<configIds.length;i++){
    %>
    <input type="hidden" name="configId" value="<%=configIds[i]%>" />
    <%
        }
    %>
</form>

<jsp:include flush="false" page="../ajax/progressbar.jsp?downloadKey=${downloadKey}"/>

<script type="text/javascript">
    dwr.engine.setActiveReverseAjax(true);
    <portlet:namespace/>startProgress();
</script>

<%--
<p><a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="index-before" /></portlet:actionURL>">
<fmt:message key="consolebase.common.cancel"/></a></p>
--%>
