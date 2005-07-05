package org.apache.geronimo.system.main;

import java.net.URI;
import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Revision: 1.0$
 */
public class SilentStartupMonitor implements StartupMonitor {
    public void systemStarting(long startTime) {
    }

    public void systemStarted(Kernel kernel) {
    }

    public void foundConfigurations(URI[] configurations) {
    }

    public void configurationLoading(URI configuration) {
    }

    public void configurationLoaded(URI configuration) {
    }

    public void configurationStarting(URI configuration) {
    }

    public void configurationStarted(URI configuration) {
    }

    public void startupFinished() {
    }

    public void serverStartFailed(Exception problem) {
    }

    public void loadFailed(String configuration, Exception problem) {
    }

    public void startFailed(String configuration, Exception problem) {
    }
}
