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
<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="pluginportlets"/>
<portlet:defineObjects/>

<p><fmt:message key="car.confirmExport.title" /></p>

<p>
<fmt:message key="car.confirmExport.useBtnBelow" >
<fmt:param  value="${name}"/>
</fmt:message>
</p>

<%-- todo: calculate the /console prefix somehow --%>
<form action="/console/forwards/car-export" method="GET">
    <input type="hidden" name="configId" value="${configId}" />
    <input type="submit" value='<fmt:message key="car.common.exportPlugin" />' />
    <input type="submit" value='<fmt:message key="consolebase.common.done" />' onclick="history.go(-2); return false;" />
</form>
