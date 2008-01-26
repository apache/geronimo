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
<%@ page import="org.apache.geronimo.monitoring.console.StatsGraph" %>
<%@ page import="org.apache.geronimo.monitoring.console.GraphsBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.DatabaseMetaData" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="org.apache.geronimo.monitoring.console.util.*" %>
<portlet:defineObjects/>

<%

String view_id = (String) request.getAttribute("view_id"); 

DBManager DBase = new DBManager();
Connection con = DBase.getConnection();

PreparedStatement pStmt = con.prepareStatement("SELECT view_id, name, description, graph_count, added, modified FROM views WHERE enabled=1 AND view_id="+view_id);
ResultSet rs = pStmt.executeQuery();
GraphsBuilder run = new GraphsBuilder(con);
String errors = new String("");

if (rs.next())
{
    String name = rs.getString("name");
    String description = rs.getString("description");
    pStmt = con.prepareStatement("SELECT * FROM views_graphs WHERE view_id="+view_id);
    ResultSet rs2 = pStmt.executeQuery();
    ArrayList<Integer> ids = new ArrayList<Integer>();
    while(rs2.next()) {
        ids.add( new Integer(rs2.getInt("graph_id")) );
    }
    
    rs.close();
    rs2.close();

    try {
        if(con != null) {
            con.close();
        }
    } catch(Exception e) {
        
    }
    
    ArrayList <StatsGraph> graphs = new ArrayList<StatsGraph>();
    for(int i = 0 ; i < ids.size(); i++) {
        try {
            StatsGraph graph = run.buildOneDB( ids.get(i).intValue() );
            if(graph != null) {
                graphs.add( graph );
            }
        } catch (Exception e) {
            errors = errors + "<li>Graph " + ids.get(i) + " could not be drawn due to server being offline</li>";
        }
    }
    
%>
<!-- <head> -->
    <script type='text/javascript' src='/dojo/1.0/dojo/dojo.js' djConfig='isDebug: false, parseOnLoad: true'>
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
    <% for (StatsGraph graph : graphs)
       out.println(graph.getJS());

    %>
};

dojo.addOnLoad(makeObjects);
    </script>
<!-- </head> -->

<table>
    <tr>
        <!-- Body -->
        <td width="90%" align="left" valign="top">
            <p>
            <font face="Verdana" size="+1">
            <%=name%>
            </font>
            </p>         
            <p><%=description%></p>

<% 
try
{
for (StatsGraph graph : graphs) 
{
%>
<p>
<div id="<%=graph.getDivName()%>Head" style="background-color: #f2f2f2; border-top: 1px solid #2581c7; margin: 0px; width: 100%; height: 16px;"><div align="left" style="background-color: #f2f2f2; float:left; text-align:left; width:75%; height: 20px;"><%=graph.getName() %></div><div align=right style="background-color: #f2f2f2; float:left; width:25%; text-align:right;"><a href="#" onClick="hide('<%=graph.getDivName()%>')"><img border=0 src="/monitoring/images/min-b.png"></a>&nbsp;<a href="#" onClick="show('<%=graph.getDivName()%>')"><img border=0 src="/monitoring/images/max-b.png"></a>&nbsp;<a href="#" onClick="hide('<%=graph.getDivName()%>');hide('<%=graph.getDivName()%>Head');"><img border=0 src="/monitoring/images/close-b.png"> </a></div></div>
<%=graph.getDivImplement()%>
</p>
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
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditView" /><portlet:param name="view_id" value="<%=view_id%>" /></portlet:actionURL>">Modify this view</a></li>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="disableView" /><portlet:param name="view_id" value="<%=view_id%>" /></portlet:actionURL>">Disable this view</a></li>
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
}
    else
    {%>
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
