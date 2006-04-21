<% String dwrForwarderServlet = org.apache.geronimo.console.util.PortletManager.getConsoleFrameworkServletPath(request) + "/../dwr"; %>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/interface/ProgressMonitor.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/engine.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/util.js'></script>

<script>
function refreshProgress()
{
    ProgressMonitor.getProgressInfo(updateProgress);
}

function updateProgress(progressInfo)
{
    // get the bean values from DWR
    var progressPercent = progressInfo.progressPercent;
    var mainMessage = progressInfo.mainMessage;
    var subMessage = progressInfo.subMessage;

    // set the bean values in the HTML document
    if (mainMessage != null) {
	    document.getElementById('progressMeterMainMessage').innerHTML = mainMessage;
    }
    if (subMessage != null) {
	    document.getElementById('progressMeterSubMessage').innerHTML = subMessage;
    }
    if (progressPercent > -1) {
       document.getElementById('progressMeterShell').style.display = 'block';
       document.getElementById('progressMeterBar').style.width = parseInt(progressPercent * 3.5) + 'px';
    }
    window.setTimeout('refreshProgress()', 1000);
    return true;
}

function startProgress()
{
    document.getElementById('progressMeter').style.display = 'block';
    document.getElementById('progressMeterMainMessage').innerHTML = 'Processing...';
    window.setTimeout("refreshProgress()", 1000);
    return true;
}
</script>

<div id="progressMeter" style="display: none; padding-top: 5px;">
    <br/>
    <div>
        <div id="progressMeterMainMessage"></div>
        <div id="progressMeterSubMessage"></div>
        <div id="progressMeterShell" style="display: none; width: 350px; height: 20px; border: 1px inset; background: #eee;">
            <div id="progressMeterBar" style="width: 0; height: 20px; border-right: 1px solid #444; background: #9ACB34;"></div>
        </div>
    </div>
</div>
