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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import javax.management.ObjectName;

import org.apache.axis.utils.ClassUtils;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.config.ConfigurationManager;

public class SimplePOJOWebServiceTest extends AbstractWebServiceTest {

    public SimplePOJOWebServiceTest(String testName) throws FileNotFoundException, WaitingException, IOException {
        super(testName);
    }

    public void testPOJOWS() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[]{}, cl);
        File jarfile = new File(getTestFile("target/generated/samples/echo-war/echo-ewsimpl.jar"));
  
        //Start axis gbean        
        GBeanMBean axisgbean = new GBeanMBean(AxisGbean.getGBeanInfo(), myCl);
        kernel.loadGBean(axisname, axisgbean);
        kernel.startGBean(axisname);

        //build the configuration
        WSConfigBuilder wsconfBuilder = new WSConfigBuilder(getEARConfigBuilder(), store);
        List uri = wsconfBuilder.buildConfiguration(null, jarfile, outFile);
        //start the configuration
        for(int i = 0; i< uri.size();i++){
            GBeanMBean config = store.getConfiguration((URI) uri.get(i));
            ConfigurationManager configurationManager = kernel.getConfigurationManager();
            ObjectName configName = configurationManager.load(config, null);
            kernel.startRecursiveGBean(configName);
        }


        //let us try to brows the WSDL of the service
        URL wsdlrequestUrl = AxisGeronimoUtils.getURL("/axis/services/echoPort?wsdl");
        //+"/axis/services/AdminService?wsdl");
        
        HttpURLConnection connection = (HttpURLConnection) wsdlrequestUrl.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        connection.getResponseCode();
        String line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }
        Class echoLoacaterClass = ClassUtils.forName("org.apache.ws.echosample.EchoServiceLocator");
        Object echoLoacater = echoLoacaterClass.newInstance();
        Method getportMethod = echoLoacaterClass.getMethod("getechoPort", new Class[]{URL.class});
        URL serviceURL = AxisGeronimoUtils.getURL("/axis/services/echoPort");
        Object echoPort = getportMethod.invoke(echoLoacater, new Object[]{serviceURL});
        Class echoClass = echoPort.getClass();
        Method echoStringMethod = echoClass.getMethod("echoString", new Class[]{String.class});
        String val = "Hi";
        assertEquals(val, echoStringMethod.invoke(echoPort, new Object[]{val}));
        Class structClass = ClassUtils.forName("org.apache.ws.echosample.EchoStruct");
        Method echostuctMethod = echoClass.getMethod("echoStruct", new Class[]{structClass});
        assertNotNull(echostuctMethod);
        Object structval = structClass.newInstance();
        assertNotNull(structval);
        kernel.stopGBean(axisname);
        kernel.unloadGBean(axisname);
    }

}
