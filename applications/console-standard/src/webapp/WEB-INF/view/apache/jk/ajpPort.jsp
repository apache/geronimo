<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects/>
<p><b>Apache mod_jk</b> -- AJP Port</p>

<!-- FORM TO COLLECT DATA FOR THIS PAGE -->
<form name="<portlet:namespace/>ApacheForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="ajp-after"/>
    <input type="hidden" name="os" value="${model.os}"/>
    <input type="hidden" name="workersPath" value="${model.workersPath}"/>
    <input type="hidden" name="logFilePath" value="${model.logFilePath}"/>
    <c:forEach var="webApp" items="${model.webApps}" varStatus="status">
        <input type="hidden" name="webapp.${status.index}.configId" value="${webApp.configId}"/>
        <input type="hidden" name="webapp.${status.index}.enabled" value="${webApp.enabled}"/>
        <input type="hidden" name="webapp.${status.index}.dynamicPattern" value="${webApp.dynamicPattern}"/>
        <input type="hidden" name="webapp.${status.index}.serveStaticContent" value="${webApp.serveStaticContent}"/>
        <input type="hidden" name="webapp.${status.index}.contextRoot" value="${webApp.contextRoot}"/>
        <input type="hidden" name="webapp.${status.index}.webAppDir" value="${webApp.webAppDir}"/>
    </c:forEach>
    <table border="0">
        <!-- ENTRY FIELD: AJP Port -->
        <tr>
            <th><div align="right">Network Port for AJP:</div></th>
            <td><input name="addAjpPort" type="text" size="5" maxlength="5"
                       value="${model.addAjpPort}"/></td>
        </tr>
        <tr>
            <td></td>
            <td><tt>mod_jk</tt> talks to the Geronimo web container using a protocol called
              AJP.  Currently, you do not have an AJP protocol listener configured for the
              web container.  Select a network port here, and an AJP protocol listener will
              be added for you.</td>
        </tr>

        <!-- SUBMIT BUTTON -->
        <tr>
            <td></td>
            <td><input type="submit" value="Next"/></td>
        </tr>
    </table>
</form>
<!-- END OF FORM TO COLLECT DATA FOR THIS PAGE -->


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>">Cancel</a></p>
