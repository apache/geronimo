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
package org.apache.geronimo.system.configuration;

import java.io.File;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;

/**
 * @version $Rev$ $Date$
 */
public class InPlaceConfigurationUtilTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));

    private static final File baseConfigDir = new File(basedir, "target/config");
    private static final File configDir = new File(baseConfigDir, "config");
    private static final File inPlaceConfig = new File(baseConfigDir, "inPlaceConfig");

    private InPlaceConfigurationUtil inPlaceConfUtil;

	public void testWriteReadInPlaceLocation() throws Exception {
		ConfigurationData configurationData = new ConfigurationData(null,
                Collections.EMPTY_LIST,
				Collections.EMPTY_MAP,
				new Environment(new Artifact("groupId", "artifactId", "version", "type")),
				configDir,
				inPlaceConfig,
				new Jsr77Naming());
		
        inPlaceConfUtil.writeInPlaceLocation(configurationData, configDir);
		
		File actualInPlaceConfig = inPlaceConfUtil.readInPlaceLocation(configDir);
		assertEquals(inPlaceConfig, actualInPlaceConfig);
	}

	public void testAttemptReadNotExistingInPlaceLocation() throws Exception {
		File actualInPlaceConfig = inPlaceConfUtil.readInPlaceLocation(configDir);
		assertNull(actualInPlaceConfig);
	}

    public void testIsInPlaceConfiguration() throws Exception {
        assertFalse(inPlaceConfUtil.isInPlaceConfiguration(configDir));
        
        ConfigurationData configurationData = new ConfigurationData(null,
                Collections.EMPTY_LIST,
                Collections.EMPTY_MAP,
                new Environment(new Artifact("groupId", "artifactId", "version", "type")),
                configDir,
                inPlaceConfig,
                new Jsr77Naming());
        
        inPlaceConfUtil.writeInPlaceLocation(configurationData, configDir);
        assertTrue(inPlaceConfUtil.isInPlaceConfiguration(configDir));
    }

	protected void setUp() throws Exception {
		configDir.mkdirs();
		inPlaceConfig.mkdirs();
        
        inPlaceConfUtil = new InPlaceConfigurationUtil();
	}
	
	protected void tearDown() throws Exception {
		recursiveDelete(baseConfigDir);
	}
	
	private final void recursiveDelete(File dir) {
		File[] nestedFiles = dir.listFiles();
		if (null != nestedFiles) {
			for (int i = 0; i < nestedFiles.length; i++) {
				recursiveDelete(nestedFiles[i]);
			}
		}
		dir.delete();
	}
}
