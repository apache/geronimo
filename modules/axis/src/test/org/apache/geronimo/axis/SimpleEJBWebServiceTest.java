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

import org.apache.geronimo.axis.testUtils.AxisGeronimoConstants;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;

import javax.management.ObjectName;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class SimpleEJBWebServiceTest extends AbstractWebServiceTest {

    public SimpleEJBWebServiceTest(String testName) throws FileNotFoundException, WaitingException, IOException {
        super(testName);
    }

    public void testLoad() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[]{}, cl);

  
        //axis gbean        
        GBeanMBean axisgbean = new GBeanMBean(AxisGbean.getGBeanInfo(), myCl);
        kernel.loadGBean(axisname, axisgbean);
        kernel.startGBean(axisname);
        File jarfile = new File(getTestFile("target/generated/samples/echo-jar/echo-ewsimpl.jar"));
        WSConfigBuilder wsconfBuilder = new WSConfigBuilder(getEARConfigBuilder(), store);
        File out = new File("target/temp");
        out.mkdirs();
        File ws = wsconfBuilder.installWebService(jarfile, out, Thread.currentThread().getContextClassLoader());
        GBeanMBean[] gbeans = wsconfBuilder.loadtheWSConfigurations(ws, jarfile, cl);
        ObjectName wsName = ObjectName.getInstance("test:configuration=" + "echo");
        ObjectName wsEJBName = ObjectName.getInstance("test:configuration=" + "echoEJB");
        AxisGeronimoUtils.startGBean(wsName, gbeans[0], kernel);
        AxisGeronimoUtils.startGBean(wsEJBName, gbeans[1], kernel);


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


        //invoke the ejb just in the same way it is invoked by the webservice 
        String msg = "Hi Hello";
        String result =
                (String) AxisGeronimoUtils.invokeEJB("echo",
                        "echoString",
                        new Class[]{String.class},
                        new Object[]{msg});
        System.out.println(result);
        assertEquals(msg, result);
        AxisGeronimoUtils.invokeEJB("echo", "echoVoid", new Class[]{
        }, new Object[]{
        });
        int valInt = 2345;
        Integer intObj =
                (Integer) AxisGeronimoUtils.invokeEJB("echo",
                        "echoInt",
                        null,
                        new Object[]{new Integer(valInt)});
        assertEquals(valInt, intObj.intValue());
        double valDouble = 2425.57;
        Double doubleObj =
                (Double) AxisGeronimoUtils.invokeEJB("echo",
                        "echoDouble",
                        null,
                        new Object[]{new Double(valDouble)});
        assertEquals(valDouble, doubleObj.doubleValue(), 3);
        float valfloat = 2425.57f;
        Float floatObj =
                (Float) AxisGeronimoUtils.invokeEJB("echo",
                        "echoFloat",
                        null,
                        new Object[]{new Float(valfloat)});
        assertEquals(valfloat, floatObj.doubleValue(), 3);
        boolean valBoolean = true;
        Boolean booleanObj =
                (Boolean) AxisGeronimoUtils.invokeEJB("echo",
                        "echoBoolean",
                        null,
                        new Object[]{new Boolean(valBoolean)});
        assertEquals(valBoolean, booleanObj.booleanValue());
        long valLong = 2425573566l;
        Long longObj =
                (Long) AxisGeronimoUtils.invokeEJB("echo",
                        "echoLong",
                        null,
                        new Object[]{new Long(valLong)});
        assertEquals(valLong, longObj.longValue());
        short valShort = 242;
        Short shortObj =
                (Short) AxisGeronimoUtils.invokeEJB("echo",
                        "echoShort",
                        null,
                        new Object[]{new Short(valShort)});
        assertEquals(valShort, shortObj.shortValue());
        byte[] byteVal = "Hi Hello".getBytes();
        byte[] byteValreturn =
                (byte[]) AxisGeronimoUtils.invokeEJB("echo",
                        "echoBytes",
                        new Class[]{byte[].class},
                        new Object[]{byteVal});
        assertTrue(Arrays.equals(byteVal, byteValreturn));




//        
//        //check the real web service invocations 
        ClassLoader ocl = Thread.currentThread().getContextClassLoader();
        URLClassLoader jarclassloder = new URLClassLoader(new URL[]{jarfile.toURL()});
        Thread.currentThread().setContextClassLoader(jarclassloder);
        Class echoLoacaterClass = Class.forName("org.apache.ws.echosample.EchoServiceLocator", true, jarclassloder);
        Object echoLoacater = echoLoacaterClass.newInstance();
        Method getportMethod = echoLoacaterClass.getMethod("getechoPort", new Class[]{URL.class});
        URL serviceURL = AxisGeronimoUtils.getURL("/axis/services/echoPort");
        Object echoPort = getportMethod.invoke(echoLoacater, new Object[]{serviceURL});
        Class echoClass = echoPort.getClass();
        Method echoStringMethod = echoClass.getMethod("echoString", new Class[]{String.class});
        String val = "Hi";
        assertEquals(val, echoStringMethod.invoke(echoPort, new Object[]{val}));
        Class structClass = Class.forName("org.apache.ws.echosample.EchoStruct", true, jarclassloder);
        Method echostuctMethod = echoClass.getMethod("echoStruct", new Class[]{structClass});
        assertNotNull(echostuctMethod);
        Object structval = structClass.newInstance();
        assertNotNull(structval);
        Thread.currentThread().setContextClassLoader(ocl);
        kernel.stopGBean(axisname);
        kernel.unloadGBean(axisname);
    }

    protected void tearDown() throws Exception {
        j2eeManager.stopJ2EEContainer(kernel);
        kernel.shutdown();
        File file = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        AxisGeronimoUtils.delete(file);
    }

}
