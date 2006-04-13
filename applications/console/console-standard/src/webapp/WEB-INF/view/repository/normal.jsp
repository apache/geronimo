<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

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
function <portlet:namespace/>validate() {
   if (! (document.<portlet:namespace/>fileSelect.local.value 
      && document.<portlet:namespace/>fileSelect.group.value 
      && document.<portlet:namespace/>fileSelect.artifact.value 
      && document.<portlet:namespace/>fileSelect.version.value 
      && document.<portlet:namespace/>fileSelect.fileType.value))
   {
      alert("File, Group, Artifact, Version, and Type are all required fields");
      return false;
   }
}

function <portlet:namespace/>parse(localFile) {
   // Check for windows file delim
   fileChar = "\\";
   fileNameIndex = localFile.lastIndexOf(fileChar);
   if (fileNameIndex == -1) {
      // if not found check for *nix delim
      fileChar = "/";
      fileNameIndex = localFile.lastIndexOf(fileChar);
   }

   if (fileNameIndex != -1) {
      basename = localFile.substring(fileNameIndex + 1);

      prefix = localFile.substring(0 , fileNameIndex );

      groupIndex = prefix.lastIndexOf(fileChar);
      if (groupIndex != -1) {
          group = prefix.substring(groupIndex + 1 );
          document.<portlet:namespace/>fileSelect.group.value = group;        
      }
   }
   else {
      basename = localFile;
   }

   // Attempt to get the artifact and type from the basename
   typeIndex = basename.lastIndexOf(".");
   if (typeIndex != -1) {
       fileType = basename.substring(typeIndex + 1);
       document.<portlet:namespace/>fileSelect.fileType.value = fileType;

       artifact = basename.substring(0 , typeIndex ); 

       versionIndex = artifact.lastIndexOf("-");
       if (versionIndex != -1) {
           version = artifact.substring(versionIndex + 1); 
           document.<portlet:namespace/>fileSelect.version.value = version;

           artifact = artifact.substring(0 , versionIndex );
           document.<portlet:namespace/>fileSelect.artifact.value = artifact;
       }
       else {
          version = artifact = "";
       }
   }
   else {
      fileType = "";
   }
}

</script>

<table width="100%">
<tr>
  <td align="center">
  <form onsubmit="return <portlet:namespace/>validate();" enctype="multipart/form-data" name="<portlet:namespace/>fileSelect" method="POST" action="<portlet:actionURL><portlet:param name="action" value="deploy"/></portlet:actionURL>">
  <table>
    <tr>
      <th colspan="2">Add Archive to Repository</th>
    </tr>
    <tr>
      <td>File</td>
      <td><input name="local" onchange="<portlet:namespace/>parse(value);" type="file">&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr>
      <td>Group:</td>
      <td><input type="text" name="group" value="${group}"/></td>
    </tr>
    <tr>
      <td>Artifact:</td>
      <td><input type="text" name="artifact" value="${artifact}"/></td>
    </tr>
    <tr>
      <td>Version:</td>
      <td><input type="text" name="version" value="${version}"/></td>
    </tr>
    <tr>
      <td>Type:</td>
      <td><input type="text" name="fileType" value="${fileType}"/></td>
    </tr>
    <tr><td colspan="2"><font size="-2">&nbsp;</font></td></tr>
    <tr>
      <td colspan="2" align="center"><input type="submit" value="Install" /></td>
    </tr>
  </table>
  </form>
  </td>
</tr>
</table>

<b>Current Repository Entries</b>
<ul>
<c:forEach items="${reslist}" var="res">
<li><c:out value="${res}"/></li>
</c:forEach>
</ul>