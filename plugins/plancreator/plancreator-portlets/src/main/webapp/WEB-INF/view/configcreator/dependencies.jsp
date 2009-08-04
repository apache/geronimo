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
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt"%>
<fmt:setLocale value="<%=request.getLocale()%>"/>
<fmt:setBundle basename="plancreator-portlet"/>
<portlet:defineObjects />

<p><fmt:message key="plancreator.dependency.title"/></p>

<p><fmt:message key="plancreator.dependency.desc"/></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>DependenciesForm" action="<portlet:actionURL/>" method="POST">
<input type="hidden" name="mode" value="dependencies-after" />

<table border="0">
  <!-- SUBMIT BUTTON -->
  <tr>
    <th>
    <div align="right"></div>
    </th>
    <td><input type="submit" value="<fmt:message key='plancreator.common.next'/>" /></td>
  </tr>

  <!-- Dependencies -->
  <c:forEach var="commonLib" items="${commonLibs}">
    <tr>
      <td><input type="checkbox" name="selectedLibs" id="<portlet:namespace/>selectedLibs-${commonLib}" value="${commonLib}"
        <c:forEach var="dependency" items="${data.environmentConfig.dependencies}">
          <c:if test="${commonLib == dependency}">checked="checked"
          </c:if>
        </c:forEach> />
      </td>
      <td>
      <label for="<portlet:namespace/>selectedLibs-${commonLib}">${commonLib}</label>
      </td>
    </tr>
  </c:forEach>

  <!-- SUBMIT BUTTON -->
  <tr>
    <th>
    <div align="right"></div>
    </th>
    <td><input type="submit" value="<fmt:message key='plancreator.common.next'/>" /></td>
  </tr>
</table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>"><fmt:message key="plancreator.common.cancel"/></a></p>
