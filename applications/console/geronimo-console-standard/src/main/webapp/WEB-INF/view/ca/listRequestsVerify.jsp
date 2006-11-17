<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<b>Certificate Requests awaiting verification</b>

<p> This screen shows the certificate requests waiting for verification.</p>

<jsp:include page="_header.jsp" />

<table border="0">
    <tr>
        <th class="DarkBackground" align="left">Certificate Requests</th>
    </tr>
  <c:choose>
    <c:when test="${!empty(csrIds)}">
      <c:set var="backgroundClass" value='MediumBackground'/>
      <c:forEach items="${csrIds}" var="csrId">
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
            <a href="<portlet:actionURL portletMode="view">
                       <portlet:param name="mode" value="listRequestsVerify-after"/>
                       <portlet:param name="requestId" value="${csrId}"/>
                    </portlet:actionURL>">${csrId}</a>
          </td>
        </tr>
      </c:forEach>
      </c:when>
    <c:otherwise>
        <tr>
          <td>There are no requests.</td>
        </tr>
    </c:otherwise>
  </c:choose>
</table>
<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>">Back to CA home</a></p>
