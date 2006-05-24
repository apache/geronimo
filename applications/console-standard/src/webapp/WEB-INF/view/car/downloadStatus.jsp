<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>Processing ${configId}...</p>


<form name="<portlet:namespace/>ContinueForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="downloadStatus-after" />
    <input type="hidden" name="configId" value="${configId}" />
    <input type="hidden" name="repository" value="${repository}" />
    <input type="hidden" name="repo-user" value="${repouser}" />
    <input type="hidden" name="repo-pass" value="${repopass}" />
</form>

<jsp:include flush="true" page="../ajax/progressbar.jsp"/>

<script type="text/javascript">
    <portlet:namespace/>startProgress();
</script>
<%--
<p><a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="index-before" /></portlet:actionURL>">Cancel</a></p>
--%>