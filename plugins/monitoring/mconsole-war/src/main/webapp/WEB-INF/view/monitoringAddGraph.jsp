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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="org.apache.geronimo.monitoring.console.MRCConnector" %>
<%@ page import="org.apache.geronimo.monitoring.console.data.Node" %>
<fmt:setBundle basename="monitor-portlet"/>
<portlet:defineObjects/>

<%
    String mbean = (String) request.getAttribute("mbean");
    if (mbean == null)
        mbean = "";
    String dataname1 = (String) request.getAttribute("dataname");
    if (dataname1 == null)
        dataname1 = "";
    String server_id = (String) request.getAttribute("server_id");
    if (server_id == null)
        server_id = "";
    Long snapshotDuration = 5L;

    List<Node> nodes = (List<Node>) request.getAttribute("nodes");

    ArrayList<String> serverIds = new ArrayList<String>();
    ArrayList<String> serverNames = new ArrayList<String>();

%>
<script type="text/javascript">
    var serverBeans = new Object();
    var serverPrettyBeans = new Object();
    var serverBeanStatAttributes = new Object();
    var server_id = "<%=server_id%>";
    var mbean = "<%=mbean%>";
    var dataname1 = "<%=dataname1%>";


    <%
for (Node node: nodes) {
    try {
    String name = node.getName();
   MRCConnector  mrc = new MRCConnector(node);
    TreeMap <String,String> trackedBeansMap = mrc.getTrackedBeansMap();
    serverIds.add(name);
    serverNames.add(name +" - "+node.getHost());
    snapshotDuration = mrc.getSnapshotDuration();
    %>
    serverBeans["<%=name%>"] = new Array();
    serverPrettyBeans["<%=name%>"] = new Array();
    serverBeanStatAttributes["<%=name%>"] = new Array();
    <%
    int i = 0;
    for(Map.Entry<String, String> entry: trackedBeansMap.entrySet()){
    String prettyBean = entry.getKey();
    Set<String> statAttributes = mrc.getStatAttributesOnMBean(entry.getValue());
    %>
    serverBeans["<%=name%>"][<%=i%>] = "<%=entry.getValue()%>";
    serverPrettyBeans["<%=name%>"][<%=i%>] = "<%=prettyBean%>";
    serverBeanStatAttributes["<%=name%>"][<%=i%>] = new Array();
    <%
    int j = 0;
    for(String statAttribute: statAttributes){
    %>
    serverBeanStatAttributes["<%=name%>"][<%=i%>][<%=j%>] = "<%=statAttribute%>";
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
        e.printStackTrace();
    }
    %>

    <%}

    %>
</script>

<style type='text/css'>
</style>
<script type="text/javascript">
<!--

function hide(x) {
    document.getElementById(x).style.display = 'none';
}
function show(x) {
    document.getElementById(x).style.display = '';
}
function validate(duration)
{
    if (! (document.addGraph.name.value
            && document.addGraph.dataname1.value
            && document.addGraph.mbean.value
            && document.addGraph.server_id.value
            && document.addGraph.timeframe.value ))
    {
        alert("Server, Name, Data Series, MBean and Timeframe are all required fields");
        return false;
    }
   // ensure that the timeframe is at least 2*(snapshotduration)
    if (duration * 2 > document.addGraph.timeframe.value) {
        alert("Timeframe needs to be at least " + 2 * duration);
        return false;
    }
    if (document.addGraph.operation.value == 'other')
    {
        if (!document.addGraph.othermath.value)
        {
            alert("If operation is other, field must contain a math operation");
            return false;
        }
        var mathChars = /[\+\-\*\/]/;
        if (!document.addGraph.othermath.value.match(mathChars)) {
            alert("Operation must use at least one math symbol (+, -, *, /)");
            return false;
        }
        if (document.addGraph.dataname2.value != "")
        {
            if (document.addGraph.othermath.value.length > 1)
            {
                if (!document.addGraph.othermath.value.charAt(0).match(mathChars) || !document.addGraph.othermath.value.charAt(document.addGraph.othermath.value.length - 1).match(mathChars))
                {
                    alert("Operation must create a valid formula");
                    return false;
                }
            }
            if (document.addGraph.othermath.value.length == 2)
            {
                alert("Operation must create a valid formula");
                return false;
            }
        }
        else
        {
            if (document.addGraph.othermath.value.length > 1)
            {
                if (!document.addGraph.othermath.value.charAt(0).match(mathChars) || document.addGraph.othermath.value.charAt(document.addGraph.othermath.value.length - 1).match(mathChars))
                {
                    alert("Operation must create a valid formula");
                    return false;
                }
            }
            else if (document.addGraph.othermath.value.length == 1)
            {
                alert("Operation must create a valid formula");
                return false;
            }
        }

    }

}
function noAlpha(obj) {
    reg = /[^0-9]/g;
    obj.value = obj.value.replace(reg, "");
}

function noAlphaMath(obj) {
    reg = /[^0-9,\-,\+,\*,\/,\.]/g;
    obj.value = obj.value.replace(reg, "");
}

function clearList(selectbox)
{
    var i;
    for (i = selectbox.options.length - 1; i >= 0; i--)
    {
        selectbox.remove(i);
    }
}


function addOption(selectbox, value, text, test)
{
    //TODO why doesn't this work?
    //    var optn = new Option(text, value, value.equals(test));
    //    selectbox.options.add(optn);
    var optn = document.createElement("OPTION");
    optn.text = text;
    optn.value = value;
    if (value == test) {
        optn.selected = "selected";
    }
    selectbox.options.add(optn);
}

function updateMbeanList() {
    clearList(document.addGraph.mbean);
    if (document.addGraph.server_id.value)
    {
        addOption(document.addGraph.mbean, "", "-Select MBean-", "-");
        for (var i = 0; i < serverPrettyBeans[document.addGraph.server_id.value].length; i++)
        {
            addOption(document.addGraph.mbean, serverBeans[document.addGraph.server_id.value][i], serverPrettyBeans[document.addGraph.server_id.value][i], mbean);
        }
    }
    else
        addOption(document.addGraph.mbean, "", "-Select Server First-", "-");
    updateDatanameList();
}

function updateDatanameList() {
    clearList(document.addGraph.dataname1);
    if (document.addGraph.mbean.value)
    {
        addOption(document.addGraph.dataname1, "", "-Select Data Name-", "-");
        for (var i = 0; i < serverBeanStatAttributes[document.addGraph.server_id.value][document.addGraph.mbean.selectedIndex - 1].length; i++)
        {
            addOption(document.addGraph.dataname1, serverBeanStatAttributes[document.addGraph.server_id.value][document.addGraph.mbean.selectedIndex - 1][i], serverBeanStatAttributes[document.addGraph.server_id.value][document.addGraph.mbean.selectedIndex - 1][i], dataname1);
        }
    }
    else
        addOption(document.addGraph.dataname1, "", "-Select MBean First-", "-");
    updateDataname2List();
}

function updateDataname2List() {
    clearList(document.addGraph.dataname2);
    if (document.addGraph.mbean.value)
    {
        if (document.addGraph.operation.value == "other")
            addOption(document.addGraph.dataname2, "", "none", "-");
        addOption(document.addGraph.dataname2, "time", "Time", "time");
        for (var i = 0; i < serverBeanStatAttributes[document.addGraph.server_id.value][document.addGraph.mbean.selectedIndex - 1].length; i++)
        {
            addOption(document.addGraph.dataname2, serverBeanStatAttributes[document.addGraph.server_id.value][document.addGraph.mbean.selectedIndex - 1][i], serverBeanStatAttributes[document.addGraph.server_id.value][document.addGraph.mbean.selectedIndex - 1][i], "time");
        }
    }
    else
        addOption(document.addGraph.dataname2, "", "-Select MBean First-", "-");

    if (document.addGraph.operation.value == "")
    {
        document.addGraph.data2operation.selectedIndex = 0;
        document.addGraph.dataname2.selectedIndex = 0;
        document.addGraph.data2operation.disabled = "disabled";
        document.addGraph.dataname2.disabled = "disabled";
    }
}

function checkOtherMath() {
    if (document.addGraph.operation.value == "other")
    {
        document.addGraph.othermath.style.display = '';
    }
    else
        document.addGraph.othermath.style.display = 'none';
    if (document.addGraph.operation.value == "")
    {
        document.addGraph.data2operation.selectedIndex = 0;
        document.addGraph.dataname2.selectedIndex = 0;
        document.addGraph.data2operation.disabled = "disabled";
        document.addGraph.dataname2.disabled = "disabled";
    }
    else
    {
        document.addGraph.dataname2.disabled = "";
        document.addGraph.data2operation.disabled = "";
        updateDataname2List();
        checkNoData2();
    }

}

function checkNoData2()
{
    if (document.addGraph.dataname2.value == "")
    {
        document.addGraph.data2operation.selectedIndex = 0;
        document.addGraph.data2operation.disabled = "disabled";
    }
    else
    {
        document.addGraph.data2operation.disabled = "";
    }
}

function getObject(obj) {
    if (document.all) {
        if (typeof obj == "string") {
            return document.all(obj);
        } else {
            return obj.style;
        }
    }
    if (document.getElementById) {
        if (typeof obj == "string") {
            return document.getElementById(obj);
        } else {
            return obj.style;
        }
    }
    return null;
}

function updateFormula()
{
    var exitObj = getObject('formulaData1operation');
    if (document.addGraph.dataname1.value)
    {
        if (document.addGraph.data1operation.value == 'D')
            exitObj.innerHTML = "(Delta)";
        else
            exitObj.innerHTML = "";
        exitObj = getObject('formulaDataname1');
        exitObj.innerHTML = document.addGraph.dataname1.value;
        if (document.addGraph.operation.value)
        {
            if (document.addGraph.operation.value == "other")
            {
                exitObj = getObject('formulaOperation');
                exitObj.innerHTML = document.addGraph.othermath.value;
            }
            else
            {
                exitObj = getObject('formulaOperation');
                exitObj.innerHTML = document.addGraph.operation.value;
            }
            exitObj = getObject('formulaData2operation');
            if (document.addGraph.data2operation.value == 'D')
                exitObj.innerHTML = "(Delta)";
            else
                exitObj.innerHTML = "";
            exitObj = getObject('formulaDataname2');
            exitObj.innerHTML = document.addGraph.dataname2.value;
        }
        else
        {
            exitObj = getObject('formulaOperation');
            exitObj.innerHTML = "";
            exitObj = getObject('formulaData2operation');
            exitObj.innerHTML = "";
            exitObj = getObject('formulaDataname2');
            exitObj.innerHTML = "";
        }

    }
    else
    {
        exitObj = getObject('formulaData1operation');
        exitObj.innerHTML = "";
        exitObj = getObject('formulaDataname1');
        exitObj.innerHTML = "";
        exitObj = getObject('formulaOperation');
        exitObj.innerHTML = "";
        exitObj = getObject('formulaData2operation');
        exitObj.innerHTML = "";
        exitObj = getObject('formulaDataname2');
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
<p><font face="Verdana" size="+1"> <fmt:message key="monitor.graph.addGraph"/> </font></p>

<p>

<form onsubmit="return validate(<%=snapshotDuration/1000/60%>);"
      name="addGraph" method="POST"
      action="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="saveAddGraph"/></portlet:actionURL>">
<table cellpadding="1" cellspacing="1">
<tr>
    <td><label for="<portlet:namespace/>server_id"><fmt:message key="monitor.graph.server"/></label>:</td>
    <td>&nbsp;</td>
    <td align="right"><select name="server_id" id="<portlet:namespace/>server_id"
                              onChange="updateMbeanList(); updateFormula();">
        <option value="">-<fmt:message key="monitor.graph.selectServer"/>-</option>
    </select>
        <script type='text/javascript'>
            <%
            for (int i = 1; i < serverIds.size()+1; i++)
            {
            %>
            document.addGraph.server_id.options[<%=i%>] = new Option("<%=serverNames.get(i-1)%>", "<%=serverIds.get(i-1)%>", <%if (server_id.equals(serverIds.get(i-1))){%>true<%}else{%>false<%}%>);
            <%
            }%>
        </script>
    </td>
    <td></td>
</tr>
<tr>
    <td><label for="<portlet:namespace/>name"><fmt:message key="monitor.common.name"/></label>:</td>
    <td>&nbsp;</td>
    <td align="right"><input type="text" name="name" id="<portlet:namespace/>name" value=""></td>
    <td></td>
</tr>
<tr>
    <td><label for="<portlet:namespace/>description"><fmt:message key="monitor.common.desc"/>:</label></td>
    <td>&nbsp;</td>
    <td align="right"><textarea rows="5" cols="50"
                                name="minxss_description" id="<portlet:namespace/>description"></textarea></td>
    <td></td>
</tr>
<tr>
    <td><label for="<portlet:namespace/>xlabel"><fmt:message key="monitor.graph.x"/></label>:</td>
    <td>&nbsp;</td>
    <td align="right"><input type="text" name="xlabel" id="<portlet:namespace/>xlabel" value=""/></td>
    <td></td>
</tr>
<tr>
    <td><label for="<portlet:namespace/>ylabel"><fmt:message key="monitor.graph.y"/></label>:</td>
    <td>&nbsp;</td>
    <td align="right"><input type="text" name="ylabel" id="<portlet:namespace/>ylabel" value=""/></td>
    <td></td>
</tr>
<tr>
    <td><label for="<portlet:namespace/>timeframe"><fmt:message key="monitor.graph.time"/></label>:</td>
    <td>&nbsp;</td>
    <td align="right"><input type="text" width="5" size="4" id="<portlet:namespace/>timeframe"
                             name="timeframe" onKeyUp='noAlpha(this)' onKeyPress='noAlpha(this)'
                             value="60"/></td>
    <td><fmt:message key="monitor.common.minute"/></td>
</tr>
<tr>
    <td><label for="<portlet:namespace/>mbean">Mbean</label>:</td>
    <td>&nbsp;</td>
    <td align="right"><select name="mbean" id="<portlet:namespace/>mbean"
                              onChange="updateDatanameList(); updateFormula();">
        <option value="">-<fmt:message key="monitor.graph.selectServerFirst"/>-</option>
    </select></td>
    <td></td>
</tr>
<tr>
    <td><label for="<portlet:namespace/>dataname1"><fmt:message key="monitor.graph.data"/></label>:</td>
    <td>&nbsp;</td>
    <td align="right"><select name="data1operation" title="data operation"
                              onchange="updateFormula();">
        <option value="A" selected="selected"><fmt:message key="monitor.graph.asis"/></option>
        <option value="D"><fmt:message key="monitor.graph.change"/></option>
    </select> <select name="dataname1" id="<portlet:namespace/>dataname1" onchange="updateFormula();">
        <option value="">-<fmt:message key="monitor.graph.selectMbeanFirst"/>-</option>
    </select></td>
    <td></td>
</tr>
<tr>
    <td><label for="<portlet:namespace/>operation"><fmt:message key="monitor.graph.math"/></label>:</td>
    <td>&nbsp;</td>
    <td align="right"><select name="operation" id="<portlet:namespace/>operation"
                              onChange="checkOtherMath(); updateFormula();">
        <option value="" selected="selected"><fmt:message key="monitor.common.none"/></option>
        <option value="+">+</option>
        <option value="-">-</option>
        <option value="*">*</option>
        <option value="/">/</option>
        <option value="other"><fmt:message key="monitor.common.other"/></option>
    </select></td>
    <td><input type="text" style="display: none;" width="6"
               size="8" name="othermath" title="Other match operation"
               onKeyUp='noAlphaMath(this); updateFormula();'
               onKeyPress='noAlphaMath(this); updateFormula();' value=""/></td>
</tr>
<tr>
    <td><label for="<portlet:namespace/>dataname2"><fmt:message key="monitor.graph.data"/> 2</label>:</td>
    <td>&nbsp;</td>
    <td align="right"><select name="data2operation" title="data operation"
                              disabled="disabled" onchange="updateFormula();">
        <option value="A" selected="selected"><fmt:message key="monitor.graph.asis"/></option>
        <option value="D"><fmt:message key="monitor.graph.change"/></option>
    </select> <select name="dataname2" id="<portlet:namespace/>dataname2" disabled="disabled"
                      onchange="updateFormula(); checkNoData2();">
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
    <td>&nbsp;</td>
    <td align="right"><input type="checkbox" name="showArchive" id="<portlet:namespace/>showArchive"><label for="<portlet:namespace/>showArchive"><fmt:message key="monitor.graph.showArchive"/></label></td>
    <td></td>
</tr>
<tr>
    <td><fmt:message key="monitor.common.graphing"/>:</td>
    <td colspan="2"><strong><span
            id="formulaData1operation"></span> <span id="formulaDataname1"></span>
        <span id="formulaOperation"></span> <span id="formulaData2operation"></span>
        <span id="formulaDataname2"></span></strong></td>
</tr>
<tr>
    <td colspan="3"><font size="-2">&nbsp;</font></td>
</tr>
<tr>
    <td colspan="1" align="left">
        <button type="button" value="<fmt:message key="monitor.common.cancel"/>"
                onclick="javascript:history.go(-1)"><fmt:message key="monitor.common.cancel"/>
        </button>
    </td>
    <td>&nbsp;</td>
    <td colspan="1" align="right"><input type="submit" value="<fmt:message key="monitor.common.add"/>"/></td>
    <td></td>
</tr>
</table>
</form>
<script type='text/javascript'>
    updateFormula();
    checkNoData2();
</script>
</td>

<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

<!-- Geronimo Links -->
<td valign="top">

    <table width="100%" style="border-bottom: 1px solid #2581c7;"
           cellspacing="1" cellpadding="1">
        <tr>
            <td class="DarkBackground" align="left" nowrap><font
                    face="Verdana" size="+1"><fmt:message key="monitor.common.nav"/></font></td>
        </tr>
        <tr>
            <td bgcolor="#FFFFFF" nowrap>&nbsp;<br/>
                <ul>
                    <li><a
                            href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showHome" /></portlet:actionURL>"><fmt:message key="monitor.common.home"/></a></li>
                    <li><a
                            href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllViews" /></portlet:actionURL>"><fmt:message key="monitor.common.view"/></a></li>
                    <li><a
                            href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllServers" /></portlet:actionURL>"><fmt:message key="monitor.common.server"/></a></li>
                    <li><a
                            href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllGraphs" /></portlet:actionURL>"><fmt:message key="monitor.common.graph"/></a></li>
                </ul>
                &nbsp;<br/>
            </td>
        </tr>
    </table>

</td>
</tr>
</table>




