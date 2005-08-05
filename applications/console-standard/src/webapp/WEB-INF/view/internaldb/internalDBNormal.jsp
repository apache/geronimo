<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<%--
Choose DB: &nbsp; 
<c:choose>
  <c:when test="${(param.rdbms == '1') || (empty param.rdbms)}">
    Derby
  </c:when>
  <c:otherwise>
    <a href="<portlet:actionURL portletMode="view">
               <portlet:param name="rdbms" value="1" />
             </portlet:actionURL>">Derby
    </a>
  </c:otherwise>
</c:choose>

&nbsp;|&nbsp;

<c:choose>
  <c:when test="${param.rdbms == '2'}">
    MS SQL
  </c:when>
  <c:otherwise>
    <a href="<portlet:actionURL portletMode="view">
               <portlet:param name="rdbms" value="2" />
             </portlet:actionURL>">MS SQL
    </a>
  </c:otherwise>
</c:choose>
--%>

<table width="100%">
  <tr> 
    <td class="DarkBackground" width="100%" colspan="2" align="center">DB</td> 
  </tr> 
  <tr> 
  <tr> 
    <td class="LightBackground" width="20%" nowrap>DB Product Name</td> 
    <td class="LightBackground" width="80%">${internalDB['DB Product Name']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground">DB Product Version</td> 
    <td class="MediumBackground">${internalDB['DB Product Version']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground">DB Major Version</td> 
    <td class="LightBackground">${internalDB['DB Major Version']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground">DB Minor Version</td> 
    <td class="MediumBackground">${internalDB['DB Minor Version']}</td> 
  </tr> 
</table>
<br>
<table width="100%">
  <tr> 
    <td class="DarkBackground" width="100%" colspan="2" align="center">Driver</td> 
  </tr> 
  <tr> 
  <tr> 
    <td class="LightBackground" width="20%" nowrap>Driver Name</td> 
    <td class="LightBackground" width="80%">${internalDB['Driver Name']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground">Driver Version</td> 
    <td class="MediumBackground">${internalDB['Driver Version']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground">Driver Major Version</td> 
    <td class="LightBackground">${internalDB['Driver Major Version']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground">Driver Minor Version</td> 
    <td class="MediumBackground">${internalDB['Driver Minor Version']}</td> 
  </tr> 
</table>
<br>  
<table width="100%">
  <tr> 
    <td class="DarkBackground" width="100%" colspan="2" align="center">JDBC</td> 
  </tr> 
  <tr> 
  <tr> 
    <td class="LightBackground" width="20%" nowrap>JDBC Major Version</td> 
    <td class="LightBackground" width="80%">${internalDB['JDBC Major Version']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground">JDBC Minor Version</td> 
    <td class="MediumBackground">${internalDB['JDBC Minor Version']}</td> 
  </tr> 
</table>
<br>
<table width="100%">
  <tr> 
    <td class="DarkBackground" width="100%" colspan="2" align="center">Etc</td> 
  </tr> 
  <tr> 
    <td class="LightBackground" width="20%" nowrap>URL</td> 
    <td class="LightBackground" width="80%">${internalDB['URL']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground">Username</td> 
    <td class="MediumBackground">${internalDB['Username']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground">Read Only</td> 
    <td class="LightBackground">${internalDB['Read Only']}</td> 
  </tr> 
</table>
<br>
<table width="100%">
  <tr> 
    <td class="DarkBackground" width="100%" colspan="2" align="center">Functions</td> 
  </tr> 
  <tr> 
  <tr> 
    <td class="LightBackground" width="20%" nowrap>Numeric Functions</td> 
    <td class="LightBackground" width="80%">${internalDB['Numeric Functions']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground">String Functions</td> 
    <td class="MediumBackground">${internalDB['String Functions']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground">System Functions</td> 
    <td class="LightBackground">${internalDB['System Functions']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground">Time Date Functions</td> 
    <td class="MediumBackground">${internalDB['Time Date Functions']}</td> 
  </tr> 
</table>
<br>  
<table width="100%">
  <tr> 
    <td class="DarkBackground" width="100%" colspan="2" align="center">SQL</td> 
  </tr> 
  <tr> 
  <tr> 
    <td class="LightBackground" width="20%" nowrap>Supported SQL Keywords</td> 
    <td class="LightBackground" width="80%">${internalDB['Supported SQL Keywords']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground">Supported Types</td> 
    <td class="MediumBackground">${internalDB['Supported Types']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground">Table Types</td> 
    <td class="LightBackground">${internalDB['Table Types']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground">Schemas</td> 
    <td class="MediumBackground">${internalDB['Schemas']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground">SQL State Type</td> 
    <td class="LightBackground">${internalDB['SQL State Type']}</td> 
  </tr> 
  <tr> 
    <td class="MediumBackground">Default Transaction Isolation</td> 
    <td class="MediumBackground">${internalDB['Default Transaction Isolation']}</td> 
  </tr> 
  <tr> 
    <td class="LightBackground">Result Set Holdability</td> 
    <td class="LightBackground">${internalDB['Result Set Holdability']}</td> 
  </tr> 
</table>