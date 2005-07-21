<p>This displays the current port number for HTTPS access. By clicking on the &quot;edit&quot; link the user can change the default port number and select whether to use the default trust store and whether to require client authentication.</p>
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top">port</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">Enter the new port number in this box.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top">Keystore Type</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">The keystore type is displayed here.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top">Use default trust store</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">The default trust store location is shown on the Certificates link under &quot;keystore location.&quot; At this time the user cannot specify an alternative trust store from within the Geronimo Console; it must be done from the command line and only source code customers have access to this feature.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top">Needs client authentication</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">Pull-down menu to require client authentication (Yes) or (No).</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top"><img src="/console/images/update.gif" /></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">Click on this button to make the changes. The Geronimo Server must be rebooted to make the change take effect. Use the Server Management portlet to reboot. After the reboot the new port number must be used to access the Geronimo Console, for example: https://localhost:8446...</td>
  </tr>
</table>
<p>&nbsp;</p>
