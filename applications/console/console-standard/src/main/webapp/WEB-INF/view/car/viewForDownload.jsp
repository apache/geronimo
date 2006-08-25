<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<h1>${plugin.name}</h1>

<p>Here is the available information on this plugin:</p>

<table border="0">
  <tr>
    <th align="right" valign="top">Name:</th>
    <td>${plugin.name}</td>
  </tr>
  <tr>
    <th align="right" valign="top">Module ID:</th>
    <td>${plugin.moduleId}</td>
  </tr>
  <tr>
    <th align="right" valign="top">Group:</th>
    <td>${plugin.category}</td>
  </tr>
  <tr>
    <th align="right" valign="top">Description:</th>
    <td>${plugin.HTMLDescription}</td>
  </tr>
  <tr>
    <th align="right" valign="top">Author:</th>
    <td>${plugin.author}</td>
  </tr>
  <tr>
    <th align="right" valign="top">Web Site:</th>
    <td><a href="${plugin.pluginURL}">${plugin.pluginURL}</a></td>
  </tr>
  <c:forEach var="license" items="${plugin.licenses}">
      <tr>
        <th align="right" valign="top">License:</th>
        <td>${license.name}
          <c:choose>
              <c:when test="${license.osiApproved}">(Open Source)</c:when>
              <c:otherwise>(Proprietary)</c:otherwise>
          </c:choose>
        </td>
      </tr>
  </c:forEach>
  <tr>
    <th align="right" valign="top">Ger&nbsp;Versions:</th>
    <td>
      ${fn:join(plugin.geronimoVersions, ", ")}
    </td>
  </tr>
  <tr>
    <th align="right" valign="top">JVM Versions:</th>
    <td>
      <c:choose>
          <c:when test="${empty plugin.jvmVersions}">
            <i>Any</i>
          </c:when>
          <c:otherwise>
            ${fn:join(plugin.jvmVersions, ", ")}
          </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
    <th align="right" valign="top">Dependencies:</th>
    <td>
      <c:forEach var="dependency" items="${plugin.dependencies}">
        ${fn:replace(dependency, "//", "/*/")}<br />
      </c:forEach>
    </td>
  </tr>
  <tr>
    <th align="right" valign="top">Prerequisites:</th>
    <td>
      <c:choose>
        <c:when test="${empty plugin.prerequisites}">
          <i>None</i>
        </c:when>
        <c:otherwise>
          <c:forEach var="prereq" items="${plugin.prerequisites}">
            <b>${prereq.moduleIdWithStars}</b> (${prereq.resourceType})<br/>
            ${prereq.description}
            <c:if test="${!prereq.present}">
                <br /><b><font color="red">NOT AVAILABLE</font></b>
            </c:if>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
    <th align="right" valign="top">Obsoletes:</th>
    <td>
      <c:choose>
        <c:when test="${empty plugin.obsoletes}">
          <i>None</i>
        </c:when>
        <c:otherwise>
          <c:forEach var="module" items="${plugin.obsoletes}">
            ${fn:replace(module, "//", "/*/")}<br />
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </td>
  </tr>

</table>

<c:if test="${plugin.eligible}">
<form name="<portlet:namespace/>PluginForm" action="<portlet:actionURL/>">
    <input type="hidden" name="configId" value="${configId}" />
    <input type="hidden" name="mode" value="viewForDownload-after" />
    <input type="hidden" name="repository" value="${repository}" />
    <input type="hidden" name="repo-user" value="${repouser}" />
    <input type="hidden" name="repo-pass" value="${repopass}" />
    <input type="submit" value="Continue" />
</form>
</c:if>

<p><a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="index-before" /></portlet:actionURL>">Cancel</a></p>
