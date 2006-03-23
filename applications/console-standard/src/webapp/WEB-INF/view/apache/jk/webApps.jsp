<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects/>
<p><b>Apache mod_jk</b> -- Web App Selection</p>

<p>For each web application available in Geronimo, select:</p>
<dl>
  <dt>Through Apache</dt>
  <dd>Whether the web application should be exposed through Apache</dd>
  <dt>Static Content</dt>
  <dd>Whether Apache should serve static content for the web application (instead of all
    content being handled by Geronimo)</dd>
  <dt>Dynamic Paths</dt>
  <dd>If Apache is serving static content, which URL paths should be passed to Geronimo
      (e.g. <tt>/servlet/*</tt> or <tt>/sample/*.jsp</tt>)</dd>
</dl>

<!-- FORM TO COLLECT DATA FOR THIS PAGE -->
<form name="<portlet:namespace/>ApacheForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="webapp-after"/>
    <input type="hidden" name="os" value="${model.os}"/>
    <input type="hidden" name="addAjpPort" value="${model.addAjpPort}"/>
    <input type="hidden" name="workersPath" value="${model.workersPath}"/>
    <input type="hidden" name="logFilePath" value="${model.logFilePath}"/>
<c:forEach var="webApp" items="${model.webApps}" varStatus="status">
    <input type="hidden" name="webapp.${status.index}.configId" value="${webApp.configId}"/>
    <input type="hidden" name="webapp.${status.index}.contextRoot" value="${webApp.contextRoot}"/>
    <input type="hidden" name="webapp.${status.index}.webAppDir" value="${webApp.webAppDir}"/>
</c:forEach>
    <table border="0">
        <tr>
            <th>Web Application</th>
            <th>Through Apache</th>
            <th>Static Content</th>
            <th>Dynamic Paths</th>
        </tr>
      <c:forEach var="web" items="${webApps}" varStatus="status">
        <tr>
            <td>${web.configID}</td>
            <td align="center"><input type="checkbox" name="webapp.${status.index}.enabled"<c:if test="${model.webApps[status.index].enabled}"> checked="checked"</c:if> /></td>
            <td align="center"><input type="checkbox" name="webapp.${status.index}.serveStaticContent"<c:if test="${model.webApps[status.index].serveStaticContent}"> checked="checked"</c:if> /></td>
            <td><input type="text" name="webapp.${status.index}.dynamicPattern" size="20" maxlength="250"
                       value="${model.webApps[status.index].dynamicPattern}"/></td>
        </tr>
      </c:forEach>

        <!-- SUBMIT BUTTON -->
        <tr>
            <td></td>
            <td colspan="3"><input type="submit" value="Finish"/></td>
        </tr>
    </table>
</form>
<!-- END OF FORM TO COLLECT DATA FOR THIS PAGE -->


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>">Cancel</a></p>
