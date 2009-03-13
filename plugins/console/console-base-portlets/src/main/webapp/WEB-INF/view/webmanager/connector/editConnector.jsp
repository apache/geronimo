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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>
<script language="JavaScript">
// validate the form submission
var <portlet:namespace/>formName = "<portlet:namespace/>Form";
var <portlet:namespace/>requiredFields = new Array("uniqueName");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="webmanager.common.emptyText"/>');
        return false;    
    }
    
    return <portlet:namespace/>validate();
}

function <portlet:namespace/>validate() {
    with(document.<portlet:namespace/>Form){
        <c:forEach var="connectorAttribute" items="${connectorAttributes}">
            <c:if test="${connectorAttribute.required && connectorAttribute.attributeClass.simpleName ne 'Boolean'}">
            //validate the required attribute has a value
            if(${connectorAttribute.attributeName}.value.length < 1){
                addErrorMessage("<portlet:namespace/>", '<fmt:message key="webmanager.common.emptyText"/>');
                return false;
            }
            </c:if>
            <c:if test="${connectorAttribute.attributeClass.simpleName eq 'Integer'}">
            //validate the Integer attribute has a numeric value
            if(!checkIntegral('<portlet:namespace/>Form', '${connectorAttribute.attributeName}')) {
                addErrorMessage("<portlet:namespace/>", '<fmt:message key="webmanager.common.integer"/>');
                return false;
            }
            </c:if>
        </c:forEach>  
    }

    return true;
}
</script>

<div id="<portlet:namespace/>CommonMsgContainer"></div><br>

<form method="POST" name="<portlet:namespace/>Form" action="<portlet:actionURL/>">
<input type="hidden" name="mode" value="${mode}">
<input type="hidden" name="connectorType" value="${connectorType}">
<input type="hidden" name="containerURI" value="${containerURI}">
<input type="hidden" name="managerURI" value="${managerURI}">
<c:if test="${mode eq 'save'}">
  <input type="hidden" name="connectorURI" value="${connectorURI}">
</c:if>

<!-- Current Task -->
<c:choose>
  <c:when test="${mode eq 'add'}">
    <fmt:message key="webmanager.connector.editConnector.addNew"/> ${connectorType}
  </c:when>
  <c:otherwise>
    <fmt:message key="webmanager.connector.editConnector.editConnector"/> ${uniqueName}
  </c:otherwise>
</c:choose>
<p>
<fmt:message key="webmanager.connector.editConnector.requiredAttribute"/>
<table border="0" cellpadding="3">
<tr>
  <th class="DarkBackground"><fmt:message key="webmanager.connector.editConnector.attribute"/></th>
  <th class="DarkBackground"><fmt:message key="webmanager.connector.editConnector.type"/></th>
  <th class="DarkBackground"><fmt:message key="webmanager.connector.editConnector.value"/></th>
  <th class="DarkBackground"><fmt:message key="webmanager.connector.editConnector.desc"/></th>
</tr>
<tr>
  <td class="LightBackground"><strong>*<fmt:message key="webmanager.common.uniqueName"/></strong></td>
  <td>String</td>
  <td><c:choose>
        <c:when test="${empty connectorURI}">
            <input name="uniqueName" title='<fmt:message key="webmanager.common.uniqueName"/>' type="text" size="30">
        </c:when>
        <c:otherwise>
            <input name="uniqueName" type="hidden" value='<c:out escapeXml="true" value="${uniqueName}"/>'>
            <c:out escapeXml="true" value="${uniqueName}"/>
        </c:otherwise>
      </c:choose>
  </td>
  <td><fmt:message key="webmanager.common.uniqueNameExp"/></td>
</tr>
<c:forEach var="connectorAttribute" items="${connectorAttributes}" varStatus="status">
  <c:set var="style" value="${status.index % 2 == 0 ? 'MediumBackground' : 'LightBackground'}"/>
  <c:set var="enumValues" value="${geronimoConsoleEnumValues[connectorAttribute.attributeName]}"/>
  <tr>
    <td class="${style}">
    <c:if test="${connectorAttribute.required}"><strong>*</c:if>
    <label for="<portlet:namespace/>${connectorAttribute.attributeName}">${connectorAttribute.attributeName}</label>
    <c:if test="${connectorAttribute.required}"></strong></c:if>
    </td>
    <td class="${style}">${connectorAttribute.attributeClass.simpleName}</td>
    <c:choose>
        <c:when test="${enumValues != null}">
    	    <td class="${style}">
    	    <select name="${connectorAttribute.attributeName}" id="<portlet:namespace/>${connectorAttribute.attributeName}">
                <c:if test="${fn:length(connectorAttribute.value) > 0}">
                    <option selected>
                      <c:out escapeXml="true" value="${connectorAttribute.value}"/>
                    </option>
                </c:if>
                <c:forEach var="enumValue" items="${enumValues}">
                    <c:if test="${connectorAttribute.value ne enumValue}">
                        <option>
                          <c:out escapeXml="true" value="${enumValue}"/>
                        </option>
                    </c:if>
                </c:forEach>
    	    </select>
    	     </td>
        </c:when>
        <c:when test="${connectorAttribute.attributeClass.simpleName eq 'Integer'}">
    	    <td class="${style}"><input name="${connectorAttribute.attributeName}" id="<portlet:namespace/>${connectorAttribute.attributeName}" type="text" size="5" 
    	     value="<c:out escapeXml="true" value="${connectorAttribute.stringValue}"/>"></td>
        </c:when>
        <c:when test="${connectorAttribute.attributeClass.simpleName eq 'Boolean'}">
		    <td class="${style}"><input name="${connectorAttribute.attributeName}" id="<portlet:namespace/>${connectorAttribute.attributeName}" type="checkbox" 
		    <c:if test="${connectorAttribute.value}">checked</c:if> /></td>
        </c:when>
        <c:when test="${fn:containsIgnoreCase(connectorAttribute.attributeName, 'pass')}">
		    <td class="${style}"><input name="${connectorAttribute.attributeName}" id="<portlet:namespace/>${connectorAttribute.attributeName}" type="password" size="30"
    	     value="<c:out escapeXml="true" value="${connectorAttribute.stringValue}"/>"></td>
        </c:when>
        <c:otherwise>
		    <td class="${style}"><input name="${connectorAttribute.attributeName}" id="<portlet:namespace/>${connectorAttribute.attributeName}" type="text" size="30"
    	     value="<c:out escapeXml="true" value="${connectorAttribute.stringValue}"/>"></td>
        </c:otherwise>
    </c:choose>
    <td class="${style}"><fmt:message key="${connectorAttribute.description}"/></td>
  </tr>
</c:forEach>
</table>
<P>
<!-- Submit Button -->
<input name="submit" type="submit" value="<fmt:message key="consolebase.common.save"/>" onClick="return <portlet:namespace/>validateForm()">
<input name="reset" type="reset" value="<fmt:message key="consolebase.common.reset"/>">
<input name="submit" type="submit" value="<fmt:message key="consolebase.common.cancel"/>">
</form>
<P>
<a href='<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="list" />
         </portlet:actionURL>'><fmt:message key="webmanager.common.listConnectors"/></a>
