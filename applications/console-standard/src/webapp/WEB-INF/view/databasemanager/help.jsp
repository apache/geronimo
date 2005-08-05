This portlet displays the name and the state of installed databases, allows the user to test the database connection, and allows the user to add new datasources.<br>
<br>
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;" width="150" align="right" valign="top">detail</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px"><p>Each data source has a detail link. Clicking on it will provide detailed information about that database. From the &quot;detail&quot; page the user can<br>
click on the &quot;change&quot; link to update the fields. For information on the detail page and the database fields, see below.<br>
      </p>    </td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px" width="150" align="right" valign="top"><strong>Name</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">The name of the database.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px" width="150" align="right" valign="top"><strong> Global JNDI Name</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px"><p>The global JNDI name is assigned by the site administrator. It is used when trying to connect to the resource (database or queue/topic) from an application client. It should not be used from other server-side components. It should be  unique for every resource deployed in Geronimo. Currently users of Geronimo are not provided a mechanism for entering a value in this field.</p>    </td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px" width="150" align="right" valign="top"><strong>State</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">State of the database, either &quot;running&quot; or &quot;stopped.&quot;</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px" width="150" align="right" valign="top"><strong>Test Result</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px"><p>Clicking on this link will test the connection  to the database and return a result. If the 
database is connected, the result will say &quot;Connected.&quot; Otherwise, there will be a &quot;Failed&quot; message with details about the failure.</p>    </td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;" width="150" align="right" valign="top">Add New Datasource</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px"><p>Clicking on this link allows the user to add a new datasource. On the add new datasource form, fill in the fields and click on the Create button to add the new datasource. The fields are defined on the page itself.</p>    </td>
  </tr>
</table>
<p><br>The detail page shows the properties associated with the database. The user (or DB Administrator) can change these properties by clicking on the &quot;change&quot; link. </p>
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;" width="150" align="right" valign="top">change</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">Allows the user to edit the fields.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 10px 5px; font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;" width="150" align="right" valign="top">back</td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">Displays the previous screen.</td>
  </tr>
</table>
<p>Clicking on the change link will bring up another page. This page allows the user to change the properties associated with the database. The fields are defined as follows:</p>
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px; color: #1E1E52;" width="150" align="right" valign="top"><strong>UserName</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">Username that is used to connect to the database.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px; color: #1E1E52;" width="150" align="right" valign="top"><strong>Password</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">Password for the database user.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px; color: #1E1E52;" width="150" align="right" valign="top"><strong>Driver</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">The JDBC class name for the database driver. </td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px; color: #1E1E52;" width="150" align="right" valign="top"><strong>ConnectionURL</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">The JDBC URL for connecting to the database.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px; color: #1E1E52;" width="150" align="right" valign="top"><strong>ExceptionSorterClass</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">The class that is responsible for determining whether any given SQL Exception is fatal, and if so, the connection will be closed and released from the pool. Current options for this field are &quot;NoExceptionsAreFatalSorter&quot; or &quot;AllExceptionsAreFatalSorter.&quot;</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px; color: #1E1E52;" width="150" align="right" valign="top"><strong>Partition Count</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">By default set to 1 because there is typically only one partition. The rare circumstances in which this value might change are:<br>
<br>a.) if application managed security is used, i.e., the user/password is supplied in getConnection(user,pw) call.<br>
<br>b.) if container managed security is used (mapping the JAAS subject to a db user/pw).</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px; color: #1E1E52;" width="150" align="right" valign="top"><strong>Connection Count</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">Total number of connections summed over all partitions.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px; color: #1E1E52;" width="150" align="right" valign="top"><strong>IdleConnectionCount</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">Maximum number of idle connections allowed.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px; color: #1E1E52;" width="150" align="right" valign="top"><strong>PartitionMaxSize</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">The maximum number of simultaneous connections allowed to this connection pool.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px; color: #1E1E52;" width="150" align="right" valign="top"><strong>PartitionMinSize</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">The minimum size of the connection pool.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px; color: #1E1E52;" width="150" align="right" valign="top"><strong>BlockingTimeout (ms)</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">If a caller attempts to connect to the database while all the connection pools are in use, the caller will wait this long for an available connection.</td>
  </tr>
  <tr>
    <td class="MediumBackground" style="padding: 10px 10px 5px 10px; color: #1E1E52;" width="150" align="right" valign="top"><strong> Idle Timeout (minutes)</strong></td>
    <td class="LightBackground" style="padding: 10px 5px 10px 10px">How often the pool is checked for unused connections. If any connection has been unused for this interval, that connection will be closed and removed from the pool.</td>
  </tr>
</table>
