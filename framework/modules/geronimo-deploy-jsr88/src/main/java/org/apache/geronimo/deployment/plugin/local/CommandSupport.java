/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;
import org.apache.geronimo.deployment.plugin.jmx.CommandContext;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;

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
    protected CommandContext commandContext = null; //todo: this is pretty bad; should add it into constructor

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
        ProgressEvent event;
        synchronized (this) {
            listeners.add(pol);
            event = this.event;
        }
        if(event != null) {
            pol.handleProgressEvent(event);
        }
    }

    public synchronized void removeProgressListener(ProgressListener pol) {
        listeners.remove(pol);
    }

    public final void fail(String message) {
        sendEvent(message, StateType.FAILED);
    }

    protected final void complete(String message) {
        sendEvent(message, StateType.COMPLETED);
    }

    public final void updateStatus(String message) {
        sendEvent(message, state);
    }

    public void doFail(Throwable e) {
        if (e instanceof InternalKernelException) {
            Exception test = (Exception)e.getCause();
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
            StringBuilder buf = new StringBuilder();
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
        this.commandContext = new CommandContext(commandContext);
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
        Map filter = new HashMap();
        filter.put("j2eeType", "WebModule");
        filter.put("name", configName);
        Set set = kernel.listGBeans(new AbstractNameQuery(null, filter));
        return set.size() > 0;
    }

    protected void addWebURLs(Kernel kernel) throws Exception{
        addWebContextPaths(kernel, moduleIDs);
    }

    /**
     * Given a list of TargetModuleIDs, figure out which ones represent web
     * modules and add a WebURL to each if possible.
     */
    public static void addWebContextPaths(Kernel kernel, List moduleIDs) throws Exception{
        Set webApps = null;
        for (int i = 0; i < moduleIDs.size(); i++) {
            TargetModuleIDImpl id = (TargetModuleIDImpl) moduleIDs.get(i);
            if(id.getType() != null && id.getType().getValue() == ModuleType.WAR.getValue()) {
                if(webApps == null) {
                    webApps = kernel.listGBeans(new AbstractNameQuery("org.apache.geronimo.management.geronimo.WebModule"));
                }
                for (Iterator it = webApps.iterator(); it.hasNext();) {
                    AbstractName name = (AbstractName) it.next();
                    if(name.getName().get("name").equals(id.getModuleID())) {
                        id.setWebURL(kernel.getAttribute(name, "contextPath").toString());
                        break;
                    }
                }
            }
            if(id.getChildTargetModuleID() != null) {
                addWebContextPaths(kernel, Arrays.asList(id.getChildTargetModuleID()));
            }
        }
    }

    public static List loadChildren(Kernel kernel, String configName) {
        List kids = new ArrayList();

        Map filter = new HashMap();
        filter.put("J2EEApplication", configName);

        filter.put("j2eeType", "WebModule");
        Set test = kernel.listGBeans(new AbstractNameQuery(null, filter));
        for (Iterator it = test.iterator(); it.hasNext();) {
            AbstractName child = (AbstractName) it.next();
            String childName = child.getNameProperty("name");
            kids.add(childName);
        }

        filter.put("j2eeType", "EJBModule");
        test = kernel.listGBeans(new AbstractNameQuery(null, filter));
        for (Iterator it = test.iterator(); it.hasNext();) {
            AbstractName child = (AbstractName) it.next();
            String childName = child.getNameProperty("name");
            kids.add(childName);
        }

        filter.put("j2eeType", "AppClientModule");
        test = kernel.listGBeans(new AbstractNameQuery(null, filter));
        for (Iterator it = test.iterator(); it.hasNext();) {
            AbstractName child = (AbstractName) it.next();
            String childName = child.getNameProperty("name");
            kids.add(childName);
        }

        filter.put("j2eeType", "ResourceAdapterModule");
        test = kernel.listGBeans(new AbstractNameQuery(null, filter));
        for (Iterator it = test.iterator(); it.hasNext();) {
            AbstractName child = (AbstractName) it.next();
            String childName = child.getNameProperty("name");
            kids.add(childName);
        }
        return kids;
    }
}
