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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import javax.management.ObjectName;

import org.apache.geronimo.ews.ws4j2ee.toWs.Ws4J2ee;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

public class WebServiceDeploymentTest extends AbstractTestCase{
	private Kernel kernel;
    private ObjectName name;
    private JettyServiceWrapper jettyService;
    private File jarFile;

	public WebServiceDeploymentTest(String name){
		super(name);
	}


    
    public void testDeployEJB() throws Exception{
		WebServiceDeployer deployer 
			= new WebServiceDeployer(tempDir,kernel);
		deployer.deployEWSModule(getTestFile("target/generated/samples/echo-ewsimpl.jar"),
				null,
				"ws/apache/axis/test2");
    }
    
    protected void setUp() throws Exception {
        new File(outDir).mkdirs();
        name = new ObjectName("test:name=AxisGBean");
        kernel = new Kernel("test.kernel", "test");
        kernel.boot();
        jettyService = new JettyServiceWrapper(kernel);
        jettyService.doStart();
        ClassLoader cl = getClass().getClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[0], cl);
        GBeanMBean gbean = new GBeanMBean(AxisGbean.getGBeanInfo(), myCl);
        gbean.setAttribute("Name", "Test");
        kernel.loadGBean(name, gbean);
        kernel.startGBean(name);
        
        jarFile =  new File(outDir + "/echo-ewsimpl.jar");
        if(!jarFile.exists()){
            GeronimoWsDeployContext deployContext =
                 new GeronimoWsDeployContext(
                     getTestFile("target/samples/echo.jar"),
                     outDir);
            Ws4J2ee ws4j2ee = new Ws4J2ee(deployContext, null);
                    ws4j2ee.generate();
        }
        File file = new File(tempDir);
        file.getParentFile().mkdirs();

    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
        jettyService.doStop();
        kernel.shutdown();
        File file = new File(tempDir);
        AxisGeronimoUtils.delete(file);
        file = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        AxisGeronimoUtils.delete(file);

    }

}
