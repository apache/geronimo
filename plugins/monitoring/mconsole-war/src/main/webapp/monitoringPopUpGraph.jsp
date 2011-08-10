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
<%@page import="org.apache.geronimo.monitoring.console.Constants"%>
<%@page import="org.apache.geronimo.monitoring.console.StatsGraph"%>
<portlet:defineObjects/>
<html lang="en">
<head>
<%
StatsGraph graph = (StatsGraph) request.getAttribute("statsGraph");
String graphString = graph.getJS();
String divImplString = graph.getDivImplement();
String divNameString = graph.getName();
%>

<script type='text/javascript' src='<%=Constants.DOJO_JS%>' djConfig='isDebug: false, parseOnLoad: true'></script>
<script type='text/javascript' src='<%=Constants.DOJOX_JS%>'></script>
<script type='text/javascript'>
    dojo.require("dojox.charting.Chart2D");
    dojo.require("dojox.charting.themes.PlotKit.blue");
    dojo.require("dojox.fx.easing");

    makeObjects = function() {
        <%=graphString%>
    };
    dojo.addOnLoad(makeObjects);
    function refreshPeriodic() {
   // Reload the page every 5 minutes
   location.reload();
   timerID = setTimeout("refreshPeriodic()",300000);
}
timerID = setTimeout("refreshPeriodic()",300000);
</script>
</head>
<body>
<div id="<%=graph.getDivName()%>Head" style="background-color: #f2f2f2; height: 20px;">
    <span style="line-height:20px; vertical-align:middle;"><%=divNameString%></span>
</div>
<%=divImplString %>
</body>
</html>
