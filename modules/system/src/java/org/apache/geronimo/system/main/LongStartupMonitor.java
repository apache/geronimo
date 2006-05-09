package org.apache.geronimo.system.main;

import java.io.PrintStream;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;

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
        long time = System.currentTimeMillis() - configStarted;        
        StringBuffer buf = new StringBuffer();
        buf.append(" started in ");
        
        String formattedTime = getFormattedTime(time);
        if (formattedTime.startsWith("0.")) {
            // don't display zero seconds
            formattedTime = " " +formattedTime.substring(1);
        }
        
        // if first number (e.g. seconds or minutes) is one digit,
        // pad it with a leading space to get times to line up nicely
        int index = formattedTime.indexOf(':'); // must look for colon first
        if (index == -1)
            index = formattedTime.indexOf('.');
                
        if (index == 1)
            buf.append(' ');
            
        buf.append(formattedTime);
        
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

    // time formatting method - thanks to Maven 
    private static String getFormattedTime( long time )
    {
        String pattern = "s.SSS's'";
        if ( time / 60000L > 0 )
        {
            pattern = "m:s" + pattern;
            if ( time / 3600000L > 0 )
            {
                pattern = "H:m" + pattern;
            }
        }
        DateFormat fmt = new SimpleDateFormat( pattern );
        fmt.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        return fmt.format( new Date( time ) );
    }
}
