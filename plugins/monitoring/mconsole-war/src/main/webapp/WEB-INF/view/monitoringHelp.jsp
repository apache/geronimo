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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="monitor-portlet"/>
<p><font face="Verdana" size="+1">
<center><b><fmt:message key="monitor.help.title"/></b></center>
</font></p>
  <p><fmt:message key="monitor.help.desc"/></p>
<ol>
  <li><fmt:message key="monitor.edit.msg01"/>:
    <ul style="list-style:none">
      <li><fmt:message key="monitor.common.name"/> = localhost</li>
      <li><fmt:message key="monitor.server.ip"/>/<fmt:message key="monitor.server.hostname"/> = 127.0.0.1</li>
      <li><fmt:message key="monitor.server.port"/> = 1099</li>
      <li><fmt:message key="monitor.server.username"/> = system</li>
      <li><fmt:message key="monitor.server.pwd"/> = manager</li>
    </ul>
    <fmt:message key="monitor.edit.msg02"/></li>
  <li><fmt:message key="monitor.edit.msg03"/></li>
  <li><fmt:message key="monitor.edit.msg04"/></li>
  <li><fmt:message key="monitor.edit.msg05"/>
    <ol type="A">
      <li><fmt:message key="monitor.edit.msg06"/>:
	<ul style="list-style:none">
	  <li><fmt:message key="monitor.common.name"/> : <fmt:message key="monitor.edit.msg07"/></li>
	  <li><fmt:message key="monitor.common.desc"/> : <fmt:message key="monitor.common.st"/></li>
	  <li><fmt:message key="monitor.graph.x"/> : <fmt:message key="monitor.edit.msg08"/></li>
	  <li><fmt:message key="monitor.graph.y"/> : <fmt:message key="monitor.edit.msg09"/></li>
	  <li><fmt:message key="monitor.graph.data"/> :' <fmt:message key="monitor.graph.asis"/>'</li>
          <li><fmt:message key="monitor.graph.math"/> :  /100000</li>
	  <li>    <fmt:message key="monitor.edit.msg10"/></li>
	</ul>
      <li><fmt:message key="monitor.edit.msg11"/>:</li>
      <ul style="list-style:none">
	<li><fmt:message key="monitor.common.name"/> : <fmt:message key="monitor.edit.msg07"/></li>
        <li><fmt:message key="monitor.common.desc"/> : <fmt:message key="monitor.common.st"/></li>
	<li><fmt:message key="monitor.graph.x"/> : <fmt:message key="monitor.edit.msg08"/></li>
	<li><fmt:message key="monitor.graph.y"/> : <fmt:message key="monitor.edit.msg12"/></li>
	<li><fmt:message key="monitor.graph.data"/> 1 : '<fmt:message key="monitor.graph.change"/>', <fmt:message key="monitor.edit.msg13"/></li>
	<li><fmt:message key="monitor.graph.math"/> :  /300</li>
	<li><fmt:message key="monitor.edit.msg10"/></li>
      </ul>
      <fmt:message key="monitor.edit.msg14"/> 
    </li></ol>
  <li><fmt:message key="monitor.edit.msg15"/></li>
  <li><fmt:message key="monitor.edit.msg16"/></li>
</ol>
<P><fmt:message key="monitor.common.ret"/></P>
