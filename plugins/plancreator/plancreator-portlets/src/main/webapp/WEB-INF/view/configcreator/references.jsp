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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects />

<p><b>WAR - References</b> -- Resolve EJB, EJB Local, JDBC Connection Pool, JavaMail Session, JMS Connection Factory and 
JMS Destination references</p>

<p>Map the references declared in your Web application to specific items available in the server environment. 
References declared in your web-app (ex. EJB, EJB Local, JDBC Connection Pool, JavaMail Session, JMS Connection Factory and 
JMS Destination references) are shown below to the left and the resources (available in the server environment) 
to which they can be linked are shown to the right.</p>

<!-- References not resolved -->
<c:if test="${data.referenceNotResolved}">
  <p>Some of the references are not resolved. Please resolve them and only then press Next button.
</c:if>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>ReferencesForm" action="<portlet:actionURL/>" method="POST">
<input type="hidden" name="mode" value="references-after" />

<!-- ENTRY FIELD: EJB References -->
<c:if test="${!empty(data.ejbRefs)}">
  <p><b>EJB References:</b></p>
  <table border="0" width="100%">
    <tr>
      <th class="DarkBackground" align="center">EJB Ref</th>
      <th class="DarkBackground" align="center">EJBs Deployed</th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="refData" items="${data.ejbRefs}" varStatus="status">
      <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
      </c:choose>
      <input type="hidden" name="ejbRef.${status.index}.refName" value="${refData.refName}" />
      <tr>
        <td class="${backgroundClass}">
          <div align="right">${refData.refName}</div>
        </td>
        <td class="${backgroundClass}">
          <select name="ejbRef.${status.index}.refLink">
            <c:forEach var="ejb" items="${deployedEjbs}">
              <option value="${ejb.patternName}">${ejb.displayName}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<!-- ENTRY FIELD: EJB Local References -->
<c:if test="${!empty(data.ejbLocalRefs)}">
  <p><b>EJB Local References:</b></p>
  <table border="0" width="100%">
    <tr>
      <th class="DarkBackground" align="center">EJB Local Ref</th>
      <th class="DarkBackground" align="center">EJBs Deployed</th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="refData" items="${data.ejbLocalRefs}" varStatus="status">
      <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
      </c:choose>
      <input type="hidden" name="ejbLocalRef.${status.index}.refName" value="${refData.refName}" />
      <tr>
        <td class="${backgroundClass}">
          <div align="right">${refData.refName}</div>
        </td>
        <td class="${backgroundClass}">
          <select name="ejbLocalRef.${status.index}.refLink">
            <c:forEach var="ejb" items="${deployedEjbs}">
              <option value="${ejb.patternName}">${ejb.displayName}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<!-- ENTRY FIELD: JDBC Connection Pool References -->
<c:if test="${!empty(data.jdbcPoolRefs)}">
  <p><b>JDBC Pool References:</b></p>
  <table border="0" width="100%">
    <tr>
      <th class="DarkBackground" align="center">JDBC Ref</th>
      <th class="DarkBackground" align="center">JDBC Pools</th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="refData" items="${data.jdbcPoolRefs}" varStatus="status">
      <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
      </c:choose>
      <input type="hidden" name="jdbcPoolRef.${status.index}.refName" value="${refData.refName}" />
      <tr>
        <td class="${backgroundClass}">
          <div align="right">${refData.refName}</div>
        </td>
        <td class="${backgroundClass}">
          <select name="jdbcPoolRef.${status.index}.refLink">
          <c:forEach var="jdbcPool" items="${deployedJdbcConnectionPools}">
            <option value="${jdbcPool.patternName}">${jdbcPool.displayName}</option>
          </c:forEach>
          </select>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<!-- ENTRY FIELD: JMS Connection Factory References -->
<c:if test="${!empty(data.jmsConnectionFactoryRefs)}">
  <p><b>JMS Connection Factory References:</b></p>
  <table border="0" width="100%">
    <tr>
      <th class="DarkBackground" align="center">JMS Ref</th>
      <th class="DarkBackground" align="center">JMS Factories</th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="refData" items="${data.jmsConnectionFactoryRefs}" varStatus="status">
      <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
      </c:choose>
      <input type="hidden" name="jmsConnectionFactoryRef.${status.index}.refName" value="${refData.refName}" />
      <tr>
        <td class="${backgroundClass}">
          <div align="right">${refData.refName}</div>
        </td>
        <td class="${backgroundClass}">
          <select name="jmsConnectionFactoryRef.${status.index}.refLink">
            <c:forEach var="jmsFactory" items="${deployedJmsConnectionFactories}">
              <option value="${jmsFactory.patternName}">${jmsFactory.displayName}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<!-- ENTRY FIELD: JMS Destination References -->
<c:if test="${!empty(data.jmsDestinationRefs) || !empty(data.messageDestinations)}">
  <p><b>JMS Destination References:</b></p>
  <table border="0" width="100%">
    <tr>
      <th class="DarkBackground" align="center">JMS Ref</th>
      <th class="DarkBackground" align="center">JMS Destinations</th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="refData" items="${data.jmsDestinationRefs}" varStatus="status">
      <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
      </c:choose>
      <input type="hidden" name="jmsDestinationRef.${status.index}.refName" value="${refData.refName}" />
      <tr>
        <td class="${backgroundClass}">
          <div align="right">${refData.refName}</div>
        </td>
        <td class="${backgroundClass}">
          <select name="jmsDestinationRef.${status.index}.refLink">
            <c:forEach var="jmsDestination" items="${deployedJmsDestinations}">
              <option value="${jmsDestination.patternName}" 
                <c:if test="${fn:startsWith(jmsDestination.displayName, refData.refLink)}"> selected="selected"</c:if>
              >${jmsDestination.displayName}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
    </c:forEach>
    <c:forEach var="refData" items="${data.messageDestinations}" varStatus="status">
      <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
      </c:choose>
      <input type="hidden" name="messageDestination.${status.index}.refName" value="${refData.refName}" />
      <tr>
        <td class="${backgroundClass}">
          <div align="right">${refData.refName}</div>
        </td>
        <td class="${backgroundClass}">
          <select name="messageDestination.${status.index}.refLink">
            <c:forEach var="jmsDestination" items="${deployedJmsDestinations}">
              <option value="${jmsDestination.patternName}" 
                <c:if test="${fn:startsWith(jmsDestination.displayName, refData.refLink)}"> selected="selected"</c:if>
              >${jmsDestination.displayName}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<!-- ENTRY FIELD: JavaMail Session References -->
<c:if test="${!empty(data.javaMailSessionRefs)}">
  <p><b>JavaMail Session References:</b></p>
  <table border="0" width="100%">
    <tr>
      <th class="DarkBackground" align="center">Mail Session Ref</th>
      <th class="DarkBackground" align="center">Mail Sessions Available</th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="refData" items="${data.javaMailSessionRefs}" varStatus="status">
      <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
      </c:choose>
      <input type="hidden" name="javaMailSessionRef.${status.index}.refName" value="${refData.refName}" />
      <tr>
        <td class="${backgroundClass}">
          <div align="right">${refData.refName}</div>
        </td>
        <td class="${backgroundClass}">
          <select name="javaMailSessionRef.${status.index}.refLink">
            <c:forEach var="javaMailSession" items="${deployedJavaMailSessions}">
              <option value="${javaMailSession.patternName}">${javaMailSession.displayName}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<!-- SUBMIT BUTTON -->
<table border="0">
  <tr>
    <th>
    <div align="right"></div>
    </th>
    <td><input type="submit" value="Next" /></td>
  </tr>
</table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>">Cancel</a></p>
