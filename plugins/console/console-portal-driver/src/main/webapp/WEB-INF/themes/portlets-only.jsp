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

<style type="text/css">
body {
    margin:0;
    padding:10px;
    overflow-x:hidden;
    overflow-y:scroll;
}
</style>

<%-- Transparent Line Definition : Start --%>
<style type="text/css">
#transparentLine{
    background: url("/console/images/transparent_line.png") repeat-x;
    position:fixed;
    z-index:999; 
    width: 100%; 
    height: 9px;
    top: 0; 
    left: 0;
}
</style>
<div id="transparentLine"></div>
<%-- Transparent Line Definition : End --%>

<script language="Javascript" src="/console/GlobalUtils.js" type="text/javascript"></script>

<body id="portlets">

<pluto:isMaximized var="isMax" />
<c:forEach var="portlet" varStatus="status" items="${currentPage.portletIds}">
    <c:set var="portlet" value="${portlet}" scope="request" />
    <jsp:include page="portlet-skin.jsp" />
</c:forEach>

</body>

<script language="Javascript">
    // we show the "Loading..." status in navigation.js
    hideGlobalStatus();
</script>

<script type="text/javascript">
    if(parent){
        if(!(parent.location.hash=="#noxssPage=<c:out value="${hashOfCurrentPortalPage}"/>"))
            parent.location.hash="#noxssPage=<c:out value="${hashOfCurrentPortalPage}"/>";
        <!--window.parent.dojo.hash("#noxssPage=<c:out value='${hashOfCurrentPortalPage}'/>",true);-->
    }
</script>