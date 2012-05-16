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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;

import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.crypto.AbstractEncryption;

 /* 
 * @version $Rev$ $Date$
 */
public class ConfiguredEncryption extends AbstractEncryption{

	private final static Log log = LogFactory.getLog(ConfiguredEncryption.class);
	
	private SecretKeySpec spec;

	public ConfiguredEncryption(String location) throws IOException{
		File keyFile = new File(location);
		ObjectInputStream oin = null;
		if (keyFile != null) {
			if (keyFile.exists()) {
				FileInputStream fi = new FileInputStream(keyFile);
				try {
					oin = new ObjectInputStream(fi);
					spec = (SecretKeySpec) oin.readObject();
				} catch (ClassNotFoundException e) {
					log.error("Unable to read object or class not found: ", e);
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
				log.info("Generate a new configured encryption password: "+spec.getEncoded().toString());
			}
		}
	}

	@Override
	protected SecretKeySpec getSecretKeySpec() {
		return spec;
	}

}
