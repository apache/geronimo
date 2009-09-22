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
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="org.apache.geronimo.monitoring.console.MRCConnector" %>
<%@ page import="org.apache.geronimo.monitoring.console.data.Graph" %>
<%@ page import="org.apache.geronimo.monitoring.console.data.Node" %>
<fmt:setBundle basename="monitor-portlet"/>
<portlet:defineObjects/>

<%
    Graph graph = (Graph) request.getAttribute("graph");
    List<Node> nodes = (List<Node>) request.getAttribute("nodes");

if (graph != null)
{    
    String server_id = graph.getNode().getName();
    Long snapshotDuration = null;

    ArrayList<String> serverIds = new ArrayList<String>();
    ArrayList<String> serverNames = new ArrayList<String>();
%>
<script type = "text/javascript">
var serverBeans = new Object();
var serverPrettyBeans = new Object();
var serverBeanStatAttributes = new Object();
var server_id = "<%=server_id%>";
var mbean = "<%=graph.getMBeanName()%>";
var dataname1 = "<%=graph.getDataName1()%>";
var dataname2 = "<%=graph.getDataName2()%>";

<%
for (Node node: nodes) {
    TreeMap <String,String> trackedBeansMap  ;
    try {
        MRCConnector mrc = new MRCConnector(node);
        trackedBeansMap = mrc.getTrackedBeansMap();;
        serverIds.add(node.getName());
        serverNames.add(node.getName());
        snapshotDuration = mrc.getSnapshotDuration();
        %>
        serverBeans["<%=node.getName()%>"] = new Array();
        serverPrettyBeans["<%=node.getName()%>"] = new Array();
        serverBeanStatAttributes["<%=node.getName()%>"] = new Array();
        <%
        int i = 0;
        for (Iterator <String> it = trackedBeansMap.keySet().iterator(); it.hasNext();)
            {
                String prettyBean = it.next().toString();
                 Set<String> statAttributes = mrc.getStatAttributesOnMBean(trackedBeansMap.get(prettyBean));
            %>
                serverBeans["<%=node.getName()%>"][<%=i%>]="<%=trackedBeansMap.get(prettyBean)%>";
                serverPrettyBeans["<%=node.getName()%>"][<%=i%>]="<%=prettyBean%>";
                serverBeanStatAttributes["<%=node.getName()%>"][<%=i%>] = new Array();
                <%
                int j = 0;
                for (Iterator <String> itt = statAttributes.iterator(); itt.hasNext();)
                {
                    %>
                    serverBeanStatAttributes["<%=node.getName()%>"][<%=i%>][<%=j%>]="<%=itt.next().toString()%>";
                    <%
                    j++;
                }
                i++;
            }       
         %>
             
        <%
        mrc.dispose();
    }
    catch (Exception e)
    {
        
    }
    %>
    
<%}

%>
</script>

<!-- <head> -->

    <style type='text/css'>
    </style>
    <script type = "text/javascript">
<!--

function hide(x) {
document.getElementById(x).style.display='none';
}
function show(x) {
document.getElementById(x).style.display='';
}
function validate(duration) 
{
   if (! (document.editGraph.name.value  
      && document.editGraph.dataname1.value
      && document.editGraph.mbean.value
      && document.editGraph.server_id.value
      && document.editGraph.timeframe.value ))
   {
      alert("Server, Name, Data Series, MBean and Timeframe are all required fields");
      return false;
   }
   // ensure that the timeframe is at least 2*(snapshotduration)
   if(duration * 2 > document.editGraph.timeframe.value) {
        alert("Timeframe needs to be at least " + 2 * duration);
        return false;
   }
   if (document.editGraph.operation.value == 'other')
   {
       if (!document.editGraph.othermath.value)
       {
            alert("If operation is other, field must contain a math operation");
            return false;
       }
       var mathChars= /[\+\-\*\/]/;
       if (!document.editGraph.othermath.value.match(mathChars)) {
            alert("Operation must use at least one math symbol (+, -, *, /)");
            return false;
        }
        if (document.editGraph.dataname2.value != "")
        {
            if (document.editGraph.othermath.value.length > 1) 
            {
                if (!document.editGraph.othermath.value.charAt(0).match(mathChars) || !document.editGraph.othermath.value.charAt(document.editGraph.othermath.value.length - 1).match(mathChars))
                {
                    alert("Operation must create a valid formula");
                    return false;
                }
            }
                if (document.editGraph.othermath.value.length == 2) 
                {
                    alert("Operation must create a valid formula");
                    return false;
                }
        }
        else
        {
            if (document.editGraph.othermath.value.length > 1) 
            {
                if (!document.editGraph.othermath.value.charAt(0).match(mathChars) || document.editGraph.othermath.value.charAt(document.editGraph.othermath.value.length - 1).match(mathChars))
                {
                    alert("Operation must create a valid formula");
                    return false;
                }
            }
            else if (document.editGraph.othermath.value.length == 1) 
            {
                alert("Operation must create a valid formula");
                return false;
            }
        }
        
    }
}
function noAlpha(obj){
    reg = /[^0-9]/g;
    obj.value =  obj.value.replace(reg,"");
 }
 
 function noAlphaMath(obj){
    reg = /[^0-9,\-,\+,\*,\/,\.]/g;
    obj.value =  obj.value.replace(reg,"");
 }
 
 function clearList(selectbox)
{
    var i;
    for(i=selectbox.options.length-1;i>=0;i--)
    {
        selectbox.remove(i);
    }
}


function addOption(selectbox, value, text )
{
    var optn = document.createElement("OPTION");
    optn.text = text;
    optn.value = value;
    if (selectbox == document.editGraph.mbean)
    {
        if (value==mbean)
            optn.selected = "selected";
    }
    else if (selectbox == document.editGraph.dataname2 && (document.editGraph.dataname2.disabled = ""))
    {
        if (value=="time")
            optn.selected = "selected";
    }
    else if (selectbox == document.editGraph.dataname1)
    {
        if (value==dataname1)
            optn.selected = "selected";
    }
    else if (selectbox == document.editGraph.dataname2)
    {
        if (value==dataname2)
            optn.selected = "selected";
    }
    selectbox.options.add(optn);
}
 
 
 function updateMbeanList(){
    clearList(document.editGraph.mbean);
    if (document.editGraph.server_id.value)
    {
        addOption(document.editGraph.mbean, "", "-Select MBean-");
        for (var i = 0; i < serverPrettyBeans[document.editGraph.server_id.value].length; i++)
        {
            addOption(document.editGraph.mbean, serverBeans[document.editGraph.server_id.value][i], serverPrettyBeans[document.editGraph.server_id.value][i]);
        }
    }
    else
        addOption(document.editGraph.mbean, "", "-Select Server First-");
    updateDatanameList();
 }
 
  function updateDatanameList(){
    clearList(document.editGraph.dataname1);
    if (document.editGraph.mbean.value)
    {
        addOption(document.editGraph.dataname1, "", "-Select Data Name-");
        for (var i = 0; i < serverBeanStatAttributes[document.editGraph.server_id.value][document.editGraph.mbean.selectedIndex-1].length; i++)
        {
            addOption(document.editGraph.dataname1, serverBeanStatAttributes[document.editGraph.server_id.value][document.editGraph.mbean.selectedIndex-1][i], serverBeanStatAttributes[document.editGraph.server_id.value][document.editGraph.mbean.selectedIndex-1][i]);
        }
    }
    else
        addOption(document.editGraph.dataname1, "", "-Select MBean First-");
    updateDataname2List();
 }
 
   function updateDataname2List(){
    clearList(document.editGraph.dataname2);
    if (document.editGraph.mbean.value)
    {
        if (document.editGraph.operation.value=="other")
            addOption(document.editGraph.dataname2, "", "none");
        addOption(document.editGraph.dataname2, "time", "Time");
        for (var i = 0; i < serverBeanStatAttributes[document.editGraph.server_id.value][document.editGraph.mbean.selectedIndex-1].length; i++)
        {
            addOption(document.editGraph.dataname2, serverBeanStatAttributes[document.editGraph.server_id.value][document.editGraph.mbean.selectedIndex-1][i], serverBeanStatAttributes[document.editGraph.server_id.value][document.editGraph.mbean.selectedIndex-1][i]);
        }
    }
    else
        addOption(document.editGraph.dataname2, "", "-Select MBean First-");
        
     if (document.editGraph.operation.value=="")
     {
        document.editGraph.data2operation.selectedIndex=0;
        document.editGraph.dataname2.selectedIndex=0;
        document.editGraph.data2operation.disabled="disabled";
        document.editGraph.dataname2.disabled="disabled";
     }
 }
 
   function checkOtherMath(){
    if (document.editGraph.operation.value=="other")
    {
        document.editGraph.othermath.style.display='';
    }
    else
        document.editGraph.othermath.style.display='none';
   if (document.editGraph.operation.value=="")
   {
        document.editGraph.data2operation.selectedIndex=0;
        document.editGraph.dataname2.selectedIndex=0;
        document.editGraph.data2operation.disabled="disabled";
        document.editGraph.dataname2.disabled="disabled";
   }
   else
   {
       document.editGraph.dataname2.disabled="";
       document.editGraph.data2operation.disabled="";
       updateDataname2List();
       checkNoData2();
   }

 }
  function checkNoData2()
 {
    if (document.editGraph.dataname2.value == "")
    {
        document.editGraph.data2operation.selectedIndex = 0;
        document.editGraph.data2operation.disabled="disabled";
    }
    else
    {
        document.editGraph.data2operation.disabled="";
    }
 }
 function getObject(obj) {
  if(document.all) {
    if(typeof obj=="string") {
      return document.all(obj);
    } else {
      return obj.style;
    }
  }
  if(document.getElementById) {
    if(typeof obj=="string") {
      return document.getElementById(obj);
    } else {
      return obj.style;
    }
  }
  return null;
}
 
    function updateFormula()
    {
        var exitObj=getObject('formulaData1operation');
        if (document.editGraph.dataname1.value)
        {
            if (document.editGraph.data1operation.value == 'D')
                exitObj.innerHTML = "(Delta)";
            else
                exitObj.innerHTML = "";
            exitObj=getObject('formulaDataname1');
            exitObj.innerHTML = document.editGraph.dataname1.value;
            if (document.editGraph.operation.value)
            {
                if (document.editGraph.operation.value == "other")
                {
                    exitObj=getObject('formulaOperation');
                    exitObj.innerHTML = document.editGraph.othermath.value;
                }
                else
                {
                    exitObj=getObject('formulaOperation');
                    exitObj.innerHTML = document.editGraph.operation.value;
                }
                exitObj=getObject('formulaData2operation');
                if (document.editGraph.data2operation.value == 'D')
                    exitObj.innerHTML = "(Delta)";
                else
                    exitObj.innerHTML = "";
                exitObj=getObject('formulaDataname2');
                exitObj.innerHTML = document.editGraph.dataname2.value;
            }
            else
            {
                exitObj=getObject('formulaOperation');
                exitObj.innerHTML = "";
                exitObj=getObject('formulaData2operation');
                exitObj.innerHTML = "";
                exitObj=getObject('formulaDataname2');
                exitObj.innerHTML = "";
            }
            
        }
        else
        {
            exitObj=getObject('formulaData1operation');
            exitObj.innerHTML = "";
            exitObj=getObject('formulaDataname1');
            exitObj.innerHTML = "";
            exitObj=getObject('formulaOperation');
            exitObj.innerHTML = "";
            exitObj=getObject('formulaData2operation');
            exitObj.innerHTML = "";
            exitObj=getObject('formulaDataname2');
            exitObj.innerHTML = "";
        }
    }

//-->
</script>
<!-- </head> -->
        
<CommonMsg:commonMsg/><br>

<table>
    <tr>
        <!-- Body -->
        <td width="90%" align="left" valign="top">
            <p>
            <font face="Verdana" size="+1">
            <fmt:message key="monitor.common.editing"/>: <%=graph.getGraphName1()%>
            </font>
            </p>         
            <p>
   <form onsubmit="return validate(<%=snapshotDuration/1000/60%>);" name="editGraph" method="POST" action="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="saveEditGraph"/></portlet:actionURL>">
   <table cellpadding="1" cellspacing="1">
      <%--<tr>--%>
      <%--<td>Added:</td>--%>
      <%--<td>&nbsp;</td>--%>
      <%--<td align="right"><%=added%></td>--%>
      <%--<td></td>--%>
    <%--</tr>--%>
    <%--<tr>--%>
      <%--<td>Last Modified:</td>--%>
      <%--<td>&nbsp;</td>--%>
      <%--<td align="right"><%=modified%></td>--%>
      <%--<td></td>--%>
    <%--</tr>--%>
    <%--<tr>--%>
      <%--<td>Last Seen:</td>--%>
      <%--<td>&nbsp;</td>--%>
      <%--<td align="right"><%=last_seen%></td>--%>
      <%--<td></td>--%>
    <%--</tr>--%>
   <tr>
      <td><label for="<portlet:namespace/>server_id"><fmt:message key="monitor.graph.server"/></label>:</td>
      <td>&nbsp;</td>
      <td align="right">
    <select name="server_id" id="<portlet:namespace/>server_id" onChange="updateMbeanList(); updateFormula();">
      <option value="">-<fmt:message key="monitor.graph.selectServer"/>-</option>
    </select>    
    <script type='text/javascript'>
    <% 
    for (int i = 1; i < serverIds.size()+1; i++)
    {
    %>
        document.editGraph.server_id.options[<%=i%>]=new Option("<%=serverNames.get(i-1)%>", "<%=serverIds.get(i-1)%>", <%if (server_id.equals(serverIds.get(i-1))){%>true<%}else{%>false<%}%>);
    <%
    }%>
    </script>
    </td>
      <td></td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>name"><fmt:message key="monitor.common.name"/></label>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" name="name" id="<portlet:namespace/>name" value="<%=graph.getGraphName1()%>"></td>
      <td></td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>description"><fmt:message key="monitor.common.desc"/></label>:</td>
      <td>&nbsp;</td>
      <td align="right"><textarea rows="5" cols="50" name="minxss_description" id="<portlet:namespace/>description"><%=graph.getDescription()%></textarea></td>
      <td></td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>xlabel"><fmt:message key="monitor.graph.x"/></label>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" name="xlabel" id="<portlet:namespace/>xlabel" value="<%=graph.getXlabel()%>"/></td>
      <td></td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>ylabel"><fmt:message key="monitor.graph.y"/></label>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" name="ylabel" id="<portlet:namespace/>ylabel" value="<%=graph.getYlabel()%>"/></td>
      <td></td>
    </tr>
    <tr>
        <td><label for="<portlet:namespace/>timeframe"><fmt:message key="monitor.graph.time"/></label>:</td>
        <td>&nbsp;</td>
        <td align="right"><input type="text" width="5" size="4" name="timeframe" id="<portlet:namespace/>timeframe" onKeyUp='noAlpha(this)' onKeyPress='noAlpha(this)' value="<%=graph.getTimeFrame()%>"/></td>
        <td> <fmt:message key="monitor.common.minute"/></td>
      </tr>
    <tr>
      <td><label for="<portlet:namespace/>mbean">Mbean</label>:</td>
      <td>&nbsp;</td>
      <td align="right">
    <select name="mbean" id="<portlet:namespace/>mbean" onChange="updateDatanameList(); updateFormula();">
      <option value="">-<fmt:message key="monitor.graph.selectServerFirst"/>-</option>
    </select>
    </td>
      <td></td>
    </tr>
    <tr>
      <td><fmt:message key="monitor.graph.data"/>:</td>
      <td>&nbsp;</td>
      <td align="right">
      <select name="data1operation" title="data operation" onchange="updateFormula();">
        <option value="A" <%if (graph.getData1operation()=='A'){  %>selected="selected"<%} %>><fmt:message key="monitor.graph.asis"/></option>
        <option value="D" <%if (graph.getData1operation()=='D'){  %>selected="selected"<%} %>><fmt:message key="monitor.graph.change"/></option>
      </select><label for="<portlet:namespace/>dataname1"><fmt:message key="monitor.common.name"/></label>
      <select name="dataname1" id="<portlet:namespace/>dataname1" onchange="updateFormula();">
        <option value="">-<fmt:message key="monitor.graph.selectMbeanFirst"/>-</option>
      </select>
      </td>
      <td></td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>operation"><fmt:message key="monitor.graph.math"/></label>:</td>
      <td>&nbsp;</td>
        <td align="right">
      <select name="operation" id="<portlet:namespace/>operation" onChange="checkOtherMath(); updateFormula();">
        <option value="" <%if (graph.getOperation().equals("null") || graph.getOperation().equals("") || graph.getOperation() == null){  %>selected="selected"<%} %>><fmt:message key="monitor.common.none"/></option>
        <option value="+" <%if (graph.getOperation().equals("+")){  %>selected="selected"<%} %>>+</option>
        <option value="-" <%if (graph.getOperation().equals("-")){  %>selected="selected"<%} %>>-</option>
        <option value="*" <%if (graph.getOperation().equals("*")){  %>selected="selected"<%} %>>*</option>
        <option value="/" <%if (graph.getOperation().equals("/")){  %>selected="selected"<%} %>>/</option>
        <option value="other" <%if (!graph.getOperation().equals("null") && !graph.getOperation().equals("+") && !graph.getOperation().equals("-") && !graph.getOperation().equals("*") && !graph.getOperation().equals("/") && !graph.getOperation().equals("") && graph.getOperation() != null){  %>selected="selected"<%} %>><fmt:message key="monitor.common.other"/></option>
      </select>
      </td>
      <td><input type="text" <%if (!graph.getOperation().equals("null") && !graph.getOperation().equals("+") && !graph.getOperation().equals("-") && !graph.getOperation().equals("*") && !graph.getOperation().equals("/") && !graph.getOperation().equals("") && graph.getOperation() != null){  %><%} else {%>style="display: none;"<%}%> width="6" size="8" name="othermath" title="other operation" onKeyUp='noAlphaMath(this); updateFormula();' onKeyPress='noAlphaMath(this); updateFormula();' value="<%=graph.getOperation() %>"/></td>
    </tr>
    <tr>
      <td><fmt:message key="monitor.graph.data"/> 2:</td>
      <td>&nbsp;</td>
      <td align="right">
      <select name="data2operation" title="data operation" <%if (graph.getOperation().equals("null") || graph.getOperation().equals("") || graph.getOperation() == null){  %>disabled="disabled"<%} %> onchange="updateFormula();">
        <option value="A" <%if (graph.getData2operation()=='A'){  %>selected="selected"<%} %>><fmt:message key="monitor.graph.asis"/></option>
        <option value="D" <%if (graph.getData2operation()=='D'){  %>selected="selected"<%} %>><fmt:message key="monitor.graph.change"/></option>
      </select><label for="<portlet:namespace/>dataname2"><fmt:message key="monitor.common.name"/></label>
      <select name="dataname2" id="<portlet:namespace/>dataname2" <%if (graph.getOperation().equals("null") || graph.getOperation().equals("") || graph.getOperation() == null){  %>disabled="disabled"<%} %> onchange=" checkNoData2(); updateFormula();">
        <option value="">-<fmt:message key="monitor.graph.selectOpFirst"/>-</option>
      </select>
      <script type='text/javascript'>
        updateMbeanList();
      </script>
      </td>
      <td></td>
    </tr>
    <tr>
        <td></td>
        <td></td>
        <td>
            <% if(graph.isShowArchive()) { %>
                <input type="checkbox" name="showArchive" id="<portlet:namespace/>showArchive" checked><label for="<portlet:namespace/>showArchive"><fmt:message key="monitor.graph.showArchive"/></label>
            <% } else { %>
                <input type="checkbox" name="showArchive" id="<portlet:namespace/>showArchive"><label for="<portlet:namespace/>showArchive"><fmt:message key="monitor.graph.showArchive"/></label>
            <% } %>
        </td>
        <td></td>
    </tr>
    <tr><td><fmt:message key="monitor.common.graphing"/>: </td><td colspan="2"><strong><span id="formulaData1operation"></span> <span id="formulaDataname1"></span> <span id="formulaOperation"></span> <span id="formulaData2operation"></span> <span id="formulaDataname2"></span></strong></td></tr>
    <tr><td colspan="3"><font size="-2">&nbsp;</font></td></tr>
    <tr>
      <td colspan="1" align="left"><button type="button" value="Cancel" onclick="javascript:history.go(-1)"><fmt:message key="monitor.common.cancel"/></button></td>
      <td>&nbsp;</td>
      <td colspan="1" align="right"><input type="submit" value="<fmt:message key="monitor.common.save"/>" /></td>
      <td></td>
    </tr>
  </table>
  <input name="graph_id" type="hidden" value="<%=graph.getIdString()%>">
  </form>
      <script type='text/javascript'>
        updateFormula();
        checkNoData2();
      </script>
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
                            <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="deleteGraph" /><portlet:param name="graph_id" value="<%=graph.getIdString()%>" /></portlet:actionURL>"><fmt:message key="monitor.graph.deleteGraph"/></a></li>
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
            <a HREF="javascript:history.go(-1)"><< <fmt:message key="monitor.common.back"/></a>
            <p>
            <font face="Verdana" size="+1">
            <fmt:message key="monitor.graph.notExist"/>
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
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddGraph" /></portlet:actionURL>"><fmt:message key="monitor.graph.addGraph"/></a></li>
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


