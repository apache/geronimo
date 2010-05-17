<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed  under the  License is distributed on an "AS IS" BASIS,
WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
implied.

See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://portals.apache.org/pluto" prefix="pluto" %>
<body id="portlets" marginwidth="0" marginheight="0" leftmargin="0" topmargin="0" rightmargin="0">
<!-- Content block: portlets are divided into two columns/groups -->
<!--<div id="body-block" style="height:100%">-->
<div id="content"><pluto:isMaximized var="isMax" />

<c:forEach var="portlet" varStatus="status" items="${currentPage.portletIds}">
    <c:set var="portlet" value="${portlet}" scope="request" />
    <jsp:include page="portlet-skin.jsp" />
</c:forEach>
</div>

<script type="text/javascript">
    if(parent){
       
        if(!(parent.location.hash=="#noxssPage=<c:out value="${hashOfCurrentPortalPage}"/>"))
        parent.location.hash="#noxssPage=<c:out value="${hashOfCurrentPortalPage}"/>";
        <!--window.parent.dojo.hash("#noxssPage=<c:out value='${hashOfCurrentPortalPage}'/>",true);-->
        
    }
</script>
</body>

