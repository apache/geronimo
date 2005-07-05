package org.apache.geronimo.system.main;

import java.net.URI;
import org.apache.geronimo.kernel.Kernel;

/**
 * An interface used by the Daemon to convey the status of the server
 * startup.
 *
 * @version $Revision: 1.0$
 */
public interface StartupMonitor {
    // Normal calls, will generally occur in this order
    void systemStarting(long startTime);
    void systemStarted(Kernel kernel);
    void foundConfigurations(URI[] configurations);
    void configurationLoading(URI configuration);
    void configurationLoaded(URI configuration);
    void configurationStarting(URI configuration);
    void configurationStarted(URI configuration);
    void startupFinished();

    // Indicate failures during load
    void serverStartFailed(Exception problem);
    void loadFailed(String configuration, Exception problem);
    void startFailed(String configuration, Exception problem);
}
