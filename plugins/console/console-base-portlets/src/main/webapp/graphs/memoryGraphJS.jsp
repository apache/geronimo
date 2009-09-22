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
<%@ page contentType="text/javascript" %>
dwr.engine.setErrorHandler(null);
var stopped=false;
function callServer() {
    metadata = {};
    metadata.callback=updateValues;
    metadata.errorHandler=onError;
    top.Jsr77Stats.getJavaVMStatistics(metadata);
}
function updateValues(serverStats) {
    setMaxValue(serverStats.bytesMost, serverStats.memoryMost);
    addPoint(serverStats.bytesCurrent);
    setLowerCaption("Using "+serverStats.memoryCurrent+" of "+serverStats.memoryAllocated+" allocated");
    if(!stopped) {
        setTimeout("callServer()", 1000);
    }
}
function onError() {
    stopped=true;
//    dwr.util.setValue("<portlet:namespace/>ErrorArea", '<form name="<portlet:namespace/>Refresh" action="<portlet:actionURL/>"><input type="submit" value="Refresh"/></form>');
}
