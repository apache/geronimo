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
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg"%>
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
    var <portlet:namespace/>formName = "<portlet:namespace/>fileSelect";
    var <portlet:namespace/>requiredFields = new Array("local");
    
    if(document.getElementById("specify").checked){
        <portlet:namespace/>requiredFields = new Array("local", "artifact", "version", "fileType");
    }
    
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="repository.normal.emptyText"/>');
        return false;    
    }
    
    return <portlet:namespace/>validate();
}

function <portlet:namespace/>validate() {
    with(document.<portlet:namespace/>fileSelect){
        var illegalChars= /[\.]{2}|[()<>,;:\\/"'\|]/ ;
        if (group.value.match(illegalChars)) {
            group.focus(); 
            addErrorMessage("<portlet:namespace/>", '<fmt:message key="repository.normal.invalidChar"/>');
            return false;
        } else if (artifact.value.match(illegalChars)) {
            artifact.focus(); 
            addErrorMessage("<portlet:namespace/>", '<fmt:message key="repository.normal.invalidChar"/>');
            return false;
        } else if (version.value.match(illegalChars)) {
            version.focus(); 
            addErrorMessage("<portlet:namespace/>", '<fmt:message key="repository.normal.invalidChar"/>');
            return false;
        } else if (fileType.value.match(illegalChars)) {
            fileType.focus(); 
            addErrorMessage("<portlet:namespace/>", '<fmt:message key="repository.normal.invalidChar"/>');
            return false;
        }
    }

    return true;
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

<script>
function showHideByCheckbox(cb,target){
    if (cb.checked)
	    document.getElementById(target).style.display='block';
	else
	    document.getElementById(target).style.display='none';
}
</script>

<!-- js's common msg -->
<div id="<portlet:namespace/>CommonMsgContainer"></div>
<!-- portlet's common msg -->
<CommonMsg:commonMsg/>
<br/>
<form onsubmit="return <portlet:namespace/>validateForm();" enctype="multipart/form-data" name="<portlet:namespace/>fileSelect" method="POST" action="<portlet:actionURL/>">
  <input type="hidden" value="deploy" name="action"/>
  <table width="100%" class="TableLine">
    <tr class="DarkBackground">
        <th scope="col" colspan="3" width="40" align="center"><fmt:message key="repository.normal.addArchiveToRepository"/></th>   
    </tr>
    <tr class="LightBackground">
      <td align="right" width="20%"><label for="<portlet:namespace/>local"><fmt:message key="consolebase.common.file"/></label>:</td>
      <td><input type="file" name="local" id="<portlet:namespace/>local" onchange="<portlet:namespace/>parse(value)"></td>
      <td>If the file is a normal jar file, it will be automatically convert to an OSGi bundle</td>
    </tr>
    <tr class="MediumBackground">
      <td align="right" width="20%">Artifact string:</td>
      <td>
        <table width="100%">
            <tr>
              <td align="right" width="50"><label for="<portlet:namespace/>group"><fmt:message key="consolebase.common.group"/></label>:</td>
              <td align="left"><input type="text" name="group" id="<portlet:namespace/>group" value="${group}" /></td>
            </tr>
            <tr>
              <td align="right"></td>
              <td align="left">
                <input type="checkbox" name="specify" id="specify" value="yes" onChange="showHideByCheckbox(this,'specifyTb')"/> Specify other parts
              </td>
            </tr>
        </table>
        <table width="100%" id="specifyTb" style="display:none">
            <tr>
              <td align="right" width="50"><label for="<portlet:namespace/>artifact"><fmt:message key="repository.normal.artifact"/></label>:</td>
              <td align="left"><input type="text" name="artifact" id="<portlet:namespace/>artifact" value="${artifact}"/></td>
            </tr>
            <tr>
              <td align="right" width="50"><label for="<portlet:namespace/>version"><fmt:message key="consolebase.common.version"/></label>:</td>
              <td align="left"><input type="text" name="version" id="<portlet:namespace/>version" value="${version}"/></td>
            </tr>
            <tr>
              <td align="right" width="50"><label for="<portlet:namespace/>fileType"><fmt:message key="consolebase.common.type"/></label>:</td>
              <td align="left"><input type="text" name="fileType" id="<portlet:namespace/>fileType" value="${fileType}"/></td>
            </tr>
         </table>
      </td>
      <td>
        The default groupId is "default".<br/>
        The other parts could be calculated automatically, if:<br/>
        (1) It is an OSGi bundle, then the artifactId is its Bundle-SymbolicName and the version is its Bundle-Version;<br/>
        (2) It is a file with filename in the form: &lt;artifactId&gt;-&lt;version&gt;.&lt;type&gt;, for e.g. mylib-1.0.jar, and then it will be converted to an OSGi Bundle using the artifactId as the Bundle-SymbolicName, and using version as the Bundle-Version.<br/>
      </td>
    </tr>
    <tr class="LightBackground">
      <td align="right" width="20%"><label for="<portlet:namespace/>jarName"><fmt:message key="repository.normal.replacedName"/></label>:</td>
      <td><input type="text" name="jarName" id="<portlet:namespace/>jarName" value="${jarName}" style="width:230px" /></td>
      <td><fmt:message key="repository.normal.replacedNameDescription"/></td>
    </tr>
    <tr class="MediumBackground">
      <td width="20%"></td>
      <td><input type="submit" value='<fmt:message key="consolebase.common.install"/>' /></td>
      <td></td>
    </tr>
  </table>
</form>

<br/>

<b><fmt:message key="repository.normal.currentRepositoryEntries"/></b>
<p><fmt:message key="repository.normal.toViewUsage"/></p>
<ul>
<c:forEach items="${reslist}" var="res">
<li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="usage"/><portlet:param name="res" value="${res}"/></portlet:actionURL>"><c:out value="${res}"/></a></li>
</c:forEach>
</ul>
