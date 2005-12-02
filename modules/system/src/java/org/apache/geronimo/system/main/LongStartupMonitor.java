package org.apache.geronimo.system.main;

import java.io.PrintStream;
import java.util.*;
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
 * A startup monitor that shows the progress of loading and starting
 * configurations, outputing a new line for each configuration started
 * showing the time taken to start the configuration along with the
 * configId.
 *
 * This startup monitor produces more lines of output than the
 * ProgressBarStartupMonitor but its output is suitable for redirection
 * to a file or for when Geronimo is running under an IDE or other tool.
 *
 * A summary will also be produced containing a list of ports
 * Geronimo is listening on, the configIds of application modules
 * that were started and the URLs of Web applications that were started. 
 *
 * @version $Revision: 1.0$
 */
public class LongStartupMonitor implements StartupMonitor {
    private final static Log log = LogFactory.getLog(LongStartupMonitor.class.getName());
    /** Minimum width of the time (padded with leading spaces) in configuration start messages */
    private final static int MIN_TIME_WIDTH=3;
    /** PrintStream */
    private PrintStream out;
    /** Number of configurations to start */
    private int numConfigs;
    /** Number of digits in number of configurations to start */
    private int numConfigsDigits;
    /** Number of configuration currently being started */
    private int configNum;
    /** Time Geronimo was started */
    private long started;
    /** Time the current configuration being processed was started */
    private long configStarted;
    /** The Kernel of the system being started */
    private Kernel kernel;

    public void systemStarting(long startTime) {
        out = System.out;
        started = startTime;
    }

    public void systemStarted(Kernel kernel) {
        this.kernel = kernel;
    }

    public synchronized void foundConfigurations(URI[] configurations) {
        numConfigs = configurations.length;
        numConfigsDigits = Integer.toString(numConfigs).length();
        
    }

    public synchronized void configurationLoading(URI configuration) {
        configNum++;
    }

    public synchronized void configurationLoaded(URI configuration) {
    }

    public synchronized void configurationStarting(URI configuration) {
        configStarted = System.currentTimeMillis();        
    }

    public synchronized void configurationStarted(URI configuration) {
        int time = Math.round((float)(System.currentTimeMillis() - configStarted)/1000f);
        StringBuffer buf = new StringBuffer();
        buf.append("Started configuration ");
        // pad config index
        int configIndexDigits = Integer.toString(configNum).length();
        for(; configIndexDigits < numConfigsDigits; configIndexDigits++) {
            buf.append(' ');
        }
        // pad configuration startup time
        buf.append(configNum).append('/').append(numConfigs).append(' ');
        int timeDigits = Integer.toString(time).length();
        for(; timeDigits < MIN_TIME_WIDTH; timeDigits++) {
            buf.append(' ');
        }
        buf.append(time+"s "+configuration);
        out.println(buf.toString());
    }

    public synchronized void startupFinished() {
        int time = Math.round((float)(System.currentTimeMillis() - started)/1000f);
        
        out.println("Startup completed in "+time+" seconds");
        wrapUp();
    }

    // TODO - We should probably share the wrapUp() code in ProgressBarStartupMonitor
    private void wrapUp() {
        List apps = new ArrayList();  // type = String (message)
        List webs = new ArrayList();  // type = WebAppInfo
        List ports = new ArrayList(); // type = AddressHolder
        Map containers = new HashMap();
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
                if(isWebModule(name)) {
                    String webAppName = name.getCanonicalName();
                    String context = (String) kernel.getAttribute(name, "contextPath");
                    String containerName = (String) kernel.getAttribute(name, "containerName");
                    webs.add(new WebAppInfo(containerName, webAppName, context));
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
            // Look up a URL for each WebContainer in the server (Manager -< Container -< Connector)
            containers = WebAppUtil.mapContainersToURLs(kernel);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (GBeanNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAttributeException e) {
            e.printStackTrace();
        } catch (Exception e) { // required by Kernel.getAttribute
            e.printStackTrace();
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
            out.println();
        }
        // Helpful output: list of applications started
        if(apps.size() > 0) {
            out.println("  Started Application Modules:");
            for (int i = 0; i < apps.size(); i++) {
                out.println((String)apps.get(i));
            }
            out.println();
        }
        // Helpful output: Web URLs
        if(webs.size() > 0) {
            Collections.sort(webs);
            out.println("  Web Applications:");
            for (int i = 0; i < webs.size(); i++) {
                WebAppInfo app = (WebAppInfo) webs.get(i);
                out.println("    "+containers.get(app.getContainerObjectName())+app.getContext());
            }
            out.println();
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
            out.println();
        }

        StringBuffer msg = new StringBuffer();
        msg.append("Geronimo Application Server started");
        if(serverInfo != null) {
            msg.append(" (version ").append(serverInfo).append(")");
        }
        out.println(msg.toString());
        out.flush();
    }

    // TODO Review - Currently loadFailed is not called by Daemon
    public synchronized void loadFailed(String configuration, Exception problem) {
        out.println("Failed to load configuration "+configuration);
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
        out.println("Failed to start configuration "+configuration);
        // We print the stack track now (rather than defering the printing of it)
        // since other problems that may occur during the start of a configuration 
        // (e.g. an individual GBean not being able to start) produce 
        // errors in the log (and therefore standard output) immediately.
        problem.printStackTrace(out);
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

    private static boolean isWebModule(ObjectName on) {
        String type = on.getKeyProperty("j2eeType");
        return type != null && type.equals("WebModule");
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

    private static class WebAppInfo implements Comparable {
        private String containerObjectName;
        private String webAppObjectName;
        private String context;

        public WebAppInfo(String containerObjectName, String webAppObjectName, String context) {
            this.containerObjectName = containerObjectName;
            this.webAppObjectName = webAppObjectName;
            this.context = context;
        }

        public String getContainerObjectName() {
            return containerObjectName;
        }

        public String getWebAppObjectName() {
            return webAppObjectName;
        }

        public String getContext() {
            return context;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof WebAppInfo)) return false;

            final WebAppInfo webAppInfo = (WebAppInfo) o;

            if (!containerObjectName.equals(webAppInfo.containerObjectName)) return false;
            if (!context.equals(webAppInfo.context)) return false;
            if (!webAppObjectName.equals(webAppInfo.webAppObjectName)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = containerObjectName.hashCode();
            result = 29 * result + webAppObjectName.hashCode();
            result = 29 * result + context.hashCode();
            return result;
        }

        public int compareTo(Object o) {
            WebAppInfo other = (WebAppInfo) o;
            int test = containerObjectName.compareTo(other.containerObjectName);
            if(test != 0) return test;
            return context.compareTo(other.context);
        }
    }
}
