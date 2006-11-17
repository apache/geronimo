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
<p>This portlet allows the user to select a configuration file for logging 
  and/or change the log level and Refresh period. The default &quot;Config file&quot; is a standard log4j file and defines the location where the Geronimo Server
  will log Geronimo output. The configuration file also defines the log 
  level, the maximum log file size, and other attributes associated with 
  logging. <br>
  <br>
  Gereonimo Console will display output from the Geronimo log file in the Server Log
  Viewer portlet in the next window pane.</p>
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="150" align="right" class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;">Config File</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px;">Another configuration file can be used by entering a 
different path and file name in the Config file 
text box.</td>
  </tr>
  <tr>
    <td width="150" align="right" class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;">Refresh Period</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px;">By default Geronimo checks every 60 seconds to see if the
configuration file has changed. The user may modify 
the refresh period.</td>
  </tr>
  <tr>
    <td width="150" align="right" class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;">Log Level</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px;">The user may select a level for logging from the 
drop-down menu. </td>
  </tr>
  <tr>
    <td width="150" align="right" class="MediumBackground" style="padding: 10px 10px 5px 10px; font-weight: bold;"><img src="/console/images/update.gif" width="65" height="20"></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px;">Make changes take effect.</td>
  </tr>
  <tr>
    <td width="150" align="right" class="MediumBackground" style="padding: 10px 10px 5px 10px; font-weight: bold;"><img src="/console/images/reset.gif" width="56" height="20"></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px;">Resets the &quot;Config File&quot;, &quot;Refresh Period&quot; and 
&quot;Log Level&quot; to their settings at the last &quot;Update.&quot;
It does not reset to the Geronimo Server defaults.</td>
  </tr>
</table>
