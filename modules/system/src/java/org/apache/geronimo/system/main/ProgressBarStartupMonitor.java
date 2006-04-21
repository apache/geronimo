package org.apache.geronimo.system.main;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * A startup monitor that shows the progress of loading and starting
 * configurations using a text based progress bar and the use of line
 * feeds to update the progress display, therefore minimizing the
 * number of lines output to the terminal.
 * <p/>
 * A summary will also be produced containing a list of ports
 * Geronimo is listening on, the configIds of application modules
 * that were started and the URLs of Web applications that were started.
 *
 * @version $Revision: 1.0$
 */
public class ProgressBarStartupMonitor implements StartupMonitor {
    private final static char STATUS_NOT_READY = ' ';
    private final static char STATUS_LOADING = '-';
    private final static char STATUS_LOADED = '>';
    private final static char STATUS_STARTED = '*';
    private final static char STATUS_FAILED = 'x';
    private final static int MAX_WIDTH = 70;
    private PrintStream out;
    private String currentOperation;
    private Artifact[] configurations;
    private char[] configStatus = new char[0];
    private long started;
    private int percent = 0;
    private Kernel kernel;
    private int operationLimit = 50;
    private boolean finished = false;
    private List exceptions = new ArrayList();
    private UpdateThread thread;

    public synchronized void systemStarting(long startTime) {
        out = System.out;
        started = startTime;
    }

    public synchronized void systemStarted(Kernel kernel) {
        out.println("Starting Geronimo Application Server");
        this.kernel = kernel;
        currentOperation = "Loading";
    }

    public synchronized void foundConfigurations(Artifact[] configurations) {
        this.configurations = configurations;
        configStatus = new char[configurations.length];
        for (int i = 0; i < configStatus.length; i++) {
            configStatus[i] = STATUS_NOT_READY;
        }
        operationLimit = MAX_WIDTH
                - 5 // two brackets, start and stop tokens, space afterward
                - configurations.length // configuration tokens
                - 4 // 2 digits of percent plus % plus space afterward
                - 5;// 3 digits of time plus s plus space afterward
        repaint();
        thread = new UpdateThread();
        thread.start();
    }

    public synchronized void calculatePercent() {
        if (finished) {
            this.percent = 100;
            return;
        }
        int percent = 0;
        if (kernel != null) percent += 5;
        int total = configStatus.length * 2;
        int progress = 0;
        for (int i = 0; i < configStatus.length; i++) {
            char c = configStatus[i];
            switch (c) {
                case STATUS_LOADED:
                    progress += 1;
                    break;
                case STATUS_STARTED:
                case STATUS_FAILED:
                    progress += 2;
                    break;
            }
        }
        percent += Math.round(90f * (float) progress / (float) total);
        this.percent = percent;
    }

    public synchronized void configurationLoading(Artifact configuration) {
        currentOperation = " Loading " + configuration;
        for (int i = 0; i < configurations.length; i++) {
            if (configurations[i].equals(configuration)) {
                configStatus[i] = STATUS_LOADING;
            }
        }
        repaint();
    }

    public synchronized void configurationLoaded(Artifact configuration) {
        for (int i = 0; i < configurations.length; i++) {
            if (configurations[i].equals(configuration)) {
                configStatus[i] = STATUS_LOADED;
            }
        }
        calculatePercent();
        repaint();
    }

    public synchronized void configurationStarting(Artifact configuration) {
        currentOperation = "Starting " + configuration;
    }

    public synchronized void configurationStarted(Artifact configuration) {
        for (int i = 0; i < configurations.length; i++) {
            if (configurations[i].equals(configuration)) {
                configStatus[i] = STATUS_STARTED;
            }
        }
        calculatePercent();
        repaint();
    }

    public synchronized void startupFinished() {
        finished = true;
        currentOperation = "Startup complete";
        calculatePercent();
        thread.done = true;
        thread.interrupt();
    }

    public synchronized void loadFailed(String configuration, Exception problem) {
        for (int i = 0; i < configurations.length; i++) {
            if (configurations[i].equals(configuration)) {
                configStatus[i] = STATUS_FAILED;
            }
        }
        if (problem != null) exceptions.add(problem);
    }

    public synchronized void serverStartFailed(Exception problem) {
        currentOperation = "Startup failed";
        repaint();
        out.println();
        problem.printStackTrace(out);
    }

    public synchronized void startFailed(String configuration, Exception problem) {
        for (int i = 0; i < configurations.length; i++) {
            if (configurations[i].equals(configuration)) {
                configStatus[i] = STATUS_FAILED;
            }
        }
        if (problem != null) exceptions.add(problem);
    }

    private synchronized void repaint() {
        StringBuffer buf = new StringBuffer();
        buf.append("\r[");
        buf.append(kernel == null ? STATUS_NOT_READY : STATUS_STARTED);
        for (int i = 0; i < configStatus.length; i++) {
            buf.append(configStatus[i]);
        }
        buf.append(finished ? STATUS_STARTED : STATUS_NOT_READY);
        buf.append("] ");
        if (percent < 10) {
            buf.append(' ');
        }
        buf.append(percent).append("% ");
        int time = Math.round((float) (System.currentTimeMillis() - started) / 1000f);
        if (time < 10) {
            buf.append(' ');
        }
        if (time < 100) {
            buf.append(' ');
        }
        buf.append(time).append("s ");
        if (currentOperation.length() > operationLimit) {
            int space = currentOperation.indexOf(' ', 5);
            buf.append(currentOperation.substring(0, space + 1));
            // "Foo BarBarBar" limit 9 = "Foo ...ar" = 13 - 9 + 3 + 1 + 3
            // buf.append("...").append(currentOperation.substring(currentOperation.length()-operationLimit+space+4));
            // "FooBar BarBarBar" limit 12 = "FooBar Ba..." = (7, 12-3)
            buf.append(currentOperation.substring(space + 1, operationLimit - 3)).append("...");
        } else {
            buf.append(currentOperation);
            for (int i = currentOperation.length(); i < operationLimit; i++) {
                buf.append(' ');
            }
        }
        out.print(buf.toString());
        out.flush();
    }

    private class UpdateThread extends Thread {
        private volatile boolean done = false;

        public UpdateThread() {
            super("Progress Display Update Thread");
            setDaemon(true);
        }

        public void run() {
            while (!done) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    continue;
                }
                repaint();
            }

            repaint();
            out.println();
            StartupMonitorUtil.wrapUp(ProgressBarStartupMonitor.this.out, ProgressBarStartupMonitor.this.kernel);
        }
    }
}
