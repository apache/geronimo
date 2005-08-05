<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<c:set var="rooturl" value="${requestScope['org.apache.geronimo.console.repo.root']}"/>
<c:set var="reslist" value="${requestScope['org.apache.geronimo.console.repo.list']}"/>

<style type="text/css">  
	div.Hidden {
	display: none;
	}
	
	div.Shown {
	display: block;
	font-size: 10px;
	}
</style>  

<script language="JavaScript">

state1 = false;

function toggle1() {
	state1 = !state1;
	document.getElementById("first").className = state1 ? "Shown" : "Hidden";
} 
</script>

<table width="100%">
<th>root URL: <c:out value="${rooturl}"/></th>
<tr>
 <center><td>
 <form enctype="multipart/form-data" method="POST" action="<portlet:actionURL><portlet:param name="action" value="deploy"/></portlet:actionURL>">
 <table> 
  <th align="center"><center>Add File to Repository</center></th>
  <tr>

    <tr>
   <td>File</td><td><input type="file" name="local">&nbsp;&nbsp;&nbsp;   All uploaded files are placed in the 'uploaded' directory.</td>
  </tr>  
 </table> 
 
  </td>
  <tr>
    <center><td><input type="submit" value="Install" /></td></center>
  </tr>
  <tr><td>&nbsp;</td></tr>
  <tr><td>&nbsp;</td></tr>
  </center>
</tr>
 </form>
<c:forEach items="${reslist}" var="res">
<tr><td><c:out value="${res}"/></td></tr>
</c:forEach>
<tr><td>&nbsp;</td></tr>
<tr><td>&nbsp;</td></tr>
</table>
