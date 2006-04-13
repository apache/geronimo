<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>This page lists all the available JMS Resource Groups.</p>

<c:choose>
  <c:when test="${empty(resources)}"><p><i>There are no JMS Resource Groups defined</i></p></c:when>
  <c:otherwise>
<!--
<p>For each resource listed, you can click the <b>usage</b> link to see examples of how
  to use the connection factories and destinations from your application.</p>
-->
    <table width="100%">
      <c:forEach var="resource" items="${resources}">
        <tr>
            <td colspan="5" style="padding-top: 10px"><b><c:out value="${resource.name}"/> (<c:out value="${resource.configurationName}"/>)</b></td>
        </tr>
        <tr>
          <td class="DarkBackground">Type</td>
          <td class="DarkBackground">Name</td>
          <td class="DarkBackground" align="center">Deployed As</td>
          <td class="DarkBackground" align="center">State</td>
          <td class="DarkBackground" align="center">Actions</td>
        </tr>
        <c:forEach var="factory" items="${resource.connectionFactories}">
            <tr>
              <td>Connection Factory</td>
              <td>${factory.name}</td>
              <td>
                <c:choose>
                  <c:when test="${empty resource.parentName}">
                    Server-wide
                  </c:when>
                  <c:otherwise>
                    Application-scoped
                  </c:otherwise>
                </c:choose>
              </td>
              <td>${factory.stateName}</td>
              <td>
                <%--
                <a href="<portlet:actionURL portletMode="view">
                  <portlet:param name="mode" value="editExisting" />
                  <portlet:param name="adapterObjectName" value="${pool.adapterObjectName}" />
                  <portlet:param name="objectName" value="${pool.factoryObjectName}" />
                </portlet:actionURL>">edit</a>
                <a href="<portlet:actionURL portletMode="view">
                  <portlet:param name="mode" value="usage" />
                  <portlet:param name="name" value="${pool.name}" />
                  <portlet:param name="objectName" value="${pool.factoryObjectName}" />
                </portlet:actionURL>">usage</a>
                Test
                Statistics
                --%>
              </td>
            </tr>
        </c:forEach>
        <c:forEach var="admin" items="${resource.adminObjects}">
            <tr>
              <td>${admin.type}</td>
              <td>${admin.name}</td>
              <td>
                <c:choose>
                  <c:when test="${empty resource.parentName}">
                    Server-wide
                  </c:when>
                  <c:otherwise>
                    Application-scoped
                  </c:otherwise>
                </c:choose>
              </td>
              <td>${admin.stateName}</td>
              <td>
                <%--
                <a href="<portlet:actionURL portletMode="view">
                  <portlet:param name="mode" value="editExisting" />
                  <portlet:param name="adapterObjectName" value="${pool.adapterObjectName}" />
                  <portlet:param name="objectName" value="${pool.factoryObjectName}" />
                </portlet:actionURL>">edit</a>
                <a href="<portlet:actionURL portletMode="view">
                  <portlet:param name="mode" value="usage" />
                  <portlet:param name="name" value="${pool.name}" />
                  <portlet:param name="objectName" value="${pool.factoryObjectName}" />
                </portlet:actionURL>">usage</a>
                Test
                Statistics
                --%>
              </td>
            </tr>
        </c:forEach>
      </c:forEach>
    </table>
  </c:otherwise>
</c:choose>


<p><b>Create a new JMS Resource Group:</b></p>
<ul>
<c:forEach var="provider" items="${providers}">

  <li><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list-after" />
              <portlet:param name="provider" value="${provider.name}" />
            </portlet:actionURL>">For <c:out value="${provider.name}" /></a></li>
</c:forEach>
  <li><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list-after" />
            </portlet:actionURL>">For another JMS provider...</a></li>
<%--
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
--%>
</ul>
