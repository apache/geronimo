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
<strong>Connection Name:</strong>&nbsp;${ds.jndiName}
<br><br>
<table width="100%">
    <tr><td><strong>UserName</strong></td><td><input type="text" name="UserName" value="${ds.userName}" size="75" /></td></tr>
    <tr><td><strong>Password</strong></td><td><input type="password" name="password1" size="75" /></td></tr>
    <tr><td><strong>Repeat&nbsp;Password</strong></td><td><input type="password" name="password2" size="75" /></td></tr>
<c:if test="${badPassword}"><tr><td colspan=2">Passwords did not match</td></tr></c:if>
    <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
    <tr><td><strong>Database Name</strong></td><td><input type="text" name="DatabaseName" value="${ds.databaseName}" size="75" /></td></tr>
    <tr><td><strong>Create Database</strong></td><td><input type="text" name="CreateDatabase" value="${ds.createDatabase}" size="75" /></td></tr>
    <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
    <tr><td><strong>Partition Max Size</strong></td><td><input type="text" name="partitionMaxSize" value="${connectionManagerInfo.partitionMaxSize}" size="75" /></td></tr>
    <tr><td><strong>Partition Min Size</strong></td><td><input type="text" name="partitionMinSize" value="${connectionManagerInfo.partitionMinSize}" size="75" /></td></tr>
    <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
    <tr><td><strong>Login Timeout (Seconds)</strong></td><td><input type="text" name="LoginTimeout" value="${ds.loginTimeout}" size="75" /></td></tr>
    <tr><td><strong>Blocking Timeout (Milliseconds)</strong></td><td><input type="text" name="blockingTimeoutMilliseconds" value="${connectionManagerInfo.blockingTimeoutMilliseconds}" size="75" /></td></tr>
    <tr><td><strong>Idle Timeout (Minutes)</strong></td><td><input type="text" name="idleTimeoutMinutes" value="${connectionManagerInfo.idleTimeoutMinutes}" size="75" /></td></tr>
    <tr><td colspan="2"><input type="submit" name="btnSave" value="Save" onClick="doSave();"/><input type="submit" name="btnCancel" value="Cancel" onClick="doCancel();"></td></tr>
</table>
</form>