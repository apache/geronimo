/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.crypto;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;

import javax.crypto.spec.SecretKeySpec;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.crypto.AbstractEncryption;
import org.apache.geronimo.crypto.EncryptionManager;

/**
 * Like SimpleEncryption except it uses a stored secret key. If the key file is
 * missing, it makes up a new one.
 * 
 * WARNING: NOT RECOMMENDED. If you lose the secret key file your encrypted
 * passwords will be unavailable. Instead, secure your operationg environment
 * and use something like ldap or a database to store passwords in.
 * 
 * To use, include something like this in the rmi-naming module of
 * var/config/config.xml:
 * 
 * <gbean name="org.apache.geronimo.framework/rmi-naming/2.1-SNAPSHOT/car?name=ConfiguredEncryption,j2eeType=GBean"
 * gbeanInfo="org.apache.geronimo.system.util.ConfiguredEncryption"> <attribute
 * name="path">var/security/ConfiguredSecretKey.ser</attribute> <reference
 * name="ServerInfo"><pattern><name>ServerInfo</name></pattern></reference>
 * </gbean>
 * 
 * @version $Rev$ $Date$
 */
public class ConfiguredEncryption extends AbstractEncryption implements
		GBeanLifecycle {

	private SecretKeySpec spec;

	public ConfiguredEncryption(String location) throws IOException,
			ClassNotFoundException {
		File keyFile = new File(location);
		ObjectInputStream oin = null;
		if (keyFile != null) {
			if (keyFile.exists()) {
				FileInputStream fi = new FileInputStream(keyFile);
				try {
					oin = new ObjectInputStream(fi);
					spec = (SecretKeySpec) oin.readObject();
				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					if (oin != null)
						oin.close();
					if (fi != null)
						fi.close();
				}
			} else {
				SecureRandom random = new SecureRandom();
				random.setSeed(System.currentTimeMillis());
				byte[] bytes = new byte[16];
				random.nextBytes(bytes);
				spec = new SecretKeySpec(bytes, "AES");
				File dir = keyFile.getParentFile();
				if (!dir.exists()) {
					dir.mkdirs();
				}
				if (!dir.exists() || !dir.isDirectory()) {
					throw new IllegalStateException(
							"Could not create directory for secret key spec: "
									+ dir);
				}
				FileOutputStream out = new FileOutputStream(keyFile);
				try {
					ObjectOutputStream oout = new ObjectOutputStream(out);
					try {
						oout.writeObject(spec);
						oout.flush();
					} finally {
						oout.close();
					}
				} finally {
					out.close();
				}
			}
		}
	}

	public void doStart() throws Exception {
		EncryptionManager.setEncryptionPrefix("{Configured}", this);
	}

	public void doStop() throws Exception {
	}

	public void doFail() {
	}

	protected SecretKeySpec getSecretKeySpec() {
		return spec;
	}

	public static final GBeanInfo GBEAN_INFO;

	static {
		GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(
				ConfiguredEncryption.class, "GBean");
		infoBuilder.addAttribute("path", String.class, true, true);
		infoBuilder.setConstructor(new String[] { "path" });
		GBEAN_INFO = infoBuilder.getBeanInfo();
	}

	public static GBeanInfo getGBeanInfo() {
		return GBEAN_INFO;
	}

}
