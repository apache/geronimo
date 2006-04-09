<%@ page contentType="text/javascript" %>
DWREngine.setErrorHandler(null);
var stopped=false;
function callServer() {
    metadata = {};
    metadata.callback=updateValues;
    metadata.errorHandler=onError;
    Jsr77Stats.getJavaVMStatistics(metadata);
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
//    DWRUtil.setValue("<portlet:namespace/>ErrorArea", '<form name="<portlet:namespace/>Refresh" action="<portlet:actionURL/>"><input type="submit" value="Refresh"/></form>');
}
