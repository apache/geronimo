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
<%@ page import="org.apache.geronimo.monitoring.console.Constants" %>
<%@ page import="org.apache.geronimo.monitoring.console.data.Graph" %>
<%@ page import="org.apache.geronimo.monitoring.console.data.View" %>
<%@ page import="java.util.List,java.util.ArrayList"%>
<fmt:setBundle basename="monitor-portlet"/>
<portlet:defineObjects/>

<%
    View view = (View) request.getAttribute("view");
    if (view != null) {
%>
<!-- <head> -->

    <script type='text/javascript' src='<%=Constants.DOJO_JS%>'>
    </script>
        <script type = "text/javascript">
<!--
function hide(x) {
document.getElementById(x).style.display='none';
}
function show(x) {
document.getElementById(x).style.display='';
}
function validate() {
   if (! (document.editView.name.value 
      && document.editView.minxss_description.value  ))
   {
      alert("Name and Description are required fields");
      return false;
   }
   return;
}


function openNewWindow(theURL,winName,features) {
  window.open(theURL,winName,features);
}

//-->
</script>
<!-- </head> -->
        
<CommonMsg:commonMsg/><br>

<table>
    <tr>
        <!-- Body -->
        <td width="100%" align="left" valign="top">
            <p>
            <font face="Verdana" size="+1">
            <fmt:message key="monitor.common.editing"/>: <%=view.getName()%>
            </font>
            </p>         
            <p>
  <form onsubmit="return validate();" name="editView" method="POST" action="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="saveEditView"/><portlet:param name="view_id" value="<%=view.getIdString()%>"/></portlet:actionURL>">
  <table cellpadding="1" cellspacing="1">
    <%--<tr>--%>
      <%--<td>Added:</td>--%>
      <%--<td>&nbsp;</td>--%>
      <%--<td align="right"><%=added%></td>--%>
    <%--</tr>--%>
    <%--<tr>--%>
      <%--<td>Last Modified:</td>--%>
      <%--<td>&nbsp;</td>--%>
      <%--<td align="right"><%=modified%></td>--%>
    <%--</tr>--%>
    <tr>
      <td><label for="<portlet:namespace/>name"><fmt:message key="monitor.common.name"/></label>:</td>
      <td>&nbsp;</td>
      <td align="right"><input size="50" type="text" name="name" id="<portlet:namespace/>name" value="<%=view.getName()%>"></td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>description"><fmt:message key="monitor.common.desc"/></label>:</td>
      <td>&nbsp;</td>
      <td align="right"><textarea rows="5" cols="50" name="minxss_description" id="<portlet:namespace/>description"><%=view.getDescription()%></textarea></td>
    </tr>
    <tr>
      <td valign="top"><fmt:message key="monitor.common.graph"/>:</td>
      <td>&nbsp;</td>
      <td align="right">
            <table cellpadding="1" cellspacing="1">
            <tr>
            <th width="5%"></th>
            <th><fmt:message key="monitor.common.name"/></th>
            <th><fmt:message key="monitor.graph.time"/></th>
            <th width="20%"><fmt:message key="monitor.graph.server"/></th>
            <th><fmt:message key="monitor.common.edit"/></th>
            </tr>
      <%
          List<Integer> owned_ids = new ArrayList<Integer>();
          for(Graph graph : view.getGraphs())
          {
             owned_ids.add(graph.getId());
          }
          List<Graph> graphs = (List<Graph>) request.getAttribute("graphs");
          for (Graph graph: graphs) {
              if (graph.getNode() != null)
              {
      %>     
            <tr>
            <td align="left" width="5%"><input type="checkbox" name="graph_ids" title="<%=graph.getGraphName1()%> - <%=graph.getNode().getName()%>" id="<portlet:namespace/>graph_ids" value='<%=graph.getIdString()%>' <%if (owned_ids.contains(graph.getId())){%> checked<%}%>></td>
        <%--<td align="left" width="5%"><input type="checkbox" name="graph_ids" title="<%=graph.getGraphName1()%> - <%=graph.getNode().getName()%>" id="<portlet:namespace/>graph_ids" value='<%=graph.getIdString()%>' <%if (true){%> checked<%}%>></td>--%>
            <td align="left"><a href="javascript: void(0)" onClick="openNewWindow('/monitoring/popUpGraph?graph_id=<%=graph.getIdString()%>','graph','width=800,height=300','title=<%=graph.getGraphName1() %>')"><%=graph.getGraphName1()%></a></td>
            <td align="left"><%="" + graph.getTimeFrame()%> min.</td>
            <td align="left"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showServer" /><portlet:param name="server_id" value='<%=graph.getNode().getName()%>' /></portlet:actionURL>"><%=graph.getNode().getName()%></a></td>
            <td align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditGraph" /><portlet:param name="graph_id" value='<%=graph.getIdString()%>' /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="edit"></a></td>
            </tr>
      <%

              }
          }
      %>
            </table>
</td>
    </tr>
    <tr><td colspan="2"><font size="-2">&nbsp;</font></td></tr>
    <tr>
      <td colspan="1" align="left"><button type="button" value="<fmt:message key="monitor.common.cancel"/>" onclick="javascript:history.go(-1)"><fmt:message key="monitor.common.cancel"/></button></td>
      <td>&nbsp;</td>
      <td colspan="1" align="right"><input type="submit" value="<fmt:message key="monitor.common.save"/>" /></td>
    </tr>
  </table>
  </form>


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
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showView" /><portlet:param name="view_id" value="<%=view.getIdString()%>" /></portlet:actionURL>"><fmt:message key="monitor.view.show"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddGraph" /></portlet:actionURL>"><fmt:message key="monitor.graph.createGraph"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="deleteView" /><portlet:param name="view_id" value="<%=view.getIdString()%>" /></portlet:actionURL>"><fmt:message key="monitor.view.delete"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddView" /></portlet:actionURL>"><fmt:message key="monitor.view.add"/></a></li>
                        </ul>
                        &nbsp;<br />
                    </td>   
                </tr>
            </table>
            
        </td>        
    </tr>
</table>
<%} else {%>
<table>
    <tr>
        <!-- Body -->
        <td width="90%" align="left" valign="top">
            <a HREF="javascript:history.go(-1)"><< <fmt:message key="monitor.common.back"/></a>
            <p>
            <font face="Verdana" size="+1">
            <fmt:message key="monitor.view.notExist"/>
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
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddView" /></portlet:actionURL>"><fmt:message key="monitor.view.add"/></a></li>
                        </ul>
                        &nbsp;<br />
                    </td>   
                </tr>
            </table>

        </td>  
    </tr>
</table>
    <%
    }%>




