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
<fmt:setBundle basename="consolebase"/>
<p><fmt:message key="repository.help.title"/></p><br>

<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><img src="/console/images/browse.gif" alt="Browse"/></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px"><fmt:message key="repository.help.browseExplanation"/></td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><img src="/console/images/install.gif" alt="Install"/></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px"><fmt:message key="repository.help.installExplanation"/></td>
  </tr>
</table>

<p><fmt:message key="repository.help.addDependencyElement"/>
<pre>
    &lt;dependency&gt;
        &lt;uri&gt;org/codehaus/castor/castor/1.0.5/castor-1.0.5.jar&lt;/uri&gt;
    &lt;/dependency&gt;
</pre>
</p>
