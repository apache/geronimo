<%@ page contentType="text/javascript" %>
var last = false;
function addNext() {
  if(last) {
    addPoint(200);
    last=false;
  } else {
    addPoint(50);
    last=true;
  }
  setTimeout("addNext()", 500);
}

DWREngine.setErrorHandler(null);
var stopped=false;
function callServer() {
    metadata = {};
    metadata.callback=updateValues;
    metadata.errorHandler=onError;
    Jsr77Stats.getJavaVMStatistics(metadata);
}
function updateValues(serverStats) {
    addPoint(serverStats.bytesCurrent, serverStats.memoryCurrent);
    if(!stopped) {
        setTimeout("callServer()", 1000);
    }
}
function onError() {
    stopped=true;
//    DWRUtil.setValue("<portlet:namespace/>ErrorArea", '<form name="<portlet:namespace/>Refresh" action="<portlet:actionURL/>"><input type="submit" value="Refresh"/></form>');
}
