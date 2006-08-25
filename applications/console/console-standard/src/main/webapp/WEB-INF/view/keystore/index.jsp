<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<p>This tool walks you through the process of configuring keystores to use with
  SSL connectors (for the web container, etc.).</p>

<c:choose>
  <c:when test="${empty(keystores)}"><p><i>There are no keystores defined</i></p></c:when>
  <c:otherwise>
<p>Keystores start out as locked against editing and also not available for usage by
other components in the server.  The <b>Editable</b> flag indicates whether the keystore
has been unlocked for editing (by entering the keystore password), which lasts for the
current login session.  The <b>Available</b> flag indicates whether that password has
been saved in order to make the keystore available to other components in the server.</p>

<% String consoleServletPath = PortletManager.getConsoleFrameworkServletPath(request); %>

<table width="100%">
  <tr>
    <td class="DarkBackground">Keystore File</td>
    <td class="DarkBackground" align="center">Contents</td>
    <td class="DarkBackground" align="center">Editable</td>
    <td class="DarkBackground" align="center">Available</td>
  </tr>
<c:forEach var="keystore" items="${keystores}">
  <tr>
    <td>
      <c:choose>
        <c:when test="${keystore.locked}">
          ${keystore.instance.keystoreName}
        </c:when>
        <c:otherwise>
          <a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="viewKeystore-before" /><portlet:param name="id" value="${keystore.instance.keystoreName}" /></portlet:actionURL>">${keystore.instance.keystoreName}</a>
        </c:otherwise>
      </c:choose>
    </td>
    <td>
        <c:choose>
          <c:when test="${keystore.locked}">
              <i>Keystore locked</i>
          </c:when>
          <c:otherwise>
            ${fn:length(keystore.keys)} Key<c:if test="${fn:length(keystore.keys) != 1}">s</c:if>
              and
            ${fn:length(keystore.certificates)} Cert<c:if test="${fn:length(keystore.certificates) != 1}">s</c:if>
          </c:otherwise>
        </c:choose>
    </td>
    <td>
      <c:choose>
        <c:when test="${keystore.locked}">
          <a href="<portlet:actionURL portletMode="view">
            <portlet:param name="mode" value="unlockEdit-before" />
            <portlet:param name="keystore" value="${keystore.instance.keystoreName}" />
            </portlet:actionURL>"><img src="<%=consoleServletPath%>/../images/ico_lock_16x16.gif" alt="Locked" /></a>
        </c:when>
        <c:otherwise>
          <a href="<portlet:actionURL portletMode="view">
            <portlet:param name="mode" value="lockEdit-before" />
            <portlet:param name="keystore" value="${keystore.instance.keystoreName}" />
            </portlet:actionURL>"><img src="<%=consoleServletPath%>/../images/ico_unlock3_16x16.gif" alt="Unlocked" /></a>
        </c:otherwise>
      </c:choose>
    </td>
    <td>
      <c:choose>
        <c:when test="${keystore.instance.keystoreLocked}">
          <a href="<portlet:actionURL portletMode="view">
            <portlet:param name="mode" value="unlockKeystore-before" />
            <portlet:param name="keystore" value="${keystore.instance.keystoreName}" />
            </portlet:actionURL>"><img src="<%=consoleServletPath%>/../images/ico_lock_16x16.gif" alt="Locked" /></a>
        </c:when>
        <c:otherwise>
          <a href="<portlet:actionURL portletMode="view">
            <portlet:param name="mode" value="lockKeystore-before" />
            <portlet:param name="keystore" value="${keystore.instance.keystoreName}" />
            </portlet:actionURL> "onClick="return confirm('This keystore is currently in use.  Locking it may prevent the server from starting.  Continue?');" ><img src="<%=consoleServletPath%>/../images/ico_unlock3_16x16.gif" alt="Unlocked" /></a>
            ${keys[keystore.instance.keystoreName]}
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