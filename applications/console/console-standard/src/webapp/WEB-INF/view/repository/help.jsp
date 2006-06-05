<p>This page displays the artifacts installed in the server's repository. The layout of the repository is the same as that used by Apache Maven making it possible to easily copy files over. The Geronimo Console provides a method for adding artifacts:</p><br>

<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><img src="/console/images/browse.gif" /></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">Browse to select the artifact to be added.</td>
  </tr>
  <tr>
    <td width="150" align="right" valign="top" class="MediumBackground" style="padding: 10px 10px 5px 10px"><img src="/console/images/install.gif" /></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">Click on this button to install the artifact.</td>
  </tr>
</table>

<p>To use an artifact in an application, add a dependency element into it's deployment plan. For example, to use Castor XML add the following XML to the plan:
<pre>
    &lt;dependency&gt;
        &lt;uri&gt;castor/jars/castor-0.9.5.3.jar&lt;/uri&gt;
    &lt;/dependency&gt;
</pre>
</p>