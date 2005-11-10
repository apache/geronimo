/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.deployment.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.geronimo.deployment.cli.ServerConnection.PasswordPrompt;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.KernelDelegate;

/**
 * @version $Rev$ $Date$
 */
public class StopServer {

	public static final String RMI_NAMING_CONFG_ID = "org/apache/geronimo/RMINaming";

	public static final String DEFAULT_PORT = "1099";

	String port;

	String user;

	String password;

	private String[] args;

	public static void main(String[] args) {
		StopServer cmd = new StopServer();
		cmd.execute(args);
	}

	public void execute(String args[]) {

		this.args = args;

		int i = 0;
		while (i < args.length && args[i].startsWith("--")) {
			if (setParam(i++)) {
				i++;
			}
		}

		if (i < args.length) {
			// There was an argument error somewhere.
			printUsage();
		}

		try {
			if (port != null) {
				Integer.parseInt(port);
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid port number specified.");
			System.exit(1);
		}

		PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out),
				true);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try {
			if (user == null) {
				out.print("Username: ");
				out.flush();
				user = in.readLine();
			}
			if (password == null) {
				password = new PasswordPrompt("Password: ", out)
						.getPassword(in);
			}
		} catch (IOException e) {
			System.out.println("Unable to prompt for login.");
			System.exit(1);
		}

		try {
			if (port == null) {
				port = DEFAULT_PORT;
			}
			System.out.print("Locating server on port " + port + "... ");
			Kernel kernel = null;
			try {
				kernel = getRunningKernel();
			} catch (IOException e) {
				System.out
						.println("\rCould not communicate with the server.  The server may not be running or the port number may be incorrect.");
			}
			if (kernel != null) {
				System.out.println("Server found.");
				System.out.println("Server shutdown begun");
				kernel.shutdown();
				System.out.println("Server shutdown completed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean argumentHasValue(int i) {
		return i + 1 < args.length && !args[i + 1].startsWith("--");
	}

	private boolean setParam(int i) {
		if (argumentHasValue(i)) {
			if (args[i].equals("--user")) {
				user = args[++i];
			} else if (args[i].equals("--password")) {
				password = args[++i];
			} else if (args[i].equals("--port")) {
				port = args[++i];
			} else {
				printUsage();
			}
			return true;
		} else {
			printUsage();
		}
		return false;
	}

	public Kernel getRunningKernel() throws IOException {
		Map map = new HashMap();
		map.put("jmx.remote.credentials", new String[] { user, password });
		Kernel kernel = null;
		try {
			JMXServiceURL address = new JMXServiceURL(
					"service:jmx:rmi:///jndi/rmi://localhost" + ":" + port
							+ "/JMXConnector");
			JMXConnector jmxConnector = JMXConnectorFactory.connect(address,
					map);
			MBeanServerConnection mbServerConnection = jmxConnector
					.getMBeanServerConnection();
			kernel = new KernelDelegate(mbServerConnection);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return kernel;
	}

	public void printUsage() {
		System.out.println();
		System.out.println("Command-line shutdown syntax:");
		System.out.println("    shutdown [options]");
		System.out.println();
		System.out.println("The available options are:");
		System.out.println("    --user");
		System.out.println("    --password");
		System.out.println("    --port");
		System.exit(1);
	}

}
