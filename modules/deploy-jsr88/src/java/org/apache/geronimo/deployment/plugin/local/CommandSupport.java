/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.deployment.plugin.local;

import java.util.*;
import java.io.StringWriter;
import java.io.PrintWriter;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager.CommandContext;
import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.gbean.GBeanQuery;

/**
 * @version $Rev$ $Date$
 */
public abstract class CommandSupport implements ProgressObject, Runnable {
    private final CommandType command;
    private ActionType action;
    private StateType state;
    private String message;
    private final Set listeners = new HashSet();
    private final List moduleIDs = new ArrayList();
    private CommandContext commandContext = new CommandContext();

    private ProgressEvent event = null;

    protected CommandSupport(CommandType command) {
        this.command = command;
        this.action = ActionType.EXECUTE;
        this.state = StateType.RUNNING;
        this.message = null;
    }

    protected synchronized void addModule(TargetModuleID moduleID) {
        moduleIDs.add(moduleID);
    }

    protected synchronized int getModuleCount() {
        return moduleIDs.size();
    }

    public synchronized TargetModuleID[] getResultTargetModuleIDs() {
        return (TargetModuleID[]) moduleIDs.toArray(new TargetModuleID[moduleIDs.size()]);
    }

    public synchronized DeploymentStatus getDeploymentStatus() {
        return new Status(command, action, state, message);
    }

    public ClientConfiguration getClientConfiguration(TargetModuleID id) {
        return null;
    }

