<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<script language="javascript">
<!--
	function doSave(){
		document.datasource_form.action="<portlet:actionURL portletMode="view"/>";
		document.datasource_form.mode.value="save";
		return true;
	}
	function doCancel(){
		document.datasource_form.action="<portlet:actionURL portletMode="view"/>";
		document.datasource_form.mode.value="detail";
		return true;
	}
//-->
</script>
	
<form name="datasource_form">
<input type="hidden" name="name" value="${ds.objectName}" />
<input type="hidden" name="mode" value="detail" />

<br>
<strong>Connection Name:</strong>&nbsp;${ds.name}
<br><br>
<table width="100%">
    <tr><td><strong>UserName</strong></td><td><input type="text" name="user" value="${ds.user}" size="75" /></td></tr>
    <tr><td><strong>Password</strong></td><td><input type="password" name="password1" size="75" /></td></tr>
    <tr><td><strong>Repeat&nbsp;Password</strong></td><td><input type="password" name="password2" size="75" /></td></tr>
    <c:if test="${badPassword}"><tr><td colspan=2">Passwords did not match</td></tr></c:if>
    <tr><td><strong>Server Name</strong></td><td><input type="text" name="serverName" value="${ds.serverName}" size="75" /></td></tr>
    <tr><td><strong>Port no.</strong></td><td><input type="text" name="portNumber" value="${ds.portNumber}" size="75" /></td></tr>
    <tr><td><strong>DB Name</strong></td><td><input type="text" name="ConnectionURL" value="${ds.databaseName}" size="75" /></td></tr>
    <!-- <tr><td><strong>Global JNDI Name</strong></td><td><input type="text" name="globalJNDIName" value="${ds.globalJNDIName}" size="75" /></td></tr> -->
    <tr><td colspan="2">
      <input type="submit" name="btnSave" value="Save" onClick="doSave();"/>
      <input type="submit" name="btnCancel" value="Cancel" onClick="doCancel();">
    </td></tr>
</table>
</form>