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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<p>This tool walks you through the process of configuring keystores to use with
  SSL connectors (for the web container, etc.).</p>

<jsp:include page="_header.jsp" />

<c:choose>
  <c:when test="${empty(keystores)}"><p><i>There are no keystores defined</i></p></c:when>
  <c:otherwise>
<p>Keystores start out as locked against editing and also not available for usage by
other components in the server.  The <b>Editable</b> flag indicates whether the keystore
has been unlocked for editing (by entering the keystore password), which lasts for the
current login session.  The <b>Available</b> flag indicates whether that password has
been saved in order to make the keystore available to other components in the server.</p>

<table width="100%">
  <tr>
    <td class="DarkBackground">Keystore File</td>
    <td class="DarkBackground" align="center">Contents</td>
    <td class="DarkBackground" align="center">Editable</td>
    <td class="DarkBackground" align="center">Available</td>
  </tr>
<c:set var="backgroundClass" value='MediumBackground'/>
<c:forEach var="keystore" items="${keystores}">
  <c:choose>
      <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
      </c:when>
      <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
      </c:otherwise>
  </c:choose>
  <tr>
    <td class="${backgroundClass}">
      <c:choose>
        <c:when test="${keystore.lockedEdit}">
          ${keystore.name}
        </c:when>
        <c:otherwise>
          <a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="viewKeystore-before" /><portlet:param name="id" value="${keystore.instance.keystoreName}" /></portlet:actionURL>">${keystore.instance.keystoreName}</a>
        </c:otherwise>
      </c:choose>
    </td>
    <td class="${backgroundClass}">
        <c:choose>
          <c:when test="${keystore.lockedEdit}">
              <i>Keystore locked</i>
          </c:when>
          <c:otherwise>
            ${fn:length(keystore.keys)} Key<c:if test="${fn:length(keystore.keys) != 1}">s</c:if>
              and
            ${fn:length(keystore.certificates)} Cert<c:if test="${fn:length(keystore.certificates) != 1}">s</c:if>
          </c:otherwise>
        </c:choose>
    </td>
    <td class="${backgroundClass}">
      <c:choose>
        <c:when test="${keystore.lockedEdit}">
          <a href="<portlet:actionURL portletMode="view">
            <portlet:param name="mode" value="unlockEdit-before" />
            <portlet:param name="keystore" value="${keystore.name}" />
            </portlet:actionURL>"><img src="/console/images/ico_lock_16x16.gif" alt="Locked" /></a>
        </c:when>
        <c:otherwise>
          <a href="<portlet:actionURL portletMode="view">
            <portlet:param name="mode" value="lockEdit-before" />
            <portlet:param name="keystore" value="${keystore.name}" />
            </portlet:actionURL>"><img src="/console/images/ico_unlock3_16x16.gif" alt="Unlocked" /></a>
        </c:otherwise>
      </c:choose>
    </td>
    <td class="${backgroundClass}">
      <c:choose>
        <c:when test="${keystore.lockedUse}">
          <a href="<portlet:actionURL portletMode="view">
            <portlet:param name="mode" value="unlockKeystore-before" />
            <portlet:param name="keystore" value="${keystore.name}" />
            </portlet:actionURL>"><img src="/console/images/ico_lock_16x16.gif" alt="Locked" /></a>
        </c:when>
        <c:otherwise>
          <a href="<portlet:actionURL portletMode="view">
            <portlet:param name="mode" value="lockKeystore-before" />
            <portlet:param name="keystore" value="${keystore.name}" />
            </portlet:actionURL> "onClick="return confirm('This keystore is currently in use.  Locking it may prevent the server from starting.  Continue?');" ><img src="/console/images/ico_unlock3_16x16.gif" alt="Unlocked" /></a>
            ${keys[keystore.name]}
        </c:otherwise>
      </c:choose>
    </td>
  </tr>
</c:forEach>
</table>
  </c:otherwise>
</c:choose>

<p>
    <a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="createKeystore-before" /></portlet:actionURL>">New Keystore</a>
</p>
