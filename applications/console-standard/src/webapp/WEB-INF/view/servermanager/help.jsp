<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<p>This portlet allows a warm reboot of the server or a shutdown of it.</p>
<p>A warm reboot will shutdown all applications and services and reboot the Geronimo kernel within the same process. The Geronimo Console session will be disconnected. Reconnect after the server is back up.</p>
<p>A shutdown will shutdown the server and cause the JVM to exit. To continue using the Geronimo Console after a shutdown, Geronimo must be restarted.</p>