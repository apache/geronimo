<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p><b>JMS Resource Group</b> -- Show Deployment Plan</p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>JMSForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="plan-after" />
    <input type="hidden" name="rar" value="${data.rarURI}" />
    <input type="hidden" name="dependency" value="${data.dependency}" />
    <input type="hidden" name="instanceName" value="${data.instanceName}" />
    <input type="hidden" name="workManager" value="${data.workManager}" /> <%-- todo: pick list for WorkManager --%>
    <c:forEach var="prop" items="${data.instanceProps}">
      <input type="hidden" name="${prop.key}" value="${prop.value}" />
    </c:forEach>
    <input type="hidden" name="currentFactoryID" value="${data.currentFactoryID}" />
    <input type="hidden" name="currentDestinationID" value="${data.currentDestinationID}" />
    <input type="hidden" name="factoryType" value="${data.factoryType}" />
    <input type="hidden" name="destinationType" value="${data.destinationType}" />
    <c:forEach var="factory" items="${data.connectionFactories}" varStatus="status">
      <input type="hidden" name="factory.${status.index}.factoryType" value="${factory.factoryType}" />
      <input type="hidden" name="factory.${status.index}.instanceName" value="${factory.instanceName}" />
      <input type="hidden" name="factory.${status.index}.transaction" value="${factory.transaction}" />
      <input type="hidden" name="factory.${status.index}.xaTransaction" value="${factory.xaTransactionCaching}" />
      <input type="hidden" name="factory.${status.index}.xaThread" value="${factory.xaThreadCaching}" />
      <input type="hidden" name="factory.${status.index}.poolMinSize" value="${factory.poolMinSize}" />
      <input type="hidden" name="factory.${status.index}.poolMaxSize" value="${factory.poolMaxSize}" />
      <input type="hidden" name="factory.${status.index}.poolIdleTimeout" value="${factory.poolIdleTimeout}" />
      <input type="hidden" name="factory.${status.index}.poolBlockingTimeout" value="${factory.poolBlockingTimeout}" />
      <c:forEach var="prop" items="${factory.instanceProps}">
        <input type="hidden" name="factory.${status.index}.${prop.key}" value="${prop.value}" />
      </c:forEach>
    </c:forEach>
    <c:forEach var="dest" items="${data.adminObjects}" varStatus="status">
      <input type="hidden" name="destination.${status.index}.destinationType" value="${dest.destinationType}" />
      <input type="hidden" name="destination.${status.index}.name" value="${dest.name}" />
      <c:forEach var="prop" items="${dest.instanceProps}">
        <input type="hidden" name="destination.${status.index}.${prop.key}" value="${prop.value}" />
      </c:forEach>
    </c:forEach>

    <table border="0">
    <!-- STATUS FIELD: Deployment Plan -->
      <tr>
        <th valign="top" style="min-width: 140px"><div align="right">Deployment Plan:</div></th>
        <td><textarea rows="30" cols="60" readonly>${deploymentPlan}</textarea></td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
            <input type="hidden" name="nextAction" value="deploy" />
            <input type="button" value="Edit Configuration" onclick="document.<portlet:namespace/>JMSForm.nextAction.value='review';document.<portlet:namespace/>JMSForm.submit();return false;" />
            <input type="submit" value="Deploy JMS Resource" />
        </td>
      </tr>
    <!-- STATUS FIELD: Command-line guidance -->
      <tr>
        <th valign="top"><div align="right">Deploy Command:</div></th>
        <td>To deploy a JMS resource from the command line using this plan,
          copy and paste it to a file (say, <tt>plan-file.xml</tt>) and save
          it.  Then run a command like:<br />
<pre>
cd GERONIMO_HOME
java -jar bin/deployer.jar deploy plan-file.xml \
        ${rarURL}
</pre></td>
      </tr>
    <!-- STATUS FIELD: Embed in EAR guidance -->
      <tr>
        <th valign="top"><div align="right">Add to EAR:</div></th>
        <td>Instead of deploying as a top-level JMS resource, you
          can deploy this pool as part of an EAR.  To add a JMS
          resource to an EAR using this plan:
<ol>
  <li>Copy and paste the plan to a file</li>
  <li>Save the plan file to the top level of your EAR</li>
  <li>Copy the RAR file from <tt>${rarURL}</tt>
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
