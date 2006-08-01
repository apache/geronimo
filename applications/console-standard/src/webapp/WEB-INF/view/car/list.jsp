<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<p>These are the Geronimo plugins available in the selected repository.  The entries
that are hyperlinks may be installed into the local server.  The other entries are
already available in the local server.</p>

<p><b>Available Plugins:</b></p>

<c:forEach var="category" items="${categories}">
  <p>${category.key}</p>
  <ul>
    <c:forEach var="entry" items="${category.value}">
      <c:choose>
        <c:when test="${entry.installed || !entry.eligible}">
          <li>${entry.name}
              <c:choose>
                  <c:when test="${entry.installed}">
                      (already installed)
                  </c:when>
                  <c:otherwise>
<%--                      <c:if test="${!entry.eligible}">
                          <c:forEach var="prereq" items="${entry.prerequisites}">
                              <c:if test="${!prereq.present}">
                                  (${prereq.moduleIdWithStars} is not installed)
                              </c:if>
                          </c:forEach>
                      </c:if>--%>
                      (Not available; <a href="<portlet:actionURL><portlet:param name="configId" value="${entry.moduleId}"/><portlet:param name="repository" value="${repository}"/><portlet:param name="repo-user" value="${repouser}"/><portlet:param name="repo-pass" value="${repopass}"/><portlet:param name="mode" value="viewForDownload-before"/></portlet:actionURL>">View Details</a>)
                  </c:otherwise>
              </c:choose>
          </li>
        </c:when>
        <c:otherwise>
          <li><a href="<portlet:actionURL><portlet:param name="configId" value="${entry.moduleId}"/><portlet:param name="repository" value="${repository}"/><portlet:param name="repo-user" value="${repouser}"/><portlet:param name="repo-pass" value="${repopass}"/><portlet:param name="mode" value="viewForDownload-before"/></portlet:actionURL>">${entry.name}<c:if test="${entry.name ne entry.moduleId}"> (${entry.version})</c:if></a></li>
        </c:otherwise>
      </c:choose>
    </c:forEach>
  </ul>
</c:forEach>

<p><a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="index-before" /></portlet:actionURL>">Cancel</a></p>
