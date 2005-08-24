/**
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable
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
package org.apache.geronimo.core.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.jmx.KernelDelegate;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.core.IJ2EEModule;
import org.eclipse.jst.server.generic.core.internal.GenericServerBehaviour;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;

public class GeronimoServerBehaviour extends GenericServerBehaviour {

	private final static String DEFAULT_URI = "deployer:geronimo:jmx:rmi://localhost/jndi/rmi:/JMXConnector";

	private final static String J2EE_DEPLOYER_ID = "org/apache/geronimo/RuntimeDeployer";
    
    private static final int MAX_TRIES = 10;
    
    private static final long TIMEOUT = 100000;

	private DeploymentFactoryManager dfm = null;

	private DeploymentManager dm = null;

	private IProgressMonitor _monitor = null;

	private Kernel kernel = null;

	public GeronimoServerBehaviour() {
		super();
	}

	private void discoverDeploymentFactory() {

		try {
			JarFile deployerJar = new JarFile(getServer().getRuntime().getLocation().append(
					"/deployer.jar").toFile());
			java.util.jar.Manifest manifestFile = deployerJar.getManifest();
			Attributes attributes = manifestFile.getMainAttributes();
			String key = "J2EE-DeploymentFactory-Implementation-Class";
			String className = attributes.getValue(key);
			dfm = DeploymentFactoryManager.getInstance();
			Class deploymentFactory = Class.forName(className);
			DeploymentFactory deploymentFactoryInstance = (DeploymentFactory) deploymentFactory
					.newInstance();
			dfm.registerDeploymentFactory(deploymentFactoryInstance);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public DeploymentManager getDeploymentManager() throws DeploymentManagerCreationException {

		if (dm == null) {
			discoverDeploymentFactory();
			if (dfm != null) {
				dm = dfm.getDeploymentManager(DEFAULT_URI, getUserName(), getPassword());
			} else {
				DeploymentFactory df = new DeploymentFactoryImpl();
				dm = df.getDeploymentManager(DEFAULT_URI, getUserName(), getPassword());
			}
		}
		return dm;
	}

	private String getUserName() {
		return "system";
	}

	private String getPassword() {
		return "manager";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.model.ServerBehaviourDelegate#stop(boolean)
	 */
	public void stop(boolean force) {       
	               
		if (getKernel() != null) {
            //lets shutdown the kernel so shutdown messages are displayed in the console view
			kernel.shutdown();
		}           
                

		dm = null;
		kernel = null;
        
		//kill the process
		super.stop(true);
	}

	private Kernel getKernel() {

		int tries = MAX_TRIES;

		if (kernel == null) {

			Map map = new HashMap();
			map.put("jmx.remote.credentials", new String[] { getUserName(), getPassword() });
			try {
				JMXServiceURL address = new JMXServiceURL(
						"service:jmx:rmi://localhost/jndi/rmi:/JMXConnector");
				do {
					try {

						JMXConnector jmxConnector = JMXConnectorFactory.connect(address, map);
						MBeanServerConnection mbServerConnection = jmxConnector
								.getMBeanServerConnection();
						kernel = new KernelDelegate(mbServerConnection);
						Trace.trace(Trace.INFO, "Connected to kernel.");
						break;
					} catch (Exception e) {
						Thread.sleep(3000);
						tries--;
						if (tries != 0) {
                            Trace.trace(Trace.WARNING,"Couldn't connect to kernel.  Trying again...");
						} else {
                            Trace.trace(Trace.SEVERE,"Connection to Geronimo kernel failed.", e);
						}
					}
				} while (tries > 0);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return kernel;
	}

	protected void setServerStarted() {

		boolean started = false;

		try {
			ObjectName configName = Configuration.getConfigurationObjectName(new URI(
					J2EE_DEPLOYER_ID));

			for (int tries = MAX_TRIES; tries > 0 && !started; tries--) {
				try {
					if (getKernel() != null) {
						if (kernel.getGBeanState(configName) == 1) {
							started = true;
                            setServerState(IServer.STATE_STARTED);
							Trace.trace(Trace.INFO, "RuntimeDeployer has started.");
						} else {
							Trace.trace(Trace.INFO, "RuntimeDeployer has not yet started.");
						}
					}
				} catch (InternalKernelException e) {
				} catch (GBeanNotFoundException e) {
				}
				Thread.sleep(2000);
			}
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (!started) {
			Trace.trace(Trace.SEVERE, "Runtime deployer failed to start.");
		}		

	}

	public void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor)
			throws CoreException {                

		_monitor = monitor;

		// Can only publish when the server is running
		int state = getServer().getServerState();
		if (state == IServer.STATE_STOPPED || state == IServer.STATE_STOPPING) {
			throw new CoreException(Status.CANCEL_STATUS);
		}

		if (state == IServer.STATE_STARTING) {
			int timeout = 25;
			while (getServer().getServerState() == IServer.STATE_STARTING) {
				if (--timeout == 0)
					throw new CoreException(Status.CANCEL_STATUS);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		if (deltaKind == NO_CHANGE) // Temporary workaround for WTP server tools
			// bug
			deltaKind = CHANGED;

		if (!(deltaKind == ADDED || deltaKind == REMOVED || deltaKind == CHANGED))
			return;

		invokeCommand(deltaKind, module[module.length - 1]);
	}

	class WaitForNotificationThread extends Thread {
		public void run() {
			try {
				sleep(TIMEOUT);
			} catch (InterruptedException e) {
			}
		}
	}

	class GeronimoDeploymentProgressListener implements ProgressListener {

		private Thread waitThread;

		private CommandType cmd = null;

		private IProject project = null;

		public String lastMessage = null;

		public GeronimoDeploymentProgressListener() {
			waitThread = new WaitForNotificationThread();
		}

		public void handleProgressEvent(ProgressEvent event) {
			String message = DeploymentStatusMessageTranslator.getTranslatedMessage(event, project);			
			if (!message.equals(lastMessage)) {
                _monitor.setTaskName(message);
				Trace.trace(Trace.INFO,message);
			}
			lastMessage = message;
			DeploymentStatus status = event.getDeploymentStatus();
			if (status.getMessage() != null) {
				Trace.trace(Trace.INFO,"\t" + status.getMessage());
				_monitor.subTask(status.getMessage());
			}
			if (cmd == null || cmd == status.getCommand()) {
				if (status.isCompleted() || status.isFailed()) {
					waitThread.interrupt();
				}
			}
		}

		public void start() {
			waitThread.start();
		}

		public void setType(CommandType cmd) {
			this.cmd = cmd;
		}

		public Thread getWaitThread() {
			return waitThread;
		}

		public void setProject(IProject project) {
			this.project = project;
		}
	}

	private void waitForCompletion(ProgressObject po, GeronimoDeploymentProgressListener listener,
			CommandType cmd, IProject project) {

		listener.setType(cmd);
		listener.setProject(project);

		po.addProgressListener(listener);

		try {
			listener.getWaitThread().join();
		} catch (InterruptedException e) {

		} finally {
			po.removeProgressListener(listener);
		}
	}

	private void invokeCommand(int deltaKind, IModule module) throws CoreException {

		GeronimoUtils.getConfigId(module); //triggers web dp creation

		//GeronimoUtils.copyDeploymentPlanToDeployable(module); // Temporary

		try {
			switch (deltaKind) {
			case ADDED: {
				doDeploy(module);
				break;
			}
			case CHANGED: {
				doReploy(module);
				break;
			}
			case REMOVED: {
				doUndeploy(module);
				break;
			}
			default:
				throw new IllegalArgumentException();
			}
		}  catch (DeploymentManagerCreationException e) {			
			e.printStackTrace();
			throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.jst.geronimo.core", 0,e.getMessage(), e));
		}
	}

	private void doDeploy(IModule module) throws CoreException, DeploymentManagerCreationException {

		IJ2EEModule j2eeModule = (IJ2EEModule) module.loadAdapter(IJ2EEModule.class, null);

		Target[] targets = getDeploymentManager().getTargets();
		File jarFile = createJarFile(j2eeModule.getLocation());

		GeronimoDeploymentProgressListener listener = createAndStartListener();

		ProgressObject po = getDeploymentManager().distribute(targets, jarFile, null);
		waitForCompletion(po, listener, CommandType.DISTRIBUTE, module.getProject());

		if (po.getDeploymentStatus().isCompleted()) {

			listener = createAndStartListener();

			po = getDeploymentManager().start(po.getResultTargetModuleIDs());
			waitForCompletion(po, listener, CommandType.START, module.getProject());

			if (po.getDeploymentStatus().isCompleted()) {

			} else if (po.getDeploymentStatus().isFailed()) {
				// TODO handle fail
			}
		} else if (po.getDeploymentStatus().isFailed()) {
			IStatus status = new Status(IStatus.ERROR, "org.eclipse.jst.geronimo.core", 0, "Distribution of application failed.  See .log for details.", new Exception(listener.lastMessage));
			throw new CoreException(status);
		}
	}

	private void doReploy(IModule module) throws CoreException, DeploymentManagerCreationException{

		IJ2EEModule j2eeModule = (IJ2EEModule) module.loadAdapter(IJ2EEModule.class, null);

		TargetModuleID id = getTargetModuleID(module);
		if (id != null) {
			File jarFile = createJarFile(j2eeModule.getLocation());
			GeronimoDeploymentProgressListener listener = createAndStartListener();
			ProgressObject po = getDeploymentManager().redeploy(new TargetModuleID[] { id },
					jarFile, null);
			waitForCompletion(po, listener, CommandType.REDEPLOY, module.getProject());
			if (po.getDeploymentStatus().isCompleted()) {

			} else if (po.getDeploymentStatus().isFailed()) {
				// TODO handle fail
			}
		}
	}

	private void doUndeploy(IModule module) throws CoreException, DeploymentManagerCreationException {
		TargetModuleID id = getTargetModuleID(module);
		if (id != null) {
			GeronimoDeploymentProgressListener listener = createAndStartListener();
			ProgressObject po = getDeploymentManager().undeploy(new TargetModuleID[] { id });
			waitForCompletion(po, listener, CommandType.UNDEPLOY, module.getProject());
			if (po.getDeploymentStatus().isCompleted()) {

			} else if (po.getDeploymentStatus().isFailed()) {
				// TODO handle fail
			}
		}
	}

	private GeronimoDeploymentProgressListener createAndStartListener() {
		GeronimoDeploymentProgressListener listener = new GeronimoDeploymentProgressListener();
		listener.start();
		return listener;
	}

	// TODO find a better way to get TargetModuleID for IModule
	private TargetModuleID getTargetModuleID(IModule module) throws DeploymentManagerCreationException {
		try {
			TargetModuleID ids[] = getDeploymentManager().getAvailableModules(
					GeronimoUtils.getJSR88ModuleType(module), getDeploymentManager().getTargets());
			if (ids != null) {
				for (int i = 0; i < ids.length; i++) {
					if (ids[i].getModuleID().equals(GeronimoUtils.getConfigId(module))) {
						return ids[i];
					}
				}
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (TargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	private File createJarFile(IPath location) {

		try {

			String rootFilename = location.toOSString();

			File rootDir = new File(rootFilename);
			String zipFilePrefix = rootDir.getName();
			if (zipFilePrefix.length() < 3)
				zipFilePrefix += "123";
			File zipFile = File.createTempFile(zipFilePrefix, null);

			if (zipFile.exists())
				zipFile.delete();

			FileOutputStream fos = new FileOutputStream(zipFile);
			JarOutputStream jos = new JarOutputStream(fos);

			addToJar("", rootDir, jos);

			jos.close();
			fos.close();

			zipFile.deleteOnExit();

			return zipFile;

		} catch (IOException e) {
			Trace.trace(Trace.SEVERE, "Error creating zip file", e);
			return null;
		}
	}

	private void addToJar(String namePrefix, File dir, JarOutputStream jos) throws IOException {
		File[] contents = dir.listFiles();
		for (int i = 0; i < contents.length; i++) {
			File f = contents[i];
			if (f.isDirectory()) {
				// Recurse into the directory
				addToJar(namePrefix + f.getName() + "/", f, jos);
			} else {
				JarEntry entry = new JarEntry(namePrefix + f.getName());
				jos.putNextEntry(entry);

				byte[] buffer = new byte[10000];
				FileInputStream fis = new FileInputStream(f);
				int bytesRead = 0;
				while (bytesRead != -1) {
					bytesRead = fis.read(buffer);
					if (bytesRead > 0)
						jos.write(buffer, 0, bytesRead);
				}
			}
		}
	}

	protected List getPublishClasspath() {
		String cpRef = getServerDefinition().getStop().getClasspathReference();
		return serverClasspath(cpRef);
	}

	public Map getServerInstanceProperties() {
		return getRuntimeDelegate().getServerInstanceProperties();
	}

}