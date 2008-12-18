<%--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--%>

<%-- $Rev$ $Date$ --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
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
function <portlet:namespace/>validateForm() {
   var illegalChars= /[\.]{2}|[()<>,;:\\/"']/ ;
   if (! (document.<portlet:namespace/>fileSelect.local.value 
      && document.<portlet:namespace/>fileSelect.group.value 
      && document.<portlet:namespace/>fileSelect.artifact.value 
      && document.<portlet:namespace/>fileSelect.version.value 
      && document.<portlet:namespace/>fileSelect.fileType.value))
   {
      alert("File, Group, Artifact, Version, and Type are all required fields");
      return false;
   } else if (document.<portlet:namespace/>fileSelect.group.value.match(illegalChars)) {
       alert("Group contains invalid characters - must only contain letters, numbers, and underscores");
       return false;
   } else if (document.<portlet:namespace/>fileSelect.artifact.value.match(illegalChars)) {
       alert("Artifact contains invalid characters - must only contain letters, numbers, and underscores");
       return false;
   } else if (document.<portlet:namespace/>fileSelect.version.value.match(illegalChars)) {
       alert("Version contains invalid characters - must only contain letters, numbers, and underscores");
       return false;
   } else if (document.<portlet:namespace/>fileSelect.fileType.value.match(illegalChars)) {
       alert("File type contains invalid characters - must only contain letters, numbers, and underscores");
       return false;
   }
}

function <portlet:namespace/>parse(localFile) {
    // Split the path
    var pathParts = localFile.split("\\"); // Assuming windows file delim
    if(localFile.indexOf("/") >= 0) // check if *nix 
        pathParts = localFile.split("/");
    basename = pathParts[pathParts.length - 1]; // Last part is the base file name

    // Attempt to get the artifact and type from the basename
    // This regular expression for repository filename is taken from Maven1Repository.MAVEN_1_PATTERN
    regExpStr = "(.+)-([0-9].+)\\.([^0-9]+)";
    var fileRegExp = new RegExp(regExpStr, "g");
    if(basename.match(fileRegExp) != null) {
        // base file name matches the regular expression
        fileRegExp.compile(regExpStr, "g");
        var fileParts = fileRegExp.exec(basename);
        fileType = fileParts[fileParts.length - 1];
        version = fileParts[fileParts.length - 2];
        artifact = fileParts[fileParts.length - 3];
        document.<portlet:namespace/>fileSelect.fileType.value = fileType;
        document.<portlet:namespace/>fileSelect.version.value = version;
        document.<portlet:namespace/>fileSelect.artifact.value = artifact;

        // Attempt to suggest the group
        if(pathParts.length >= 3 && pathParts[pathParts.length-2] == fileType +'s') {
            // Maven1Repository
            document.<portlet:namespace/>fileSelect.group.value = pathParts[pathParts.length-3];
        } else if(pathParts.length >= 4 && pathParts[pathParts.length-2] == version && pathParts[pathParts.length-3] == artifact) {
            // Maven2Repository
            document.<portlet:namespace/>fileSelect.group.value = pathParts[pathParts.length-4];
        } else {
            document.<portlet:namespace/>fileSelect.group.value = '';
        }
    } else {
        document.<portlet:namespace/>fileSelect.fileType.value = '';
        document.<portlet:namespace/>fileSelect.version.value = '';
        document.<portlet:namespace/>fileSelect.artifact.value = '';
        document.<portlet:namespace/>fileSelect.group.value = '';
    }
}

</script>

<table width="100%">
<tr>
  <td align="center">
  <form onsubmit="return <portlet:namespace/>validateForm();" enctype="multipart/form-data" name="<portlet:namespace/>fileSelect" method="POST" action="<portlet:actionURL><portlet:param name="action" value="deploy"/></portlet:actionURL>">
  <table>
    <tr>
      <th colspan="2"><fmt:message key="repository.normal.addArchiveToRepository"/></th>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>local"><fmt:message key="consolebase.common.file"/></label></td>
      <td><input name="local" id="<portlet:namespace/>local" onchange="<portlet:namespace/>parse(value);" type="file">&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>group"><fmt:message key="consolebase.common.group"/></label>:</td>
      <td><input type="text" name="group" id="<portlet:namespace/>group" value="${group}"/></td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>artifact"><fmt:message key="repository.normal.artifact"/></label>:</td>
      <td><input type="text" name="artifact" id="<portlet:namespace/>artifact" value="${artifact}"/></td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>version"><fmt:message key="consolebase.common.version"/></label>:</td>
      <td><input type="text" name="version" id="<portlet:namespace/>version" value="${version}"/></td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>fileType"><fmt:message key="consolebase.common.type"/></label>:</td>
      <td><input type="text" name="fileType" id="<portlet:namespace/>fileType" value="${fileType}"/></td>
    </tr>
    <tr><td colspan="2"><font size="-2">&nbsp;</font></td></tr>
    <tr>
      <td colspan="2" align="center"><input type="submit" value='<fmt:message key="consolebase.common.install"/>' /></td>
    </tr>
  </table>
  </form>
  </td>
</tr>
</table>

<b><fmt:message key="repository.normal.currentRepositoryEntries"/></b>
<p><fmt:message key="repository.normal.toViewUsage"/></p>
<ul>
<c:forEach items="${reslist}" var="res">
<li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="usage"/><portlet:param name="res" value="${res}"/></portlet:actionURL>"><c:out value="${res}"/></a></li>
</c:forEach>
</ul>
