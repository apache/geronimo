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
<%@ page import="org.apache.geronimo.monitoring.console.data.Graph" %>
<%@ page import="org.apache.geronimo.monitoring.console.data.Node" %>
<portlet:defineObjects/>

<%

String message = (String) request.getAttribute("message");


if (message == null)
    message = new String("");

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
   if (! (document.addView.name.value 
      && document.addView.description.value  ))
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
        
            <%
 if (!message.equals(""))
 {
 %>
<div align="left" style="width: 650px">
<%=message %><br>
</div>
<%} %>
<table>
    <tr>
        <!-- Body -->
        <td width="100%" align="left" valign="top">
            <p>
            <font face="Verdana" size="+1">
            Adding a new view
            </font>
            </p>         
            <p>
  <form onsubmit="return validate();" name="addView" method="POST" action="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="saveAddView"/></portlet:actionURL>">
  <table cellpadding="1" cellspacing="1">
    <tr>
      <td><label for="<portlet:namespace/>name">Name</label>:</td>
      <td>&nbsp;</td>
      <td align="right"><input size="50" type="text" name="name" id="<portlet:namespace/>name" value=""></td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>description">Description</label>:</td>
      <td>&nbsp;</td>
      <td align="right"><textarea rows="5" cols="50" name="description" id="<portlet:namespace/>description"></textarea></td>
    </tr>
    <tr>
      <td>Graphs:</td>
      <td>&nbsp;</td>
      <td align="right">
          <table cellpadding="1" cellspacing="1">
          <tr>
          <th width="5%"></th>
          <th>Name</th>
          <th>Timeframe</th>
          <th width="20%">Server</th>
          <th>Edit</th>
          </tr>
      <%
          List<Graph> graphs = (List<Graph>) request.getAttribute("graphs");
          for (Graph graph :graphs) {
              Node node = graph.getNode();
              if (node != null)
              {
      %>     
            <tr>
            <td align="left" width="5%"><input type="checkbox" name="graph_ids" title="<%=node.getName()%>-<%=graph.getGraphName1()%>" value='<%=graph.getIdString()%>'></td>
            <td align="left"><a href="javascript: void(0)" onClick="openNewWindow('/monitoring/popUpGraph?graph_id=<%=graph.getIdString()%>','graph','width=800,height=300','title=<%=graph.getGraphName1()%>')"><%=graph.getGraphName1()%></a></td>
            <td align="left"><%=graph.getTimeFrame()%> min.</td>
            <td align="left"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showServer" /><portlet:param name="server_id" value='<%=node.getName()%>' /></portlet:actionURL>"><%=node.getName()%></a></td>
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
      <td colspan="1" align="left"><button type="button" value="Cancel" onclick="javascript:history.go(-1)">Cancel</button></td>
      <td>&nbsp;</td>
      <td colspan="1" align="right"><input type="submit" value="Save" /></td>
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
            
        </td>        
    </tr>
</table>





