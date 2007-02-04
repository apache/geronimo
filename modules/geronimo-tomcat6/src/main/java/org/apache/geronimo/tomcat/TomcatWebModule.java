package org.apache.geronimo.tomcat;

import javax.management.j2ee.statistics.Stats;

import org.apache.catalina.Context;
import org.apache.geronimo.management.geronimo.WebModule;

/**
 * @version $Revision$ $Date$
 * 
 */
public interface TomcatWebModule extends WebModule {

    public Context getContext();

    public Stats getStats();

    /**
     * @return The cumulative processing times of requests by all servlets in
     *         this Context
     */
    public long getProcessingTime();

    /**
     * @return The time this context was started.
     */
    public long getStartTime();

    /**
     * @return The time (in milliseconds) it took to start this context.
     */
    public long getStartupTime();

    public long getTldScanTime();

}
