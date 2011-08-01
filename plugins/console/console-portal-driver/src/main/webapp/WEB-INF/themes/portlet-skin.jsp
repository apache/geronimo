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
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt"%>
<fmt:setLocale value="<%=request.getLocale()%>"/>
<fmt:setBundle basename="org.apache.geronimo.console.i18n.ConsoleResource"/>

<!-- Use pluto portlet tag to render the portlet -->
<pluto:portlet portletId="${portlet}">

    <!-- Assemble the rendering result -->
    <div class="portlet" id='<c:out value="${portlet}"/>'>
        <div class="title">
            <table class="Caption" width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td class="Figure">
                        <!-- Portlet Title -->
                        <h2><fmt:message key="<%=(String)request.getAttribute( org.apache.pluto.driver.AttributeKeys.PORTLET_TITLE )%>"/></h2>
                        <!-- Portlet Mode Controls -->
                        <pluto:modeAnchor portletMode="view"/>
                        <pluto:modeAnchor portletMode="edit"/>
                        <pluto:modeAnchor portletMode="help"/>
                        <!-- Window State Controls -->
                        <a href="<pluto:url windowState="minimized"/>"><span class="minimized"></span></a>
                        <a href="<pluto:url windowState="maximized"/>"><span class="maximized"></span></a>
                        <a href="<pluto:url windowState="normal"/>"><span class="normal"></span></a>
                    </td>
                </tr>
            </table>
        </div>
        <div class="body">
            <pluto:render/>
        </div>
    </div>

</pluto:portlet>

