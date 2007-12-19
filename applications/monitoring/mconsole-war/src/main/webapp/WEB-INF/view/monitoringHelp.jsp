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
<p><font face="Verdana" size="+1">
<center><b>Welcome to Monitoring Console</b></center>
</font></p>
  <p>The Monitoring Console is designed to monitor a geronimo instance running in a separate JVM. For a quick demo we will look inside the Geronimo instance the Monitoring Console is running on.</p>
<ol>
  <li>Click on 'Monitoring &rarr; Add Server'. Choose:
    <ul style="list-style:none">
      <li>name = localhost</li>
      <li>IP/Hostname = 127.0.0.1</li>
      <li>Port = 4201</li>
      <li>username = system</li>
      <li>password = manager</li>
    </ul>
    The Server 'localhost' will appear 'online' i.e. the monitoring console can talk to it.</li>
  <li>Enable data collection by clicking on 'Enable Query'.</li>
  <li>Click on 'localhost' to view live statistics for chosen MBeans. By default 4/5 Mbeans are chosen for statistics collection. Use '<b>x</b>' to remove MBeans from 'Statistics Collected'. Use '<b>+</b>' in 'Statistics Available' to add an MBean to 'Statistics Collected' MBeans.</li>
  <li>To plot a graph for a statistics click on a link in 'Live Statistics'.
    <ol type="A">
      <li>As an example choose 'JMV Heap Size Current'. Choose the following values:
	<ul style="list-style:none">
	  <li>Name : must be a unique name</li>
	  <li>Description : something</li>
	  <li>X label : The lable for the graph</li>
	  <li>Y label : Y axis for the graph, e.g. JVM Heap - MB</li>
	  <li>Data Series1 :' As is'</li>
          <li>Math Operation :  /100000</li>
	  <li>    Save the graph using 'save'</li>
	</ul>
      <li>To plot a graph for bytes sent each interval click on 'BytesSent' for TomcatWebConnector.  Use the following values:</li>
      <ul style="list-style:none">
	<li>Name : must be a unique name</li>
        <li>Description : something</li>
	<li>X label : The lable for the graph</li>
	<li>Y label : Y axis for the graph, e.g. Bytes Sent/sec</li>
	<li>data series 1 : 'Change(delta)in', i.e. the bytes sent during 5 minutes (300 sec)</li>
	<li>Math Operation :  /300</li>
	<li>Save the graph using 'save'</li>
      </ul>
      Hint : Try 'Request Count' for JettyWebConnector 
    </li></ol>
  <li>Create a view using 'Add view'. Select all the graphs and save the view.</li>
  <li>To see the graphs use 'Show this view'</li>
</ol>
<P>To return to the main Monitoring panel select the "view" link
from the header of this portlet.</P>
