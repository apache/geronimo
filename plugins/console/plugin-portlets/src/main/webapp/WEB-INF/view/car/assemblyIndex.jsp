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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="pluginportlets"/>

<portlet:defineObjects/>

<h2><fmt:message key="car.index.assembleServerLong"/></h2>

<form name="<portlet:namespace/>AssemblyForm" action="<portlet:actionURL/>" method="POST">
    <label for="<portlet:namespace/>type"><fmt:message key="car.index.assemblyMode"/></label>
    <select name="type" id="<portlet:namespace/>type">
        <option selected>Function Centric</option>
        <option>Application Centric</option>
    </select>
    <input type="hidden" name="mode" value="assemblyName-before"/>
    <input type="submit" value='<fmt:message key="car.index.assembleServer"/>'/>
</form>

