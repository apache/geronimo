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
<%@ page import="java.util.List" %>
<%@ page import="org.apache.geronimo.monitoring.console.Constants" %>
<%@ page import="org.apache.geronimo.monitoring.console.StatsGraph" %>
<%@ page import="org.apache.geronimo.monitoring.console.data.View" %>
<portlet:defineObjects/>

<%
String errors = "";

    View view = (View) request.getAttribute("view");
    List<StatsGraph> statsGraphs = (List<StatsGraph>) request.getAttribute("statsGraphs");
    if (view != null && statsGraphs != null) {

%>
<!-- <head> -->
    <script type='text/javascript' src='<%=Constants.DOJO_JS%>' djConfig='isDebug: false, parseOnLoad: true'>
    </script>
        <script type = "text/javascript">
<!--
function hide(x) {
document.getElementById(x).style.display='none';
}
function show(x) {
document.getElementById(x).style.display='';
}
//-->
</script>
    <script type='text/javascript'>
dojo.require("dojox.charting.Chart2D");
dojo.require("dojox.charting.themes.PlotKit.blue");
dojo.require("dojox.fx.easing");

makeObjects = function(){
    <% for (StatsGraph graph : statsGraphs)
       out.println(graph.getJS());

    %>
};

dojo.addOnLoad(makeObjects);
function refreshPeriodic() {
   // Reload the page every 5 minutes
   location.reload();
   timerID = setTimeout("refreshPeriodic()",300000);
}
timerID = setTimeout("refreshPeriodic()",300000);
    </script>
<!-- </head> -->

<table>
    <tr>
        <!-- Body -->
        <td width="90%" align="left" valign="top">
            <p>
            <font face="Verdana" size="+1">
            <%=view.getName()%>
            </font>
            </p>         
            <p><%=view.getDescription()%></p>

<% 
try
{
for (StatsGraph graph : statsGraphs)
{
%>
<p>
<div id="<%=graph.getDivName()%>Head" style="background-color: #f2f2f2; border-top: 1px solid #2581c7; margin: 0px; width: 100%; height: 16px;"><div align="left" style="background-color: #f2f2f2; float:left; text-align:left; width:75%; height: 20px;"><%=graph.getName() %></div><div align=right style="background-color: #f2f2f2; float:left; width:25%; text-align:right;"><a href="#" onClick="hide('<%=graph.getDivName()%>');hide('<%=graph.getDivName()%>Sub');"><img border=0 src="/monitoring/images/min-b.png" alt="hide"></a>&nbsp;<a href="#" onClick="show('<%=graph.getDivName()%>');show('<%=graph.getDivName()%>Sub');"><img border=0 src="/monitoring/images/max-b.png" alt="show"></a></div></div>
<%=graph.getDivImplement()%>
<%
}
}
catch (Exception e)
{
    
}
if (!errors.equals(""))
{
    %>
    <ul>
    <font color="red"><%=errors%></font>
    </ul>
    <%
}
%>

        </td>
     
         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

        <!-- Geronimo Links -->
        <td valign="top">
            <table width="100%" style="border-bottom: 1px solid #2581c7;" cellspacing="1" cellpadding="1">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1">Navigation</font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <ul>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showHome" /></portlet:actionURL>">Home</a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllViews" /></portlet:actionURL>">Views</a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllServers" /></portlet:actionURL>">Servers</a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllGraphs" /></portlet:actionURL>">Graphs</a></li>
                        </ul>
                        &nbsp;<br />
                    </td>   
                </tr>
            </table>
            <br>
            <br>
            <table width="100%" style="border-bottom: 1px solid #2581c7;" cellspacing="1" cellpadding="1">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1">Actions</font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <ul>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditView" /><portlet:param name="view_id" value="<%=view.getIdString()%>" /></portlet:actionURL>">Modify this view</a></li>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="disableView" /><portlet:param name="view_id" value="<%=view.getIdString()%>" /></portlet:actionURL>">Disable this view</a></li>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddView" /></portlet:actionURL>">Create a new view</a></li>
                        </ul>
                        &nbsp;<br />
                    </td>   
                </tr>
            </table>

        </td>        
    </tr>
</table>
<%
} else {
%>
<table>
    <tr>
        <!-- Body -->
        <td width="90%" align="left" valign="top">
            <a HREF="javascript:history.go(-1)"><< Back</a>
            <p>
            <font face="Verdana" size="+1">
            View does not exist or is disabled
            </font>
            </p>         

        </td>
     
         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

        <!-- Geronimo Links -->
        <td valign="top">
            <table width="100%" style="border-bottom: 1px solid #2581c7;" cellspacing="1" cellpadding="1">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1">Navigation</font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <ul>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showHome" /></portlet:actionURL>">Home</a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllViews" /></portlet:actionURL>">Views</a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllServers" /></portlet:actionURL>">Servers</a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllGraphs" /></portlet:actionURL>">Graphs</a></li>
                        </ul>
                        &nbsp;<br />
                    </td>   
                </tr>
            </table>
            <br>
            <br>
            <table width="100%" border="1" cellspacing="0" cellpadding="3" bordercolor="#000000">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1">Actions</font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <ul>
                        <li><a href="#">Create a new view</a></li>
                        </ul>
                        &nbsp;<br />
                    </td>   
                </tr>
            </table>

        </td>  
    </tr>
</table>
    <%}%>
