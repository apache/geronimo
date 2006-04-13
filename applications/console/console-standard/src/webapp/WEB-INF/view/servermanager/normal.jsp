<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<br />
<p><font face="Verdana" size="+1" COLOR=DARKRED><center><b>WARNING: A shutdown of the server will disable this Web Console!</b></center></font></p>
<p><center>Proceed only if you are certain you want to terminate the server.</center></p>
<br />
<%--   Removed until a better mechanism for rebooting the server is created
<table width="100%">
<form action="<portlet:actionURL/>">
<tr><td align="center"><input type="submit" value="Reboot" name="reboot"/></td></tr>
</form>
</table>
--%>
<br />
<table width="100%">
<form action="<portlet:renderURL/>">
<tr><td align="center"><input type="submit" value="Shutdown" name="shutdown"
onClick="return confirm('Are you sure you want to shutdown the server (last chance)?');"/></td></tr>
</form>
</table>
<br />
<br />
