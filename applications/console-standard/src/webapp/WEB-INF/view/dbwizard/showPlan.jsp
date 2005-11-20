<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p><b>Create Database Pool</b> -- Show Deployment Plan</p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>DatabaseForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="save" />
    <input type="hidden" name="user" value="${pool.user}" />
    <input type="hidden" name="name" value="${pool.name}" />
    <input type="hidden" name="dbtype" value="${pool.dbtype}" />
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
    <!-- STATUS FIELD: Deployment Plan -->
      <tr>
        <th valign="top"><div align="right">Deployment Plan:</div></th>
        <td><textarea rows="30" cols="60" readonly>${deploymentPlan}</textarea></td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
<input type="submit" value="Deploy Pool" />
<input type="button" value="Edit Settings" onclick="document.<portlet:namespace/>DatabaseForm.mode.value='edit';document.<portlet:namespace/>DatabaseForm.submit();return false;" />
        </td>
      </tr>
    <!-- STATUS FIELD: Command-line guidance -->
      <tr>
        <th valign="top"><div align="right">Deploy Command:</div></th>
        <td>To deploy a database pool from the command line using this plan,
          copy and paste it to a file (say, <tt>plan-file.xml</tt>) and save
          it.  Then run a command like:<br />
<pre>
cd GERONIMO_HOME
java -jar bin/deployer.jar deploy plan-file.xml \
        ${pool.rarPath}
</pre></td>
      </tr>
    <!-- STATUS FIELD: Embed in EAR guidance -->
      <tr>
        <th valign="top"><div align="right">Add to EAR:</div></th>
        <td>Instead of deploying as a top-level database pool, you
          can deploy this pool as part of an EAR.  To add a database
          pool to an EAR using this plan:
<ol>
  <li>Copy and paste the plan to a file</li>
  <li>Save the plan file to the top level of your EAR</li>
  <li>Copy the RAR file from <tt>GERONIMO_HOME/repository/${pool.rarPath}</tt>
    to the top level of your EAR</li>
  <li>Create a <tt>META-INF/geronimo-application.xml</tt> file in your EAR
    that has a <tt>module</tt> entry like this (substituting the correct
    RAR file name and plan file name):</li>
</ol>
<pre>
&lt;application
   xmlns="http://geronimo.apache.org/xml/ns/j2ee/application-1.0"
   configId="MyApplication"&gt;
  &lt;module&gt;
    &lt;connector&gt;rar-file-name&lt;/connector&gt;
    &lt;alt-dd&gt;plan-file-name.xml&lt;/alt-dd&gt;
  &lt;/module&gt;
&lt;/application&gt;
</pre></td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>">Cancel</a></p>
