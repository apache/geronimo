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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="org.apache.geronimo.system.plugin.DownloadResults"%> 
<%@page import="com.sun.mail.iap.Response"%>
<fmt:setBundle basename="pluginportlets"/>
<fmt:message key="car.downloadStatus.processing"/>
<portlet:defineObjects/>
<script type='text/javascript' src='/plugin/dwr/interface/ProgressMonitor.js'></script>
<script type='text/javascript' src='/plugin/dwr/engine.js'></script>
<script type='text/javascript' src='/plugin/dwr/util.js'></script>

<div id="<portlet:namespace/>progressMeter" style="display: none; padding-top: 5px;">
    <div>
        <div id="<portlet:namespace/>progressMeterCurrentFile"></div>
        <br/>
        <br/>
        <br/>
        <br/>
        <div id="<portlet:namespace/>progressMeterMainMessage"></div>
        <div id="<portlet:namespace/>progressMeterSubMessage"></div>
        <div id="<portlet:namespace/>progressMeterShell" style="display: none; width: 350px; height: 20px; border: 1px inset; background: #eee;">
            <div id="<portlet:namespace/>progressMeterBar" style="width: 0; height: 20px; border-right: 1px solid #444; background: #9ACB34;"></div>
        </div>
    </div>
</div>

<div id="<portlet:namespace/>ErrorArea"></div>

<script type="text/javascript">
dwr.engine.setErrorHandler(<portlet:namespace/>onError);

function <portlet:namespace/>onError() {
    dwr.util.setValue("<portlet:namespace/>ErrorArea", 'I had a problem!');
}

function setMainMessage(mainMessage) {
    if (mainMessage != null) {
	    document.getElementById('<portlet:namespace/>progressMeterMainMessage').innerHTML = mainMessage;
    }
}

function setProgressCurrentFile(mainFile) {
    if (mainFile != null) {
        document.getElementById('<portlet:namespace/>progressMeterCurrentFile').innerHTML = mainFile;
    }
}

function setSubMessage(subMessage) {
    if (subMessage != null) {
	    document.getElementById('<portlet:namespace/>progressMeterSubMessage').innerHTML = subMessage;
    }
}

function setProgressPercent(progressPercent) {
    if (progressPercent > -1) {
       document.getElementById('<portlet:namespace/>progressMeterShell').style.display = 'block';
       document.getElementById('<portlet:namespace/>progressMeterBar').style.width = parseInt(progressPercent * 3.5) + 'px';
    } else {
       document.getElementById('<portlet:namespace/>progressMeterShell').style.display = 'block';
       document.getElementById('<portlet:namespace/>progressMeterBar').style.width = '0px';
    }
}

//For Aesthetics
function setProgressFull() {
    document.getElementById('<portlet:namespace/>progressMeterShell').style.display = 'block';
    document.getElementById('<portlet:namespace/>progressMeterBar').style.width = parseInt(350) + 'px';
}

function setFinished() {
    <%
        DownloadResults results = (DownloadResults) request.getAttribute("console.plugins.DownloadResults");
        System.out.println("Results are" + results);
    %>
    document.forms['<portlet:namespace/>ContinueForm'].submit();
}

function <portlet:namespace/>startProgress()
{
    document.getElementById('<portlet:namespace/>progressMeterMainMessage').innerHTML = '<fmt:message key="ajax.progressbar.processing"/>';
    document.getElementById('<portlet:namespace/>progressMeter').style.display = 'block';
    metadata = {};
    metadata.errorHandler=<portlet:namespace/>onError;
    ProgressMonitor.getProgressInfo(${downloadKey},metadata);
}
</script>


