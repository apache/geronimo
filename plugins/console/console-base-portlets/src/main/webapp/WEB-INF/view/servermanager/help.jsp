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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>
<%--   Removed until a better mechanism for rebooting the server is created
<p>This portlet allows a warm reboot of the server or a shutdown of it.</p>
<p>A warm reboot will shutdown all applications and services and reboot the Geronimo kernel within the same process. The Geronimo Console session will be disconnected. Reconnect after the server is back up.</p>
--%>
<fmt:message key="servermanager.help.shutdownHelpBody" />
<br />
