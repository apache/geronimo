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
<%@ page import="org.apache.geronimo.monitoring.console.StatsGraph" %>
<%@ page import="org.apache.geronimo.monitoring.console.GraphsBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="java.lang.String" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.DatabaseMetaData" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="javax.management.InstanceNotFoundException" %>
<%@ page import="org.apache.geronimo.monitoring.console.util.*" %>
<%@ page import="org.apache.geronimo.monitoring.console.MRCConnector" %>
<fmt:setBundle basename="monitor-portlet"/>
<portlet:defineObjects/>

<%

String server_id = (String) request.getAttribute("server_id"); 
String message = (String) request.getAttribute("message");

DBManager DBase = new DBManager();
Connection con = DBase.getConnection();

MRCConnector mrc = null;

boolean isOnline = true;
Integer collecting = 0;
Long snapshotDuration = new Long(0);

if (message == null)
    message = new String("");

DBase = new DBManager();
con = DBase.getConnection();
PreparedStatement pStmt = con.prepareStatement("SELECT * FROM servers WHERE enabled=1 AND server_id="+server_id);
ResultSet rs = pStmt.executeQuery();

if (rs.next()) {
    TreeMap <String,String> availableBeansMap = null;
    TreeMap <String,String> trackedBeansMap = null;
    String ip = rs.getString("ip");
    int port = rs.getInt("port");
    int protocol = rs.getInt("protocol");
    String username = rs.getString("username");
    String name = rs.getString("name");
    String password = rs.getString("password");
    String last_seen = rs.getString("last_seen").substring(0,16);
    String added = rs.getString("added").substring(0,16);
    String modified = rs.getString("modified").substring(0,16);
    long retention = -1;
    rs.close();
    try {
        mrc = new MRCConnector(ip, username, password, port, protocol);
        availableBeansMap = mrc.getFreeStatisticsProviderBeanNamesMap();
        retention = mrc.getSnapshotRetention();
        trackedBeansMap = mrc.getTrackedBeansMap();
        snapshotDuration = (Long)mrc.getSnapshotDuration()/1000/60;
        collecting = mrc.isSnapshotRunning();
    } catch (Exception e) {
        isOnline = false;
        collecting = 0;
        message = message + "<br><font color='red'><strong><li>Server is offline</li></strong></font>";
    }
    
%>
<!-- <head> -->
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
<!-- </head> -->
            <%
 if (!message.equals(""))
 {
 %>
<div align="left" style="width: 500px">
<%=message %><br>
</div>
<%} %>
<table>
    <tr>
        <!-- Body -->
        <td width="90%" align="left" valign="top">
            <p>
            <font face="Verdana" size="+1">
            <%=name%>
            </font>
            </p>        
            <p>
            <table cellpadding="1" cellspacing="1">
                <tr>
                    <th align="left"><fmt:message key="monitor.server.status"/>:</th>
                    <td>&nbsp;</td>
                    <td align="right">
                                <%if (isOnline)
                {%>
                    <font color="green"><strong><fmt:message key="monitor.server.online"/></strong></font>
                   <%}
                                else
                                {%>
                                <font color="red"><strong><fmt:message key="monitor.server.offline"/></strong></font>
                                <%} %> 
                    </td>
                </tr>
                
                                <tr>
                    <th align="left"><fmt:message key="monitor.server.snapshotThread"/>:</th>
                    <td>&nbsp;</td>
                    <td align="right">
                                <%if (isOnline && collecting == 1)
                {%>
                    <fmt:message key="monitor.server.run"/>
                   <%}
                                else
                                {%>
                                <font color="red"><strong><fmt:message key="monitor.server.stopped"/></strong></font>
                                <%} %> 
                    </td>
                </tr>
                
                <tr>
                    <th align="left"><fmt:message key="monitor.common.added"/>:</th>
                    <td>&nbsp;</td>
                    <td align="right"><%=added%></td>
                </tr>
                <tr>
                    <th align="left"><fmt:message key="monitor.common.modified"/>:</th>
                    <td>&nbsp;</td>
                    <td align="right"><%=modified%></td>
                </tr>
                <tr>
                    <th align="left"><fmt:message key="monitor.common.seen"/>:</th>
                    <td>&nbsp;</td>
                    <td align="right"><%=last_seen%></td>
                </tr>
                <tr>
                    <th align="left"><fmt:message key="monitor.server.ip"/>/<fmt:message key="monitor.server.hostname"/>:</th>
                    <td>&nbsp;</td>
                    <td align="right"><%=ip%></td>
                </tr>
                <tr>
                    <th align="left"><fmt:message key="monitor.server.snapshotDuration"/>:</th>
                    <td>&nbsp;</td>
                          <%if (isOnline)
                {%>
                    <td align="right"><%=snapshotDuration%> <fmt:message key="monitor.common.minute"/></td>
                     <%}
                                else
                                {
                                    %>
                                    <td align="right"><fmt:message key="monitor.common.unknown"/></td>
                                    <%} %>
                </tr>
                <tr>
                    <th><fmt:message key="monitor.server.snapshotRetention"/>:</th>
                    <td>&nbsp;</td>
                          <%if (isOnline)
                {%>
                    <td align="right"><%=retention%> <fmt:message key="monitor.common.day"/></td>
                     <%}
                                else
                                {
                                    %>
                                    <td align="right"><fmt:message key="monitor.common.unknown"/></td>
                                    <%} %>
                </tr>
            </table>
            </p>
            <table>
            <thead><font size="+1"><fmt:message key="monitor.server.liveStat"/></font></thead>
            <%
            if (isOnline)
            {
            Integer counter = 0;
            for (Iterator <String> it = trackedBeansMap.keySet().iterator(); it.hasNext();)
            {
                String prettyBean = it.next().toString();
                Set<String> statAttributes = mrc.getStatAttributesOnMBean(trackedBeansMap.get(prettyBean));
                boolean started = true;
                HashMap<String, Long> beanStats = null;
                try {
                    beanStats = mrc.getStats(trackedBeansMap.get(prettyBean));
                } catch (InstanceNotFoundException infe) {
                    //The bean is not available
                    started = false;
                }
                if ((counter%3) == 0)
                {
                 %>
                 <tr>
                <%
                }
            %>
                <td valign="top">
                <table style="padding-left: 8px; padding-bottom: 10px;">
                <tr><th colspan="2"><%=prettyBean%></th></tr>
                <%
                if (started) {
                    for (Iterator <String> itt = statAttributes.iterator(); itt.hasNext();)
                    {
                        String dataName = itt.next().toString();
                %>
                        <tr><td><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddGraph" /><portlet:param name="server_id" value="<%=server_id%>" /><portlet:param name="mbean" value="<%=trackedBeansMap.get(prettyBean)%>" /><portlet:param name="dataname" value="<%=dataName%>" /></portlet:actionURL>"><%=dataName%></a></td><td><%=beanStats.get(dataName) %></td></tr>
                <%
                    }
                } else {
                %>
                    <tr><td><fmt:message key="monitor.server.statNA"/></td></tr>
                <%
                }
                %>                
                </table>
                </td>
                <%
                if ((counter%3) == 2)
                {
                 %>
                 </tr>
                <%
                }
                counter = counter + 1;
             }
             %>
            </table>
<%
            }
            else
            {
                %>
                </table>
                <font color="red"><fmt:message key="monitor.server.serverOffline"/></font>
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
                        <font face="Verdana" size="+1"><fmt:message key="monitor.common.nav"/></font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <ul>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showHome" /></portlet:actionURL>"><fmt:message key="monitor.common.home"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllViews" /></portlet:actionURL>"><fmt:message key="monitor.common.view"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllServers" /></portlet:actionURL>"><fmt:message key="monitor.common.server"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllGraphs" /></portlet:actionURL>"><fmt:message key="monitor.common.graph"/></a></li>
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
                        <font face="Verdana" size="+1"><fmt:message key="monitor.common.action"/></font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <ul>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><fmt:message key="monitor.server.modifyServer"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="disableServer" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><fmt:message key="monitor.server.disableServer"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddServer" /></portlet:actionURL>"><fmt:message key="monitor.server.addServer"/></a></li>
                        <%
                        if(collecting == 1) {
                        %>
                            <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="disableServerViewQuery" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><fmt:message key="monitor.server.disableQuery"/></a></li>
                        <%
                        } else if (collecting == 0){
                        %>
                            <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="enableServerViewQuery" /><portlet:param name="server_id" value="<%=server_id%>" /><portlet:param name="snapshotDuration" value='<%= "" + (snapshotDuration * 1000 * 60) %>' /></portlet:actionURL>"><fmt:message key="monitor.server.enableQuery"/></a></li>
                        <%
                        }
                        else if (collecting == -1){
                            %>
                                <li><fmt:message key="monitor.server.stoppingThread"/></li>
                            <%
                            }
                        %>
                        </ul>
                        &nbsp;<br />
                    </td>   
                </tr>
            </table>
            <br>
            <br>
            <table style="border-bottom: 1px solid #2581c7;" width="100%" cellspacing="1" cellpadding="1">
                <tr>
                    <td colspan="2" class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1"><fmt:message key="monitor.server.statCollected"/></font>
                    </td>
                </tr>

                        
                 <%
                 try {
                     // store all mbeans into a map [category --> list of mbeans)
                     TreeMap<String, ArrayList<String>> trackedCategoryMap = new TreeMap<String, ArrayList<String>>();
                     for(Iterator<String> it = trackedBeansMap.keySet().iterator(); it.hasNext(); ) {
                         String mbeanPretty = it.next();
                         String mbean = trackedBeansMap.get(mbeanPretty); // full mbean name
                         String[] params = mbean.split(",");
                         String category = "Other";
                         for(int i = 0 ; i < params.length; i++) {
                             if(params[i].startsWith("j2eeType=")) {
                                 // we've found our category, so stop it
                                 category = params[i].split("=")[1];
                                 if(category.equalsIgnoreCase("gbean")) {
                                     category = "Other";
                                 }
                                 break;
                             }
                         }
                         ArrayList<String> currList = trackedCategoryMap.get(category);
                         if(currList == null) {
                             currList = new ArrayList<String>();
                         }
                         currList.add(mbean);
                         trackedCategoryMap.put(category, currList);
                     }
                     // view all mbeans by category
	                 for (Iterator <String> it = trackedCategoryMap.keySet().iterator(); it.hasNext();) {
	                     String category = it.next().toString();
	                     out.println("<tr><td colspan=2 bgcolor=\"#DCDCDC\"><b>" + category + "</b></td></tr>");
	                     ArrayList<String> mbeanList = trackedCategoryMap.get(category);
	                     for(int i = 0 ; i < mbeanList.size(); i++) {
	                         String prettyBean = null;
	                         boolean found = false;
	                         for(Iterator<String> itt = trackedBeansMap.keySet().iterator(); itt.hasNext() && !found; ) {
	                             String currPrettyBean = itt.next();
	                             if(trackedBeansMap.get(currPrettyBean).equals(mbeanList.get(i))) {
	                                 prettyBean = currPrettyBean;
	                                 found = true;
	                             }
	                         }
	                   %>
			             <tr>
			                 <td width=95% bgcolor="#FFFFFF" nowrap><%=prettyBean%></td>
			                 <td align="right" width=5% bgcolor="#f2f2f2" nowrap>
			                     <a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="stopTrackingMbean" /><portlet:param name="server_id" value="<%=server_id%>" /><portlet:param name="mbean" value="<%=mbeanList.get(i)%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/close-b.png"></a><br>
			                 </td>
			             </tr>
	                 <%
	                     }
	                 }
                 } catch (Exception e) {
                 }
                 %> 
            </table>

            <br>
            <br>
            <table style="border-bottom: 1px solid #2581c7;" width="100%" cellspacing="1" cellpadding="1">
                <tr>
                    <td colspan="2" class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1"><fmt:message key="monitor.server.statAvailable"/></font>
                    </td>
                </tr>

                        
                        <%
                        try {
                            // store all mbeans into a map [category --> list of mbeans)
                            TreeMap<String, ArrayList<String>> availableCategoryMap = new TreeMap<String, ArrayList<String>>();
                            for(Iterator<String> it = availableBeansMap.keySet().iterator(); it.hasNext(); ) {
                                String mbeanPretty = it.next();
                                String mbean = availableBeansMap.get(mbeanPretty); // full mbean name
                                String[] params = mbean.split(",");
                                String category = "Other";
                                for(int i = 0 ; i < params.length; i++) {
                                    if(params[i].startsWith("j2eeType=")) {
                                        // we've found our category, so stop it
                                        category = params[i].split("=")[1];
                                        if(category.equalsIgnoreCase("gbean")) {
                                            category = "Other";
                                        }
                                        break;
                                    }
                                }
                                ArrayList<String> currList = availableCategoryMap.get(category);
                                if(currList == null) {
                                    currList = new ArrayList<String>();
                                }
                                currList.add(mbean);
                                availableCategoryMap.put(category, currList);
                            }
                            // view all mbeans by category
                            for (Iterator <String> it = availableCategoryMap.keySet().iterator(); it.hasNext();) {
                                String category = it.next().toString();
                                out.println("<tr><td colspan=2 bgcolor=\"#DCDCDC\"><b>" + category + "</b></td></tr>");
                                ArrayList<String> mbeanList = availableCategoryMap.get(category);
                                for(int i = 0 ; i < mbeanList.size(); i++) {
                                    String prettyBean = null;
                                    boolean found = false;
                                    for(Iterator<String> itt = availableBeansMap.keySet().iterator(); itt.hasNext() && !found; ) {
                                        String currPrettyBean = itt.next();
                                        if(availableBeansMap.get(currPrettyBean).equals(mbeanList.get(i))) {
                                            prettyBean = currPrettyBean;
                                            found = true;
                                        }
                                    }
                              %>
	                           <tr>
	                               <td width=95% bgcolor="#FFFFFF" nowrap><%=prettyBean%></td>
	                               <td align="right" width=5% bgcolor="#f2f2f2" nowrap>
	                                   <a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="startTrackingMbean" /><portlet:param name="server_id" value="<%=server_id%>" /><portlet:param name="mbean" value="<%=mbeanList.get(i)%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png"></a><br>
	                               </td>     
	                           </tr>
	                        <%
	                            }
                            }
                        } catch (Exception e) {
                        }
                        %>
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
            <a HREF="javascript:history.go(-1)"><< <fmt:message key="monitor.common.back"/></a>
            <p>
            <font face="Verdana" size="+1">
            <fmt:message key="monitor.server.notExistOrDisabled"/>
            </font>
            </p>         

        </td>
     
         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

        <!-- Geronimo Links -->
        <td valign="top">
            <table width="100%" style="border-bottom: 1px solid #2581c7;" cellspacing="1" cellpadding="1">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1"><fmt:message key="monitor.common.nav"/></font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <ul>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showHome" /></portlet:actionURL>"><fmt:message key="monitor.common.home"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllViews" /></portlet:actionURL>"><fmt:message key="monitor.common.view"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllServers" /></portlet:actionURL>"><fmt:message key="monitor.common.server"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllGraphs" /></portlet:actionURL>"><fmt:message key="monitor.common.graph"/></a></li>
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
                        <font face="Verdana" size="+1"><fmt:message key="monitor.common.action"/></font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <ul>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddServer" /></portlet:actionURL>"><fmt:message key="monitor.server.addServer"/></a></li>
                        </ul>
                        &nbsp;<br />
                    </td>   
                </tr>
            </table>

        </td>  
    </tr>
</table>
    <%
    rs.close();
    }%>






