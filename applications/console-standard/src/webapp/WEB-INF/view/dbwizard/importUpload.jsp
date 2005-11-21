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
        <th align="right">Config File:</th>
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


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>">Cancel</a></p>
