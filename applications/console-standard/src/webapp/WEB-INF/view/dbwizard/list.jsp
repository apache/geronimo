<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>This page lists all the available database pools.  Server-wide database pools can be edited, while database
pools deployed as part of a single application cannot (change the deployment plan in the application instead).</p>

<c:choose>
  <c:when test="${empty(pools)}"><p><i>There are no database pools defined</i></p></c:when>
  <c:otherwise>
<table width="100%">
  <tr>
    <td class="DarkBackground">Name</td>
    <td class="DarkBackground" align="center">Deployed As</td>
    <td class="DarkBackground" align="center">State</td>
    <td class="DarkBackground" align="center">Actions</td>
  </tr>
<c:forEach var="pool" items="${pools}">
  <tr>
    <td>${pool.name}</td>
    <td>
      <c:choose>
        <c:when test="${empty pool.parentName}">
          Server-wide
        </c:when>
        <c:otherwise>
          ${pool.parentName}  <%-- todo: make this a link to an application portlet --%>
        </c:otherwise>
      </c:choose>
    </td>
    <td>${pool.stateName}</td>
    <td>
    <c:if test="${empty pool.parentName}">
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
        <portlet:param name="adapterObjectName" value="${pool.adapterObjectName}" />
        <portlet:param name="objectName" value="${pool.factoryObjectName}" />
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

<p>Create a new database pool:</p>
<ul>
  <li><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="rdbms" />
            </portlet:actionURL>">Using the Geronimo database pool wizard</a></li>
  <li><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="startImport" />
              <portlet:param name="importSource" value="JBoss 4" />
              <portlet:param name="from" value="<tt>*-ds.xml</tt> file from the <tt>jboss4/server/name/deploy</tt> directory" />
            </portlet:actionURL>">Import from JBoss 4</a></li>
  <li><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="startImport" />
              <portlet:param name="importSource" value="WebLogic 8.1" />
              <portlet:param name="from" value="<tt>config.xml</tt> file from the WebLogic domain directory" />
            </portlet:actionURL>">Import from WebLogic 8.1</a></li>
</ul>