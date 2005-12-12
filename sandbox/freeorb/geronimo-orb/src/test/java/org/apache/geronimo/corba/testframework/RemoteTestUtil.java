/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.corba.testframework;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

class RemoteTestUtil {
	public static void invokeAndRethrow(Object obj, Method method)
			throws Throwable {
		try {
			method.invoke(obj, null);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}

	}

	// Forks a new java process.
	public static Process execJava(String className, Properties props,
			String[] javaArgs) throws IOException {
		// Get path to java binary
		File javaHome = new File(System.getProperty("java.home"));
		File javaBin = new File(new File(javaHome, "bin"), "java");

		// Get current class path
		URLClassLoader cl = (URLClassLoader) RemoteTest.class.getClassLoader();
		URL[] classPathUrls = cl.getURLs();

		// Construct list of arguments
		List binArgs = new ArrayList();

		// First argument is the java binary to run
		binArgs.add(javaBin.getPath());

		// Add the classpath to argument list
		binArgs.add("-classpath");
		String classPath = ""
				+ new File(classPathUrls[0].getPath().replaceAll("%20", " "));
		for (int i = 1; i < classPathUrls.length; i++) {
			classPath += File.pathSeparator
					+ new File(classPathUrls[i].getPath()
							.replaceAll("%20", " "));
		}
		classPath += "";
		binArgs.add(classPath);

		// Add properties to argument list
		Enumeration en = props.keys();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			binArgs.add("-D" + key + "=" + props.getProperty(key));
		}

		// Add class containing main method to arg list
		binArgs.add(className);

		// Add java arguments
		for (int i = 0; i < javaArgs.length; i++) {
			binArgs.add(javaArgs[i]);
		}

		// Convert arg list to array
		String[] argArray = new String[binArgs.size()];
		for (int i = 0; i < argArray.length; i++) {
			argArray[i] = (String) binArgs.get(i);
		}

		for (int i = 0; i < argArray.length; i++) {
			System.out.print(argArray[i] + " ");
		}
		System.out.println();

		// Fork process
		return Runtime.getRuntime().exec(argArray);
	}

	public static void redirectStream(final InputStream in,
			final OutputStream out, final String streamName) {
		Thread stdoutTransferThread = new Thread() {
			public void run() {
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(out),
						true);
				try {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(in));
					String line;
					while ((line = reader.readLine()) != null) {
						pw.print("[");
						pw.print(streamName);
						pw.print("] ");
						pw.println(line);
					}
					pw.print("[");
					pw.print(streamName);
					pw.print("] ");
					pw.println("EOF");
				} catch (Throwable e) {
					e.printStackTrace();
					// throw new Error(e);
				}
			}
		};
		stdoutTransferThread.start();
	}
}
