package org.apache.geronimo.system.main;

import java.io.PrintStream;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * A startup monitor that shows the progress of loading and starting
 * configurations, outputing a new line for each configuration started
 * showing the time taken to start the configuration along with the
 * configId.
 * <p/>
 * This startup monitor produces more lines of output than the
 * ProgressBarStartupMonitor but its output is suitable for redirection
 * to a file or for when Geronimo is running under an IDE or other tool.
 * <p/>
 * A summary will also be produced containing a list of ports
 * Geronimo is listening on, the configIds of application modules
 * that were started and the URLs of Web applications that were started.
 *
 * @version $Revision: 1.0$
 */
public class LongStartupMonitor implements StartupMonitor {
    /**
     * Minimum width of the time (padded with leading spaces) in configuration start messages
     */
    private final static int MIN_TIME_WIDTH = 3;

    /**
     * PrintStream
     */
    private PrintStream out;

    /**
     * Number of configurations to start
     */
    private int numConfigs;

    /**
     * Number of digits in number of configurations to start
     */
    private int numConfigsDigits;

    /**
     * Number of configuration currently being started
     */
    private int configNum;

    /**
     * Time Geronimo was started
     */
    private long started;

    /**
     * Time the current configuration being processed was started
     */
    private long configStarted;

    /**
     * The Kernel of the system being started
     */
    private Kernel kernel;

    public synchronized void systemStarting(long startTime) {
        out = System.out;
        started = startTime;
    }

    public synchronized void systemStarted(Kernel kernel) {
        this.kernel = kernel;
    }

    public synchronized void foundConfigurations(Artifact[] configurations) {
        numConfigs = configurations.length;
        numConfigsDigits = Integer.toString(numConfigs).length();
    }

    public synchronized void configurationLoading(Artifact configuration) {
        StringBuffer buf = new StringBuffer("Configuration ").append(configuration);
        out.print(buf.toString());
        configNum++;
    }

    public synchronized void configurationLoaded(Artifact configuration) {
    }

    public synchronized void configurationStarting(Artifact configuration) {
        configStarted = System.currentTimeMillis();
    }

    public synchronized void configurationStarted(Artifact configuration) {
        int time = Math.round((float) (System.currentTimeMillis() - configStarted) / 1000f);
        StringBuffer buf = new StringBuffer();
        buf.append(" started in ");
        // pad config index
        int configIndexDigits = Integer.toString(configNum).length();
        for (; configIndexDigits < numConfigsDigits; configIndexDigits++) {
            buf.append(' ');
        }
        // pad configuration startup time
        buf.append(configNum).append('/').append(numConfigs).append(' ');
        int timeDigits = Integer.toString(time).length();
        for (; timeDigits < MIN_TIME_WIDTH; timeDigits++) {
            buf.append(' ');
        }
        buf.append(time).append("s ");
        out.println(buf.toString());
    }

    public synchronized void startupFinished() {
        int time = Math.round((float) (System.currentTimeMillis() - started) / 1000f);

        out.println("Startup completed in " + time + " seconds");
        StartupMonitorUtil.wrapUp(out, kernel);
    }

    // TODO Review - Currently loadFailed is not called by Daemon
    public synchronized void loadFailed(String configuration, Exception problem) {
        out.println("Failed to load configuration " + configuration);
        out.println();
        problem.printStackTrace(out);
    }

    public synchronized void serverStartFailed(Exception problem) {
        out.println("Server Startup failed");
        out.println();
        problem.printStackTrace(out);
    }

    // TODO Review - Currently startFailed is not called by Daemon
    public synchronized void startFailed(String configuration, Exception problem) {
        out.println("Failed to start configuration " + configuration);
        // We print the stack track now (rather than defering the printing of it)
        // since other problems that may occur during the start of a configuration
        // (e.g. an individual GBean not being able to start) produce
        // errors in the log (and therefore standard output) immediately.
        problem.printStackTrace(out);
    }
}
