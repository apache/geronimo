<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<br />
<table width="100%">
<form action="<portlet:actionURL/>">
<tr><td align="center"><input type="submit" value="Reboot" name="reboot"/></td></tr>
</form>
</table>
<br />
<table width="100%">
<form action="<portlet:renderURL/>">
<tr><td align="center"><input type="submit" value="Shutdown" name="shutdown"/></td></tr>
</form>
</table>
