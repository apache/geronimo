This portlet displays and filters the Jetty log file. The Jetty log file contains HTTP accesses and messages from JSP and servlet applications that are running on Geronimo. By default this portlet displays the contents of the log file for the current date. The user can apply filtering criteria to view only data of interest. The user may enter information into any/all/none of the fields, which are defined next.</p>
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="150"  align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 10px 5px; font-size: 10px; color: #546BC7; font-weight: bold; text-decoration: underline;">Refresh</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">Retrieves the latest lines from the log file while retaining the user's filtering criteria. </td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;">From/To</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">Display log output from the dates specified.</td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;"> Ignore Dates</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">If this checkbox is checked, filtering will not be done using the date. All log lines that match the other filtering criteria will be displayed regardless of the date.</td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;"> Remote Address</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">A specific remote host can be entered in this field using it's IP address, e.g., 192.168.1.1. </td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;">Authenticated User</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">A username, such as &quot;system.&quot; </td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;"> Request Method</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">This drop-down box allows the user to filter on &quot;ANY&quot;, &quot;POST&quot; and &quot;GET&quot; methods.</td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;"> Requested URI</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">Filtering can be done on the Requested URI field by entering a URI, such as, http://localhost:8080/cviewer/jsp/viewclass.jsp</td>
  </tr>
</table>
