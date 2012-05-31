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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<CommonMsg:commonMsg/>

<br/>
<table width="100%" class="BlankTableLine" summary="Add OBR URL">
    <tr>
        <td>
            <form id="addForm" method="POST" action="<portlet:actionURL><portlet:param name='action' value='add_url'/></portlet:actionURL>">
                Add an OBR URL:
                <input type="text" id="obrUrl" name="obrUrl" size="80" value=""/>&nbsp;
                <input type="submit" value="Add" />
            </form>
        </td>
    </tr>
</table>


<table width="100%" class="TableLine" summary="OBR Repositories">
    <tr class="DarkBackground">
        <th scope="col" width="35">OSGi Bundle Repository URLs</th>   
        <th scope="col" width="100">Actions</th>
    </tr>

    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="uri" items="${repoURIs}">
      <c:choose>
          <c:when test="${backgroundClass == 'MediumBackground'}" >
              <c:set var="backgroundClass" value='LightBackground'/>
          </c:when>
          <c:otherwise>
              <c:set var="backgroundClass" value='MediumBackground'/>
          </c:otherwise>
      </c:choose>
      
      <tr class="${backgroundClass}" onmouseover="highlightBgColor(this)" onmouseout="recoverBgColor(this)">
        <!-- OBR URLs -->
        <td>&nbsp;${uri}&nbsp;</td>

        <!-- Actions -->
        <td>&nbsp; &nbsp;</td>
        

      </tr>
    </c:forEach>
</table>




