package org.apache.geronimo.system.main;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.net.URI;
import java.net.InetSocketAddress;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.NoSuchAttributeException;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A startup monitor that shows progress using line feeds
 *
 * @version $Revision: 1.0$
 */
public class ProgressBarStartupMonitor implements StartupMonitor {
    private final static Log log = LogFactory.getLog(ProgressBarStartupMonitor.class.getName());
    private final static char STATUS_NOT_READY=' ';
    private final static char STATUS_LOADING='-';
    private final static char STATUS_LOADED='>';
    private final static char STATUS_STARTED='*';
    private final static char STATUS_FAILED='x';
    private final static int MAX_WIDTH=70;
    private PrintStream out;
    private String currentOperation;
    private URI[] configurations;
    private char[] configStatus;
    private long started;
    private int percent = 0;
    private Kernel kernel;
    private int operationLimit = 50;
    private boolean finished = false;
    private List exceptions = new ArrayList();
    private UpdateThread thread;

    public void systemStarting(long startTime) {
        out = System.out;
        started = startTime;
    }

    public void systemStarted(Kernel kernel) {
        out.println("Starting Geronimo Application Server");
        this.kernel = kernel;
        currentOperation = "Loading";
    }

    public synchronized void foundConfigurations(URI[] configurations) {
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

    public void calculatePercent() {
        if(finished) {
            this.percent = 100;
            return;
        }
        int percent = 0;
        if(kernel != null) percent += 5;
        int total = configStatus.length*2;
        int progress = 0;
        for (int i = 0; i < configStatus.length; i++) {
            char c = configStatus[i];
            switch(c) {
                case STATUS_LOADED:
                    progress +=1;
                    break;
                case STATUS_STARTED:
                case STATUS_FAILED:
                    progress +=2;
                    break;
            }
        }
        percent += Math.round(90f*(float)progress/(float)total);
        this.percent = percent;
    }

    public synchronized void configurationLoading(URI configuration) {
        currentOperation = " Loading "+configuration;
        for (int i = 0; i < configurations.length; i++) {
            if(configurations[i].equals(configuration)) {
                configStatus[i] = STATUS_LOADING;
            }
        }
        repaint();
    }

    public synchronized void configurationLoaded(URI configuration) {
        for (int i = 0; i < configurations.length; i++) {
            if(configurations[i].equals(configuration)) {
                configStatus[i] = STATUS_LOADED;
            }
        }
        calculatePercent();
        repaint();
    }

    public synchronized void configurationStarting(URI configuration) {
        currentOperation = "Starting "+configuration;
    }

    public synchronized void configurationStarted(URI configuration) {
        for (int i = 0; i < configurations.length; i++) {
            if(configurations[i].equals(configuration)) {
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

    private void wrapUp() {
        repaint();
        out.println();

        List apps = new ArrayList();  // type = String (message)
        List ports = new ArrayList(); // type = AddressHolder
        Map failed = new HashMap();   // key = ObjectName, value = String (message)
        String serverInfo = null;
        try {
            Set gbeans = kernel.listGBeans(ObjectName.getInstance("*:*"));
            Map beanInfos = new HashMap(); // key = GBeanInfo, value = List (of attribute names)
            for (Iterator it = gbeans.iterator(); it.hasNext();) {
                ObjectName name = (ObjectName) it.next();
                if(isApplicationModule(name)) {
                    apps.add("    "+decodeModule(name.getKeyProperty("j2eeType"))+": "+name.getKeyProperty("name"));
                }

                int stateValue = kernel.getGBeanState(name);
                if (stateValue != State.RUNNING_INDEX) {
                    GBeanData data = kernel.getGBeanData(name);
                    State state = State.fromInt(stateValue);
                    StringBuffer buf = new StringBuffer();
                    buf.append("(").append(state.getName());
                    // Since it's not unusual for a failure to be caused by a port binding failure
                    //    we'll see if there's a likely looking port attribute in the config data
                    //    for the GBean.  It's a long shot, but hey.
                    if(data != null && data.getAttributes() != null) {
                        Map map = data.getAttributes();
                        for (Iterator it2 = map.keySet().iterator(); it2.hasNext();) {
                            String att = (String) it2.next();
                            if(att.equals("port") || att.indexOf("Port") > -1) {
                                buf.append(",").append(att).append("=").append(map.get(att));
                            }
                        }
                    }
                    buf.append(")");
                    failed.put(name, buf.toString());
                    continue;
                }

                // Check if this is ServerInfo
                GBeanInfo info = kernel.getGBeanInfo(name);
                if(info.getClassName().equals("org.apache.geronimo.system.serverinfo.ServerInfo")) {
                    serverInfo = (String) kernel.getAttribute(name, "version");
                }

                // Look for any SocketAddress properties
                List list = (List) beanInfos.get(info);
                if(list == null) {
                    list = new ArrayList(3);
                    beanInfos.put(info, list);
                    Set atts = info.getAttributes();
                    for (Iterator it2 = atts.iterator(); it2.hasNext();) {
                        GAttributeInfo att = (GAttributeInfo) it2.next();
                        if(att.getType().equals("java.net.InetSocketAddress")) {
                            list.add(att);
                        }
                    }
                }
                for (int i = 0; i < list.size(); i++) {
                    GAttributeInfo att = (GAttributeInfo) list.get(i);
                    try {
                        InetSocketAddress addr = (InetSocketAddress) kernel.getAttribute(name, att.getName());
                        if(addr == null) {
                            log.debug("No value for GBean "+name+" attribute "+att.getName());
                            continue;
                        } else if(addr.getAddress() == null || addr.getAddress().getHostAddress() == null) {
                            log.debug("Null address or host for GBean "+name+" "+att.getName()+": "+addr.getAddress());
                        }
                        String attName = info.getName();
                        if(list.size() > 1) {
                            attName += " "+decamelize(att.getName());
                        } else if(info.getAttribute("name") != null) {
                            attName += " "+kernel.getAttribute(name, "name");
                        }
                        ports.add(new AddressHolder(attName, addr));
                    } catch (IllegalStateException e) {
                        // We weren't able to load a port for this service -- that's a bummer
                    }
                }
            }
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (GBeanNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAttributeException e) {
            e.printStackTrace();
        } catch (Exception e) { // required by Kernel.getAttribute
            e.printStackTrace();
        }

        // Helpful output: list of applications started
        if(apps.size() > 0) {
            out.println("  Started Application Modules:");
            for (int i = 0; i < apps.size(); i++) {
                out.println((String)apps.get(i));
            }
        }
        // Helpful output: list of ports we listen on
        if(ports.size() > 0) {
            Collections.sort(ports);
            System.out.println("  Listening on Ports:");
            int max = 0;
            for (int i = 0; i < ports.size(); i++) {
                AddressHolder holder = (AddressHolder) ports.get(i);
                if(holder.getAddress().getAddress() != null && holder.getAddress().getAddress().getHostAddress() != null) {
                    max = Math.max(max, holder.getAddress().getAddress().getHostAddress().length());
                }
            }
            for (int i = 0; i < ports.size(); i++) {
                AddressHolder holder = (AddressHolder) ports.get(i);
                StringBuffer buf = new StringBuffer();
                buf.append("   ");
                if(holder.getAddress().getPort() < 10) {
                    buf.append(' ');
                }
                if(holder.getAddress().getPort() < 100) {
                    buf.append(' ');
                }
                if(holder.getAddress().getPort() < 1000) {
                    buf.append(' ');
                }
                if(holder.getAddress().getPort() < 10000) {
                    buf.append(' ');
                }
                buf.append(holder.getAddress().getPort()).append(' ');
                String address = holder.getAddress().getAddress() == null || holder.getAddress().getAddress().getHostAddress() == null ? "" :
                        holder.getAddress().getAddress().getHostAddress();
                buf.append(address);
                for(int j=address.length(); j<=max; j++) {
                    buf.append(' ');
                }
                buf.append(holder.getName());
                out.println(buf.toString());
            }
        }
        // Helpful output: list of GBeans that did not start
        if(failed.size() > 0) {
            out.println("  WARNING: Some GBeans were not started successfully:");
            for (Iterator it = failed.keySet().iterator(); it.hasNext();) {
                ObjectName name = (ObjectName) it.next();
                String state = (String) failed.get(name);
                if(name.getKeyProperty("name") != null) {
                    log.debug("Unable to start "+name+" "+state);
                    out.println("    "+name.getKeyProperty("name")+" "+state);
                } else {
                    out.println("    "+name+" "+state);
                }
            }
        }

        StringBuffer msg = new StringBuffer();
        msg.append("Geronimo Application Server started");
        if(serverInfo != null) {
            msg.append(" (version ").append(serverInfo).append(")");
        }
        out.println(msg.toString());
        out.flush();
    }

    public synchronized void loadFailed(String configuration, Exception problem) {
        for (int i = 0; i < configurations.length; i++) {
            if(configurations[i].equals(configuration)) {
                configStatus[i] = STATUS_FAILED;
            }
        }
        if(problem != null) exceptions.add(problem);
    }

    public synchronized void serverStartFailed(Exception problem) {
        currentOperation = "Startup failed";
        repaint();
        out.println();
        problem.printStackTrace(out);
    }

    public synchronized void startFailed(String configuration, Exception problem) {
        for (int i = 0; i < configurations.length; i++) {
            if(configurations[i].equals(configuration)) {
                configStatus[i] = STATUS_FAILED;
            }
        }
        if(problem != null) exceptions.add(problem);
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
        if(percent < 10) {
            buf.append(' ');
        }
        buf.append(percent).append("% ");
        int time = Math.round((float)(System.currentTimeMillis() - started)/1000f);
        if(time < 10) {
            buf.append(' ');
        }
        if(time < 100) {
            buf.append(' ');
        }
        buf.append(time).append("s ");
        if(currentOperation.length() > operationLimit) { // "Foo BarBarBar" limit 9 = "Foo ...ar" = 13 - 9 + 3 + 1 + 3
            int space = currentOperation.indexOf(' ');
            buf.append(currentOperation.substring(0, space+1));
            buf.append("...").append(currentOperation.substring(currentOperation.length()-operationLimit+space+4));
        } else {
            buf.append(currentOperation);
            for(int i=currentOperation.length(); i<operationLimit; i++) {
                buf.append(' ');
            }
        }
        out.print(buf.toString());
        out.flush();
    }

    private static boolean isApplicationModule(ObjectName on) {
        String type = on.getKeyProperty("j2eeType");
        String app = on.getKeyProperty("J2EEApplication");
        String name = on.getKeyProperty("name");
        if(type != null && (app == null || app.equals("null"))) {
            return (type.equals("WebModule") || type.equals("J2EEApplication") || type.equals("EJBModule") || type.equals("AppClientModule") || type.equals("ResourceAdapterModule")) && !name.startsWith("org/apache/geronimo/System");
        }
        return false;
    }

    private static String decodeModule(String value) {
        if(value.equals("WebModule")) {
            return "WAR";
        } else if(value.equals("J2EEApplication")) {
            return "EAR";
        } else if(value.equals("EJBModule")) {
            return "JAR";
        } else if(value.equals("AppClientModule")) {
            return "CAR";
        } else if(value.equals("ResourceAdapterModule")) {
            return "RAR";
        } else {
            return "UNK";
        }
    }

    private static String decamelize(String s) {
        if(s == null || s.equals("")) {
            return s;
        }
        StringBuffer buf = new StringBuffer();
        buf.append(Character.toUpperCase(s.charAt(0)));
        for(int i=1; i<s.length(); i++) {
            if(Character.isUpperCase(s.charAt(i))) {
                if(s.length() > i+1 && Character.isLowerCase(s.charAt(i+1))) {
                    buf.append(" ");
                }
            }
            buf.append(s.charAt(i));
        }
        return buf.toString();
    }

    private static class AddressHolder implements Comparable {
        private String name;
        private InetSocketAddress address;

        public AddressHolder(String name, InetSocketAddress address) {
            this.name = name;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public InetSocketAddress getAddress() {
            return address;
        }

        public void setAddress(InetSocketAddress address) {
            this.address = address;
        }

        public int compareTo(Object o) {
            AddressHolder other = (AddressHolder) o;
            int value = address.getPort() - other.address.getPort();
            return value == 0 ? address.getAddress().toString().compareTo(other.address.getAddress().toString()) : value;
        }
    }

    private class UpdateThread extends Thread {
        private volatile boolean done = false;

        public UpdateThread() {
            super("Progress Display Update Thread");
            setDaemon(true);
        }

        public void run() {
            while(!done) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    continue;
                }
                repaint();
            }
            wrapUp();
        }
    }
}
