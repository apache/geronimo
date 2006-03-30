package org.apache.geronimo.system.main;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class SilentStartupMonitor implements StartupMonitor {
    private final static Log log = LogFactory.getLog(SilentStartupMonitor.class.getName());

    private Kernel kernel;

    public void systemStarting(long startTime) {
    }

    public void systemStarted(Kernel kernel) {
        this.kernel = kernel;
    }

    public void foundConfigurations(Artifact[] configurations) {
    }

    public void configurationLoading(Artifact configuration) {
    }

    public void configurationLoaded(Artifact configuration) {
    }

    public void configurationStarting(Artifact configuration) {
    }

    public void configurationStarted(Artifact configuration) {
    }

    public void startupFinished() {
        try {
            Set gbeans = kernel.listGBeans((AbstractNameQuery)null);
            for (Iterator it = gbeans.iterator(); it.hasNext();) {
                AbstractName name = (AbstractName) it.next();
                int state = kernel.getGBeanState(name);
                if (state != State.RUNNING_INDEX) {
                    log.warn("Unable to start "+name+" ("+State.fromInt(state).getName()+")");
                }
            }
        } catch (GBeanNotFoundException e) {
        }
        System.out.println("Geronimo startup complete");
    }

    public void serverStartFailed(Exception problem) {
        System.out.println("Geronimo startup failed:");
        problem.printStackTrace(System.out);
    }

    public void loadFailed(String configuration, Exception problem) {
        problem.printStackTrace();
    }

    public void startFailed(String configuration, Exception problem) {
        problem.printStackTrace();
    }
}
