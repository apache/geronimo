/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis;

import org.apache.geronimo.kernel.Kernel;

import java.io.File;

/**
 * @author hemapani@opensource.lk
 */
public class WebServiceDeploymentTest extends AbstractTestCase{
	private Kernel kernel;
	public WebServiceDeploymentTest(String name){
		super(name);
	}

	protected void setUp() throws Exception {
		kernel = new Kernel("test.kernel", "test");
		kernel.boot();
		File file = new File(tempDir);
		file.getParentFile().mkdirs();
	}

	protected void tearDown() throws Exception {
		kernel.shutdown();
		File file = new File(tempDir);
		AxisGeronimoUtils.delete(file);
	}
    
   
    
    public void testDeployEJB() throws Exception{
		WebServiceDeployer deployer 
			= new WebServiceDeployer(tempDir,kernel);
		deployer.deploy(getTestFile("target/samples/echo.jar"),
				null,
				"ws/apache/axis/test2");
    }
}
