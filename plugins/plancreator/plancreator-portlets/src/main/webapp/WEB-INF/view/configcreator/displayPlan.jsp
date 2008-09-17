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

<%-- $Rev$ $Date$ --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects />

<p><b>Created Plan</b> -- Copy/Edit Deployment Plan</p>

<p>The generated Geronimo Deployment Plan is shown below in an edit box. If you would like to make any changes 
(default should be enough in most scenarios), do it now and then press "Deploy WAR" to deploy the Web application.</p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>DisplayPlan" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="displayPlan-after" />
    <table border="0">
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
            <input type="submit" value="Deploy WAR" />
        </td>
      </tr>
    <!-- STATUS FIELD: Deployment Plan -->
      <tr>
        <th valign="top" style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>deploymentPlan">Deployment Plan</label>:</div></th>
        <td><textarea rows="30" cols="60" name="deploymentPlan" id="<portlet:namespace/>deploymentPlan">${data.deploymentPlan}</textarea></td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
            <input type="submit" value="Deploy WAR" />
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>">Cancel</a></p>
