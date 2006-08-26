package org.apache.geronimo.system.main;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;

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
    void foundModules(Artifact[] modules);
    void moduleLoading(Artifact module);
    void moduleLoaded(Artifact module);
    void moduleStarting(Artifact module);
    void moduleStarted(Artifact module);
    void startupFinished();

    // Indicate failures during load
    void serverStartFailed(Exception problem);
}
