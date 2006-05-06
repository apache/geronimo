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
     * Length of longest Configuration Name
     */
    private int longestConfigNameLength;
    
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
        
        for (int i = 0, len= 0; i < configurations.length; i++) {
            len = configurations[i].toString().length();
            if (len > longestConfigNameLength)
                longestConfigNameLength = len;
        }
    }

    public synchronized void configurationLoading(Artifact configuration) {
        StringBuffer buf = new StringBuffer("Configuration ");
        // pad config index
        int configIndexDigits = Integer.toString(++configNum).length();
        for (; configIndexDigits < numConfigsDigits; configIndexDigits++) {
            buf.append(' ');
        }
        // append configuration index / total configs
        buf.append(configNum).append('/').append(numConfigs).append(' ');
        // append configuration name
        buf.append(configuration);
        // pad end of config name with spaces so trailing startup times will line up
        int len = configuration.toString().length();
        for (; len < longestConfigNameLength; len++) {
            buf.append(' ');
        }
        out.print(buf);
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
        // pad configuration startup time
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

    public synchronized void serverStartFailed(Exception problem) {
        out.println("Server Startup failed");
        out.println();
        problem.printStackTrace(out);
    }

}
