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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javax.management.ObjectName;

import org.apache.axis.utils.ClassUtils;
import org.apache.geronimo.axis.testUtils.AxisGeronimoConstants;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

public class ComplexTypeWebServiceTest extends AbstractWebServiceTest {
    private ObjectName axisname;
    private Kernel kernel;

    /**
     * @param testName
     * @throws IOException
     * @throws WaitingException
     * @throws FileNotFoundException
     */
    public ComplexTypeWebServiceTest(String testName) throws FileNotFoundException, WaitingException, IOException {
        super(testName);
    }

    public void testLoad() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[]{}, cl);
  
        //axis gbean        
        GBeanMBean axisgbean = new GBeanMBean(AxisGbean.getGBeanInfo(), myCl);
        kernel.loadGBean(axisname, axisgbean);
        kernel.startGBean(axisname);
        File jarfile = new File(getTestFile("target/generated/samples/echo-ewsimpl.jar"));
        
        kernel.getMBeanServer().invoke(axisname,
                "deployEWSModule",
                new Object[]{
                    jarfile.getAbsolutePath(),
                    null,
                    "ws/apache/axis/echo"},
                new String[]{
                    String.class.getName(),
                    String.class.getName(),
                    String.class.getName()});

        //check the real web service invocations 
        Class echoLoacaterClass = ClassUtils.forName("org.apache.ws.echosample.EchoServiceLocator");
        Class structClass = ClassUtils.forName("org.apache.ws.echosample.EchoStruct");
        Object echoLoacater = echoLoacaterClass.newInstance();
        Method getportMethod = echoLoacaterClass.getMethod("getechoPort", new Class[]{URL.class});
        URL serviceURL = AxisGeronimoUtils.getURL("/axis/services/echoPort");
        Object echoPort = getportMethod.invoke(echoLoacater, new Object[]{serviceURL});
        Class echoClass = echoPort.getClass();
        Method echostuctMethod = echoClass.getMethod("echoStruct", new Class[]{structClass});
        Object structval = structClass.newInstance();
        
        Object structret = echostuctMethod.invoke(echoPort, new Object[]{null});
        structret = echostuctMethod.invoke(echoPort, new Object[]{structval});
        
        assertEquals(structval, structret);
        //Thread.currentThread().setContextClassLoader(ocl); 
         
        kernel.stopGBean(axisname);
        kernel.unloadGBean(axisname);
    }

    protected void setUp() throws Exception {
        File file = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        axisname = new ObjectName("test:name=AxisGBean");
        kernel = new Kernel("test.kernel", "test");
        kernel.boot();
        AxisGeronimoUtils.delete(file);
        file.getParentFile().mkdirs();
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        File file = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        AxisGeronimoUtils.delete(file);
    }

}
