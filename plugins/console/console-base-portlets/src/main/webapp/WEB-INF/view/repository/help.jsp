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
<p>This page displays the artifacts installed in the server's repository. The layout of the repository is the same as that used by Apache Maven making it possible to easily copy files over. The Geronimo Console provides a method for adding artifacts:</p><br>

<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><img src="/console/images/browse.gif" alt="Browse"/></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">Use the Browse button to select the artifact to be added.</td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><img src="/console/images/install.gif" alt="Install"/></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">Click on the Install button to install the artifact.</td>
  </tr>
</table>

<p>To use an artifact in an application, add a dependency element into it's deployment plan. For example, to use Castor XML add the following XML to the plan:
<pre>
    &lt;dependency&gt;
        &lt;uri&gt;org/codehaus/castor/castor/1.0.5/castor-1.0.5.jar&lt;/uri&gt;
    &lt;/dependency&gt;
</pre>
</p>
