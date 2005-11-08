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
package org.apache.geronimo.deployment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class ShutdownBootstrap {

	private String shutdownJar;

	private String shutdownClassPath;

	public String getShutdownJar() {
		return shutdownJar;
	}

	public void setShutdownJar(String shutdownJar) {
		this.shutdownJar = shutdownJar;
	}

	public String getShutdownClassPath() {
		return shutdownClassPath;
	}

	public void setShutdownClassPath(String shutdownClassPath) {
		this.shutdownClassPath = shutdownClassPath;
	}

	public void bootstrap() throws Exception {
		ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(
					Bootstrap.class.getClassLoader());
			Manifest manifest = new Manifest();
			Attributes mainAttributes = manifest.getMainAttributes();
			mainAttributes.putValue(
					Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
			mainAttributes.putValue(Attributes.Name.MAIN_CLASS.toString(),
					"org.apache.geronimo.deployment.cli.StopServer");
			mainAttributes.putValue(Attributes.Name.CLASS_PATH.toString(),
					shutdownClassPath);
			createJar(manifest, new File(shutdownJar));
		} finally {
			Thread.currentThread().setContextClassLoader(oldCL);
		}
	}

	private void createJar(Manifest manifest, File destinationFile)
			throws IOException {
		JarOutputStream out = null;
		try {
			if (manifest != null) {
				out = new JarOutputStream(
						new FileOutputStream(destinationFile), manifest);
				// add the startup file which allows us to locate the startup
				// directory
				out.putNextEntry(new ZipEntry("META-INF/startup-jar"));
				out.closeEntry();
			} else {
				out = new JarOutputStream(new FileOutputStream(destinationFile));
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

}
