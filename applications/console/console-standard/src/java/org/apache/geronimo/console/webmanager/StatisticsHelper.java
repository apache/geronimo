package org.apache.geronimo.console.webmanager;

import javax.portlet.RenderRequest;
import org.apache.geronimo.management.geronimo.WebContainer;

/**
 * @version $Revision: 1.0$
 */
public interface StatisticsHelper {
    public void gatherStatistics(WebContainer container, RenderRequest renderRequest);
}