    public boolean isCancelSupported() {
        return false;
    }

    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("cancel not supported");
    }

    public boolean isStopSupported() {
        return false;
    }

    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("stop not supported");
    }

    public void addProgressListener(ProgressListener pol) {
        ProgressEvent event = null;
        synchronized (this) {
            listeners.add(pol);
            event = this.event;
        }
        if (event != null) {
            pol.handleProgressEvent(event);
        }
    }

    public synchronized void removeProgressListener(ProgressListener pol) {
        listeners.remove(pol);
    }

    protected final void fail(String message) {
        sendEvent(message, StateType.FAILED);
    }

    protected final void complete(String message) {
        sendEvent(message, StateType.COMPLETED);
    }

    protected final void updateStatus(String message) {
        sendEvent(message, state);
    }

    protected void doFail(Exception e) {
        if (e instanceof InternalKernelException) {
            Exception test = (Exception)((InternalKernelException)e).getCause();
            if(test != null) {
                e = test;
            }
        }

        if (commandContext.isLogErrors()) {
            System.err.println("Deployer operation failed: " + e.getMessage());
            if (commandContext.isVerbose()) {
                e.printStackTrace(System.err);
            }
        }

        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        printWriter.println(e.getMessage());
        if (commandContext.isVerbose()) {
            e.printStackTrace(printWriter);
        } else {
            Throwable throwable = e;
            while (null != (throwable = throwable.getCause())) {
                printWriter.println("\t" + throwable.getMessage());
            }
        }
        fail(writer.toString());
    }

    private void sendEvent(String message, StateType state) {
        assert !Thread.holdsLock(this) : "Trying to send event whilst holding lock";

        ProgressListener[] toNotify;
        DeploymentStatus newStatus;
        synchronized (this) {
            this.message = message;
            this.state = state;
            newStatus = new Status(command, action, state, message);
            toNotify = (ProgressListener[]) listeners.toArray(new ProgressListener[listeners.size()]);
            event = new ProgressEvent(this, null, newStatus);
        }

        for (int i = 0; i < toNotify.length; i++) {
            toNotify[i].handleProgressEvent(event);
        }
    }

    protected static String clean(String value) {
        if(value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length()-1);
        }
        return value;
    }

    private static class Status implements DeploymentStatus {
        private final CommandType command;
        private final ActionType action;
        private final StateType state;
        private final String message;

        public Status(CommandType command, ActionType action, StateType state, String message) {
            this.command = command;
            this.action = action;
            this.state = state;
            this.message = message;
        }

        public CommandType getCommand() {
            return command;
        }

        public ActionType getAction() {
            return action;
        }

        public String getMessage() {
            return message;
        }

        public StateType getState() {
            return state;
        }

        public boolean isRunning() {
            return StateType.RUNNING.equals(state);
        }

        public boolean isCompleted() {
            return StateType.COMPLETED.equals(state);
        }

        public boolean isFailed() {
            return StateType.FAILED.equals(state);
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("DeploymentStatus[").append(command).append(',');
            buf.append(action).append(',');
            buf.append(state);
            if (message != null) {
                buf.append(',').append(message);
            }
            buf.append(']');
            return buf.toString();
        }
    }

    public CommandContext getCommandContext() {
        return commandContext;
    }

    public void setCommandContext(CommandContext commandContext) {
        this.commandContext = commandContext;
    }

    public static ModuleType convertModuleType(ConfigurationModuleType type) {
        if(type.getValue() == ConfigurationModuleType.WAR.getValue()) {
            return ModuleType.WAR;
        }
        if(type.getValue() == ConfigurationModuleType.RAR.getValue()) {
            return ModuleType.RAR;
        }
        if(type.getValue() == ConfigurationModuleType.EJB.getValue()) {
            return ModuleType.EJB;
        }
        if(type.getValue() == ConfigurationModuleType.EAR.getValue()) {
            return ModuleType.EAR;
        }
        if(type.getValue() == ConfigurationModuleType.CAR.getValue()) {
            return ModuleType.CAR;
        }
        return null;
    }

    public static boolean isWebApp(Kernel kernel, String configName) {
        try {
            Set set = kernel.listGBeans(new ObjectName("*:j2eeType=WebModule,name="+configName+",*"));
            return set.size() > 0;
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected void addWebURLs(Kernel kernel) {
        addWebURLs(kernel, moduleIDs);
    }

    /**
     * Given a list of TargetModuleIDs, figure out which ones represent web
     * modules and add a WebURL to each if possible.
     */
    public static void addWebURLs(Kernel kernel, List moduleIDs) {
        Set webApps = null;
        Map containers = null;
        try {
            containers = mapContainersToURLs(kernel);
        } catch (Exception e) {
            e.printStackTrace();
            containers = Collections.EMPTY_MAP;
        }
        for (int i = 0; i < moduleIDs.size(); i++) {
            TargetModuleIDImpl id = (TargetModuleIDImpl) moduleIDs.get(i);
            if(id.getType() != null && id.getType().getValue() == ModuleType.WAR.getValue()) {
                if(webApps == null) {
                    webApps = kernel.listGBeans(new GBeanQuery(null, "org.apache.geronimo.management.geronimo.WebModule"));
                }
                for (Iterator it = webApps.iterator(); it.hasNext();) {
                    ObjectName name = (ObjectName) it.next();
                    if(name.getKeyProperty("name").equals(id.getModuleID())) {
                        try {
                            String container = (String) kernel.getAttribute(name, "containerName");
                            String context = (String) kernel.getAttribute(name, "contextPath");
                            id.setWebURL(containers.get(container)+context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if(id.getChildTargetModuleID() != null) {
                addWebURLs(kernel, Arrays.asList(id.getChildTargetModuleID()));
            }
        }
    }

    public static List loadChildren(Kernel kernel, String configName) throws MalformedObjectNameException {
        List kids = new ArrayList();
        Set test = kernel.listGBeans(new ObjectName("*:J2EEApplication="+configName+",j2eeType=WebModule,*"));
        for (Iterator it = test.iterator(); it.hasNext();) {
            ObjectName child = (ObjectName) it.next();
            String childName = child.getKeyProperty("name");
            kids.add(childName);
        }
        test = kernel.listGBeans(new ObjectName("*:J2EEApplication="+configName+",j2eeType=EJBModule,*"));
        for (Iterator it = test.iterator(); it.hasNext();) {
            ObjectName child = (ObjectName) it.next();
            String childName = child.getKeyProperty("name");
            kids.add(childName);
        }
        test = kernel.listGBeans(new ObjectName("*:J2EEApplication="+configName+",j2eeType=AppClientModule,*"));
        for (Iterator it = test.iterator(); it.hasNext();) {
            ObjectName child = (ObjectName) it.next();
            String childName = child.getKeyProperty("name");
            kids.add(childName);
        }
        test = kernel.listGBeans(new ObjectName("*:J2EEApplication="+configName+",j2eeType=ResourceAdapterModule,*"));
        for (Iterator it = test.iterator(); it.hasNext();) {
            ObjectName child = (ObjectName) it.next();
            String childName = child.getKeyProperty("name");
            kids.add(childName);
        }
        return kids;
    }

    /**
     * Generates a Map where the keys are web container object names (as Strings)
     * and the values are URLs (as Strings) to connect to a web app running in
     * the matching container (though the web app context needs to be added to
     * the end to be complete).
     *
     * NOTE: same as a method in geronimo-system WebAppUtil, but neither
     *       module should obviously be dependent on the other and it's not
     *       clear that this belongs in geronimo-common
     */
    public static Map mapContainersToURLs(Kernel kernel) throws Exception {
        Map containers = new HashMap();
        Set set = kernel.listGBeans(new GBeanQuery(null, "org.apache.geronimo.management.geronimo.WebManager"));
        for (Iterator it = set.iterator(); it.hasNext();) {
            ObjectName mgrName = (ObjectName) it.next();
            String[] cntNames = (String[]) kernel.getAttribute(mgrName, "containers");
            for (int i = 0; i < cntNames.length; i++) {
                String cntName = cntNames[i];
                String[] cncNames = (String[]) kernel.invoke(mgrName, "getConnectorsForContainer", new Object[]{cntName}, new String[]{"java.lang.String"});
                Map map = new HashMap();
                for (int j = 0; j < cncNames.length; j++) {
                    ObjectName cncName = ObjectName.getInstance(cncNames[j]);
                    String protocol = (String) kernel.getAttribute(cncName, "protocol");
                    String url = (String) kernel.getAttribute(cncName, "connectUrl");
                    map.put(protocol, url);
                }
                String urlPrefix = "";
                if((urlPrefix = (String) map.get("HTTP")) == null) {
                    if((urlPrefix = (String) map.get("HTTPS")) == null) {
                        urlPrefix = (String) map.get("AJP");
                    }
                }
                containers.put(cntName, urlPrefix);
            }
        }
        return containers;
    }
}
