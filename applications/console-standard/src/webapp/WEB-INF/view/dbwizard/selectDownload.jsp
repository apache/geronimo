<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ page import="org.apache.geronimo.console.util.PortletManager" %>

<% String dwrForwarderServlet = PortletManager.getConsoleFrameworkServletPath(request) + "/../dwr"; %>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/interface/DownloadMonitor.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/engine.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/util.js'></script>

<script>
function refreshProgress()
{
    DownloadMonitor.getDownloadInfo(updateProgress);
}

function updateProgress(downloadInfo)
{
    if (downloadInfo.downloadStarted) {
        var kbDownloaded = Math.floor(downloadInfo.bytesDownloaded / 1024);
        document.getElementById('progressMeterText').innerHTML = 'Download in progress: ' 
              + kbDownloaded
              + ' Kb downloaded' ;
        if (downloadInfo.totalBytes > 0) {
            document.getElementById('progressMeterShell').style.display = 'block';
            var progressPercent = Math.ceil((downloadInfo.bytesDownloaded / downloadInfo.totalBytes) * 100);
            document.getElementById('progressMeterBar').style.width = parseInt(progressPercent * 3.5) + 'px';
        } else {
            // if total bytes are unknown then hide the progress meter since calculating % complete is not possible
            document.getElementById('progressMeterShell').style.display = 'none';
        }
    }
    window.setTimeout('refreshProgress()', 1000);
    return true;
}

function startProgress()
{
    document.getElementById('progressMeter').style.display = 'block';
    document.getElementById('progressMeterText').innerHTML = 'Download in progress: 0 Kb downloaded';
    window.setTimeout("refreshProgress()", 500);
    document.getElementById('nextButton').disabled=true;
    document.getElementById('cancelButton').disabled=true;
    return true;
}
</script>

<portlet:defineObjects/>

<p><b>Create Database Pool</b> -- Step 2: Select Driver, JAR, Parameters</p>

<p>This page lets you automatically download a driver for a database where the
driver JARs are available online without login or registration.</p>

<p><i>If this page took a very long time to load and there are no drivers listed in the box below,
it probably means your Geronimo installation can't connect to apache.org to retrieve the driver
download configuration file.  Sorry for the inconvenience, you'll have to try again later or
install the driver by hand (copy it to a directory under geronimo/repository/)</i></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>DatabaseForm" action="<portlet:actionURL/>" method="POST"
onsubmit="startProgress()">
    <input type="hidden" name="mode" value="process-download" />
    <input type="hidden" name="name" value="${pool.name}" />
    <input type="hidden" name="dbtype" value="${pool.dbtype}" />
    <input type="hidden" name="user" value="${pool.user}" />
    <input type="hidden" name="password" value="${pool.password}" />
    <input type="hidden" name="driverClass" value="${pool.driverClass}" />
    <input type="hidden" name="url" value="${pool.url}" />
    <input type="hidden" name="urlPrototype" value="${pool.urlPrototype}" />
    <input type="hidden" name="jar1" value="${pool.jar1}" />
    <input type="hidden" name="jar2" value="${pool.jar2}" />
    <input type="hidden" name="jar3" value="${pool.jar3}" />
    <input type="hidden" name="minSize" value="${pool.minSize}" />
    <input type="hidden" name="maxSize" value="${pool.maxSize}" />
    <input type="hidden" name="idleTimeout" value="${pool.idleTimeout}" />
    <input type="hidden" name="blockingTimeout" value="${pool.blockingTimeout}" />
    <input type="hidden" name="adapterDisplayName" value="${pool.adapterDisplayName}" />
    <input type="hidden" name="adapterDescription" value="${pool.adapterDescription}" />
    <input type="hidden" name="rarPath" value="${pool.rarPath}" />
  <c:forEach var="prop" items="${pool.properties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
  <c:forEach var="prop" items="${pool.urlProperties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
    <table border="0">
    <!-- ENTRY FIELD: DRIVER TYPE -->
      <tr>
        <th style="min-width: 140px"><div align="right">Select Driver:</div></th>
        <td>
          <select name="driverName">
        <c:forEach var="driver" items="${drivers}">
            <option>${driver.name}</option>
        </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>A driver that Geronimo can download automatically for you.</td>
      </tr>
      <tr>
        <td></td>
        <td>
          <input type="submit" value="Next" id="nextButton"/>
          <input id="cancelButton" type="button" value="Cancel" onclick="document.<portlet:namespace/>DatabaseForm.mode.value='params';document.<portlet:namespace/>DatabaseForm.submit();return false;" />
        </td>
      </tr>
    </table>
    <div id="progressMeter" style="display: none; padding-top: 5px;">
        <br/>
        <div>
            <div id="progressMeterText"></div>
            <div id="progressMeterShell" style="display: none; width: 350px; height: 20px; border: 1px inset; background: #eee;">
                <div id="progressMeterBar" style="width: 0; height: 20px; border-right: 1px solid #444; background: #9ACB34;"></div>
            </div>
        </div>
    </div>
</form>


<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<%--
<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="params" />
            </portlet:actionURL>">Select predefined database</a></p>
<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="edit" />
            </portlet:actionURL>">Select "other" database</a></p>
--%>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>">Return to List</a></p>

<p><br /><br /><br />Here are some other JDBC drivers you might want to download on your own (just save them somewhere under geronimo/repository/):</p>
<ul>
  <li><a href="http://www.daffodildb.com/download/index.jsp">DaffodilDB</a></li>
  <li><a href="http://www.frontbase.com/cgi-bin/WebObjects/FrontBase">FrontBase</a></li>
  <li><a href="http://www.datadirect.com/products/jdbc/index.ssp">DataDirect SQL Server, DB2, Oracle, Informix, Sybase</a></li>
  <li><a href="http://www-306.ibm.com/software/data/informix/tools/jdbc/">Informix</a></li>
  <li><a href="http://www.intersystems.com/cache/downloads/index.html">InterSystems Cache</a></li>
  <li><a href="http://www.borland.com/products/downloads/download_jdatastore.html">JDataStore</a></li>
  <li><a href="http://developer.mimer.com/downloads/index.htm">Mimer</a></li>
  <li><a href="http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/index.html">Oracle</a></li>
  <li><a href="http://www.pervasive.com/developerzone/access_methods/jdbc.asp">Pervasive</a></li>
  <li><a href="http://www.pointbase.com/products/downloads/">Pointbase</a></li>
  <li><a href="http://www.progress.com/esd/index.ssp">Progress</a></li>
  <li><a href="http://www.microsoft.com/technet/prodtechnol/sql/2005/downloads/jdbc.mspx">Microsoft SQL Server</a></li>
</ul>
