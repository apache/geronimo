package org.apache.geronimo.console.webmanager;

import javax.portlet.RenderRequest;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.jetty.JettyContainer;

/**
 * @version $Revision: 1.0$
 */
public class JettyStatisticsHelper implements StatisticsHelper {
    public void gatherStatistics(WebContainer container, RenderRequest renderRequest) {
        JettyContainer jetty = ((JettyContainer)container);
        boolean statsOn = jetty.getCollectStatistics();
        renderRequest.setAttribute("statsOn", statsOn ? Boolean.TRUE : Boolean.FALSE);
        if (statsOn) {
            renderRequest.setAttribute("connections", new Integer(jetty.getConnections()));
            renderRequest.setAttribute("connectionsOpen", new Integer(jetty.getConnectionsOpen()));
            renderRequest.setAttribute("connectionsOpenMax", new Integer(jetty.getConnectionsOpenMax()));
            renderRequest.setAttribute("connectionsDurationAve", new Long(jetty.getConnectionsDurationAve()));
            renderRequest.setAttribute("connectionsDurationMax", new Long(jetty.getConnectionsDurationMax()));
            renderRequest.setAttribute("connectionsRequestsAve", new Integer(jetty.getConnectionsRequestsAve()));
            renderRequest.setAttribute("connectionsRequestsMax", new Integer(jetty.getConnectionsRequestsMax()));
            renderRequest.setAttribute("errors", new Integer(jetty.getErrors()));
            renderRequest.setAttribute("requests", new Integer(jetty.getRequests()));
            renderRequest.setAttribute("requestsActive", new Integer(jetty.getRequestsActive()));
            renderRequest.setAttribute("requestsActiveMax", new Integer(jetty.getRequestsActiveMax()));
            renderRequest.setAttribute("requestsDurationAve", new Long(jetty.getRequestsDurationAve()));
            renderRequest.setAttribute("requestsDurationMax", new Long(jetty.getRequestsDurationMax()));
        }
    }

}
