<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<table width="100%"> 
  <tr> 
    <td class="DarkBackground" width="100%" colspan="2" align="center">Kernel</td> 
  </tr> 
  <tr> 
    <td class="LightBackground" width="20%" nowrap>Kernel Boot Time</td> 
    <td class="LightBackground" width="80%">${svrProps['Kernel Boot Time']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground">Kernel Up Time</td> 
    <td class="MediumBackground">${svrProps['Kernel Up Time']}</td> 
  </tr> 
</table>
<br>
<!--
<table width="100%">
  <tr>
    <td class="DarkBackground" width="100%" colspan="2" align="center">Server</td>
  </tr>

  base directory is commented originally
  <tr>
    <td class="LightBackground" width="20%" nowrap>Base Directory</td>
    <td class="LightBackground" width="80%">${svrProps['Base Directory']}</td>
  </tr>

  <tr>
    <td class="LightBackground">Platform Architecture</td>
    <td class="LightBackground">${svrProps['Platform Architecture']}</td>
  </tr>
  <tr>
    <td class="MediumBackground" width="20%" nowrap>Version</td>
    <td class="MediumBackground"  width="80%">${svrProps['Version']}</td>
  </tr>
  <tr>
    <td class="LightBackground">Apache Geronimo Build Version</td>
    <td class="LightBackground">${svrProps['Apache Geronimo Build Version']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">J2EE Specs Version</td>
    <td class="MediumBackground">${svrProps['J2EE Specifications Version']}</td>
  </tr>
  <tr>
    <td class="LightBackground">JSR-168 Portal Version</td>
    <td class="LightBackground">${svrProps['JSR 168 Portal Version']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">Build Date</td>
    <td class="MediumBackground">${svrProps['Build Date']}</td>
  </tr>
  <tr>
    <td class="LightBackground">Build Time</td>
    <td class="LightBackground">${svrProps['Build Time']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">Copyright</td>
    <td class="MediumBackground">${svrProps['Copyright']}</td>
  </tr>
</table>
-->
<br>
<table width="100%"> 
  <tr> 
    <td class="DarkBackground" width="100%" colspan="2" align="center">JVM</td> 
  </tr> 
  <tr> 
    <td class="LightBackground" width="20%" nowrap>Java Version</td> 
    <td class="LightBackground" width="80%">${jvmProps['Java Version']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground">Java Vendor</td> 
    <td class="MediumBackground">${jvmProps['Java Vendor']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground">Node</td> 
    <td class="LightBackground">${jvmProps['Node']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground">Max Memory</td> 
    <td class="MediumBackground">${jvmProps['Max Memory']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground">Total Memory</td> 
    <td class="LightBackground">${jvmProps['Total Memory']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground">Free Memory</td> 
    <td class="MediumBackground">${jvmProps['Free Memory']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground">Available Processors</td> 
    <td class="LightBackground">${jvmProps['Available Processors']}</td> 
  </tr> 
  <tr> 
    <td colspan="2" align="center">
      <form name="<portlet:namespace/>" action="<portlet:actionURL/>"">
        <input type="submit" value="Refresh"/>
      </form>
    </td>
  </tr>  
</table>