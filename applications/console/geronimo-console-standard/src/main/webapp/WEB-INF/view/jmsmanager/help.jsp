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

<p>This portlet is used to create and remove queues/topics.</p>
<p><span style="font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;">Add Queue/Topic</span></p>
<p>To create a queue/topic click on the &quot;Add Queue/Topic&quot; link. This link brings up a form that allows the user to add a queue or a topic. Fill in the text boxes on the form and click on the &quot;Submit&quot; button. The fields and buttons on the form are defined as follows:</p>
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><strong>Message Destination Name</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">The name of the queue or topic. This is the name used in the admin object gbean name and is linked to by &lt;message-destination-link&gt; in the geronimo-web.xml plan.</td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><strong>Destination Physical Name</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">The name of the connection factory that ActiveMQ knows about.</td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><strong>Type</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">The type of message, either queue or topic.</td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><strong>Application Name</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">This is the &quot;configID&quot; in the geronimo-web.xml file. Or this can be set to the string &quot;null&quot; if the admin object is deployed stand-alone. By default this field contains the string &quot;null.&quot;</td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><strong>Module Name</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">If the module is deployed stand-alone this field is the &quot;configID&quot; in the geronimo-web.xml file. If it is deployed in an application (ear) this field should be the path of the module inside the ear, such as, myResourceAdapter.rar. By default this field contains &quot;defaultJMS.&quot;</td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><img src="/console/images/submit.gif" alt="Submit"/></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">The Submit button creates a new queue or topic and returns the user to the JMS Destination Manager main page.</td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><img src="/console/images/clear.gif" alt="Clear"/></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">The Clear button resets all input fields to the default values.</td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><img src="/console/images/back.gif" alt="Back"/></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">The Back button returns the user to the &quot;JMS Destination Manager&quot; main page without adding a queue or a topic.</td>
  </tr>
</table>
<p>On the main JMS Destination Manager pane, there is an &quot;Actions&quot; column that provides three actions which can be performed on a queue or topic. To remove a queue or topic, click on the &quot;remove&quot; link to the right of the queue/topic to be removed. To view a message, click on the &quot;view messages&quot; link to the right of the queue/topic. To view the dead letter queue, click on the &quot;view DLQ&quot; link.</p>
<p> <strong>Note:</strong> Queues/Topics with no messages will be removed after restarting the server.</p>
