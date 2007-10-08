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

<p>This portlet displays the JMS connectors that are configured with 
the Geronimo server and allows the user to add datasources as well.</p>
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;" width="150" align="right" valign="top">detail</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">This link provides details on the connector.  Clicking on the details link will display information about the Connection Factory in Property/Value pairs. Click on &quot;Back to Datasources&quot; to return to the main JMS Connection Factories page.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px" width="150" align="right" valign="top"><strong>Name</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">Name of the Connection Factory.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px" width="150" align="right" valign="top"><strong>State</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">The state of the connection, either &quot;Running&quot; or 
&quot;Stopped.&quot;</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;" width="150" align="right" valign="top">test connection</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">This link can be used to test the connector. A result will be returned of either &quot;Connected&quot; or 
a failure message.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;" width="150" align="right" valign="top">Add New Datasource</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">Clicking on this link allows the user to add a new datasource. On the add new datasource form, fill in the fields and click on the Create button to add the new datasource. The fields are defined on the form itself.</td>
  </tr>
</table>
