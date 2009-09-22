
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
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg"%>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.geronimo.monitoring.console.data.Graph" %>
<fmt:setBundle basename="monitor-portlet"/>
<portlet:defineObjects/>
<script language="JavaScript" type="text/javascript">
<!--
function openNewWindow(theURL,winName,features) {
  window.open(theURL,winName,features);
}
//-->
</script>

<CommonMsg:commonMsg/><br>

<table>
   <tr>
       <!-- Body -->
       <td width="100%" align="left" valign="top">
<table width="100%" style="border-style: solid;
border-width: 1px;">
 <thead align="center"><strong><fmt:message key="monitor.common.graph"/></strong></thead>
 <tr>
  <th class="DarkBackground" width="30%"><fmt:message key="monitor.common.name"/></th>
  <th class="DarkBackground" width="20%"><fmt:message key="monitor.graph.server"/></th>
  <th class="DarkBackground" width="15%"><fmt:message key="monitor.graph.time"/></th>
  <th class="DarkBackground" width="20%"><fmt:message key="monitor.graph.data"/></th>
  <th class="DarkBackground" width="15%"><fmt:message key="monitor.common.action"/></th>
 </tr>
 <%

     List<Graph> graphs = (List<Graph>) request.getAttribute("graphs");
     for (Graph graph: graphs) {
 %>
  <c:set var="backgroundClass" value='MediumBackground'/>
  <c:choose>
      <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
      </c:when>
      <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
      </c:otherwise>
  </c:choose>
 <tr>
 <%
 if (graph.getNode().isEnabled())
 {
 	%>
  	<td class="${backgroundClass}" width="30%" align="center">
    <a href="javascript: void(0)" onClick="openNewWindow('/monitoring/popUpGraph?graph_id=<%=graph.getIdString()%>','graph','width=800,height=300','title=<%=graph.getGraphName1() %>')"><%=graph.getGraphName1() %></a>
    </td>
  	<td class="${backgroundClass}" width="20%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showServer" /><portlet:param name="server_id" value="<%=graph.getNode().getName()%>" /></portlet:actionURL>"><%=graph.getNode().getName()%></a></td>
  	<td class="${backgroundClass}" width="15%" align="center"><%=graph.getTimeFrame()%></td>
  	<td class="${backgroundClass}" width="20%" align="center"><%=graph.getDataName1()%><%if (graph.getOperation() != null && !graph.getOperation().equals("null")){%><%=graph.getOperation()%><%}%><%if (graph.getDataName2() != null && !graph.getDataName2().equals("null")){%><%=graph.getDataName2()%><%}%></td>
       <%if(request.isUserInRole("admin")){ %>
  	<td class="${backgroundClass}" width="15%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditGraph" /><portlet:param name="graph_id" value="<%=graph.getIdString()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit"><fmt:message key="monitor.common.edit"/></a></td>
<%} }
else
{
	%>
	<td class="${backgroundClass}" width="30%" align="center"><%=graph.getGraphName1()%></td>
  	<td class="${backgroundClass}" width="20%" align="center"><%=graph.getNode().getName()%></td>
  	<td class="${backgroundClass}" width="15%" align="center"><%=graph.getTimeFrame()%></td>
  	<td class="${backgroundClass}" width="20%" align="center"><%=graph.getDataName1()%><%if (graph.getOperation() != null && !graph.getOperation().equals("null")){%><%=graph.getOperation()%><%}%><%if (graph.getDataName2() != null && !graph.getDataName2().equals("null")){%><%=graph.getDataName2()%><%}%></td>
       <%if(request.isUserInRole("admin")){ %>
  	<td class="${backgroundClass}" width="15%" align="center"><img
					border=0 src="/monitoring/images/edit-b.png" alt="edit"><fmt:message key="monitor.common.edit"/></td>
	<%
}
        }%>
 </tr>
 <%}
%>
</table>
 <%if(request.isUserInRole("admin")){ %>
<div align="right"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddGraph" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png" alt="Add Graph"><fmt:message key="monitor.graph.addGraph"/></a></div>
<%}
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
            
        </td>        
    </tr>
</table>
