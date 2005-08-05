<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<c:if test="${statsOn}">
<table width="50%">
  <tr><th width="30%">&nbsp;</th><th class="DarkBackground"><strong>Total</strong></th></tr>
  <tr><td align="right" class="LightBackground"><strong>Requests</strong></td><td align="center" class="LightBackground">${requests}</td></tr>
  <tr><td align="right" class="MediumBackground"><strong>Connections</strong></td><td align="center" class="MediumBackground">${connections}</td></tr>
  <tr><td align="right" class="LightBackground"><strong>Errors</strong></td><td align="center" class="LightBackground">${errors}</td></tr>
</table>
<table width="50%">
  <tr><th width="30%">&nbsp;</th><th class="DarkBackground">Count</th><th class="DarkBackground">Average</th><th class="DarkBackground">Max</th></tr>
  <tr><td align="right" class="LightBackground"><strong>Active Requests</strong></td><td align="center" class="LightBackground">${requestsActive}</td><td align="center" class="LightBackground">&nbsp;</td><td align="center" class="LightBackground">${requestsActive}</td></tr>
  <tr><td align="right" class="MediumBackground"><strong>Request Duration</strong></td><td align="center" class="MediumBackground">&nbsp;</td><td align="center" class="MediumBackground">${requestsDurationAve}</td><td align="center" class="MediumBackground">${requestsDurationMax}</td></tr>
  <tr><td align="right" class="LightBackground"><strong>Connections Open</strong></td><td align="center" class="LightBackground">${connectionsOpen}</td><td align="center" class="LightBackground">&nbsp;</td><td align="center" class="LightBackground">${connectionsOpenMax}</td></tr>
  <tr><td align="right" class="MediumBackground"><strong>Connection Requests</strong></td><td align="center" class="MediumBackground">&nbsp;</td><td align="center" class="MediumBackground">${connectionsRequestsAve}</td><td align="center" class="MediumBackground">${connectionsRequestsMax}</td></tr>
  <tr><td align="right" class="LightBackground"><strong>Connection Duration</strong></td><td align="center" class="LightBackground">&nbsp;</td><td align="center" class="LightBackground">${connectionsDurationAve}</td><td align="center" class="LightBackground">${connectionsDurationMax}</td></tr>
  <tr><td colspan="4" align="center"> <a href="<portlet:renderURL/>">refresh</a>&nbsp; <a href="<portlet:actionURL><portlet:param name="stats" value="false"/></portlet:actionURL>">disable</a>&nbsp; <a href="<portlet:actionURL><portlet:param name="resetStats" value="true"/></portlet:actionURL>">reset</a> </td></tr>
</table>
</c:if>
<c:if test="${!statsOn}">
Statistics are not currently being collected. <br/>
<a href="<portlet:actionURL><portlet:param name="stats" value="true"/></portlet:actionURL>">enable</a>
</c:if>
