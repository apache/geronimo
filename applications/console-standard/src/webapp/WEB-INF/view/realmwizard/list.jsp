<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>This page lists all the available security realms.  Server-wide security realms can be edited, while security
realms deployed as part of a single application cannot (change the deployment plan in the application instead).</p>

<c:choose>
  <c:when test="${empty(realms)}"><p><i>There are no security realms defined</i></p></c:when>
  <c:otherwise>
<table width="100%">
  <tr>
    <td class="DarkBackground">Name</td>
    <td class="DarkBackground" align="center">Deployed As</td>
    <td class="DarkBackground" align="center">State</td>
    <td class="DarkBackground" align="center">Actions</td>
  </tr>
<c:forEach var="realm" items="${realms}">
  <tr>
    <td>${realm.name}</td>
    <td>
      <c:choose>
        <c:when test="${empty realm.parentName}">
          Server-wide
        </c:when>
        <c:otherwise>
          ${realm.parentName}  <%-- todo: make this a link to an application portlet --%>
        </c:otherwise>
      </c:choose>
    </td>
    <td>${realm.stateName}</td>
    <td>
    <c:if test="${empty realm.parentName}">
         <%--<c:choose>
               <c:when test="${info.stateName eq 'running'}">
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="stop" />
                 <portlet:param name="name" value="${info.objectName}" />
                 <portlet:param name="managerObjectName" value="${container.managerObjectName}" />
                 <portlet:param name="containerObjectName" value="${container.containerObjectName}" />
               </portlet:actionURL>">stop</a>
               </c:when>
               <c:otherwise>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="start" />
                 <portlet:param name="name" value="${info.objectName}" />
                 <portlet:param name="managerObjectName" value="${container.managerObjectName}" />
                 <portlet:param name="containerObjectName" value="${container.containerObjectName}" />
               </portlet:actionURL>">start</a>
               </c:otherwise>
             </c:choose>--%>
      <a href="<portlet:actionURL portletMode="view">
        <portlet:param name="mode" value="editExisting" />
        <portlet:param name="objectName" value="${realm.objectName}" />
      </portlet:actionURL>">edit</a>
           <%--<a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="delete" />
                 <portlet:param name="name" value="${info.objectName}" />
                 <portlet:param name="managerObjectName" value="${container.managerObjectName}" />
                 <portlet:param name="containerObjectName" value="${container.containerObjectName}" />
               </portlet:actionURL>">delete</a>--%>
    </c:if>
    </td>
  </tr>
</c:forEach>
</table>
  </c:otherwise>
</c:choose>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="type" />
            </portlet:actionURL>">Add new security realm</a></p>
