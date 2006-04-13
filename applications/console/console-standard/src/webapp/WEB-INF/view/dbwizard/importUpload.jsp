<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>


<p><b>Import Database Pools</b> -- Step 1: Upload Configuration File</p>

<p>This page starts the process of importing database pools from another application server.
To do the import, you'll need to upload a configuration file from the other server using the
fields below.  After that, we'll convert the values we can, and ask you to confirm the
configuration for each pool we find in the configuration.</p>

<form enctype="multipart/form-data" method="POST" name="<portlet:namespace/>ImportForm"
      action="<portlet:actionURL><portlet:param name="mode" value="importUpload"/><portlet:param name="importSource" value="${pool.importSource}"/></portlet:actionURL>">
    <table width="100%">
      <tr>
        <td class="DarkBackground" colspan="2">${pool.importSource} Import</td>
      </tr>
      <tr>
        <th align="right" style="min-width: 140px">Config File:</th>
        <td><input type="file" name="configFile" /></td>
      </tr>
      <tr>
        <td></td>
        <td>Please select the ${from}.</td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td><input type="submit" value="Next" /></td>
      </tr>
    </table>
</form>

<c:if test="${pool.importSource eq 'WebLogic 8.1'}">
<br />
<br />
<br />
<form name="<portlet:namespace/>WebLogicImportForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="weblogicImport" />
    <input type="hidden" name="importSource" value="${pool.importSource}" />
    <input type="hidden" name="from" value="${from}" />
    <table width="100%">
      <tr>
        <td class="DarkBackground" colspan="2">Alternate ${pool.importSource} Import</td>
      </tr>
      <tr>
        <td colspan="2">If WebLogic 8.1 is installed on the same machine as Geronimo, and the
WebLogic domain directory is readable by the user running Geronimo, you
can also point directly to the WebLogic installation and domain directories.
This has the advantage that the import process can read the database
passwords, whereas if you just import a <tt>config.xml</tt> above you'll
need to re-enter all the passwords.</td>
      </tr>
      <tr>
        <th align="right" style="min-width: 140px">Domain directory path:</th>
        <td><input type="text" name="weblogicDomainDir" size="40" /></td>
      </tr>
      <tr>
        <td></td>
        <td>Please enter the full path to the WebLogic domain directory (containing the
          <tt>config.xml</tt> file) for your WebLogic domain (e.g. C:\bea\user_projects\domains\mydomain).</td>
      </tr>
      <tr>
        <th align="right"><tt>weblogic81/server/lib</tt> path:</th>
        <td><input type="text" name="weblogicLibDir" size="40" /></td>
      </tr>
      <tr>
        <td></td>
        <td>Please enter the full path to the <tt>weblogic81/server/lib</tt> directory
          for your WebLogic 8.1 installation (e.g. C:\bea\weblogic81\server\lib).</td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td><input type="submit" value="Next" /></td>
      </tr>
    </table>
</form>
</c:if>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>">Cancel</a></p>
