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

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

import javax.management.ObjectName;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class WebServiceTest extends AbstractTestCase {
    private ObjectName axisname;
    private Kernel kernel;

    /**
     * @param testName
     */
    public WebServiceTest(String testName) {
        super(testName);
    }

    public void testLoad() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[]{}, cl);

//      This code trying to lad the tools.jar and set it to the Context ClassLoader.
//      It does not seem to help.   
//		URL toolsURL = null;
//        String tools = System.getProperty("java.home");
//        assertNotNull(tools);		
//        //File f = new File(tools,"./../lib/tools.jar");
//        File f = new File("H:/j2sdk1.4.1_01/lib/","tools.jar");
//	    System.out.println("tools.jar is "+f.getAbsolutePath());
//	    if (f.exists()) {
//		    toolsURL = f.toURL();
//		    myCl =  new URLClassLoader(new URL[]{toolsURL}, cl);
//	    }
//        Thread.currentThread().setContextClassLoader(myCl);
//		
//        GBeanMBean deploygbean =
//            new GBeanMBean(WebServiceDeployerGbean.getGBeanInfo(), myCl);
  
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

        //let us try to brows the WSDL of the service
        URL wsdlrequestUrl = new URL("http://localhost:"
                + AxisGeronimoConstants.AXIS_SERVICE_PORT
                + "/axis/services/echoPort?wsdl");
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

        URL serviceURL = new URL("http://localhost:"
                + AxisGeronimoConstants.AXIS_SERVICE_PORT
                // + 5679
                + "/axis/services/echoPort");
        Object echoPort = getportMethod.invoke(echoLoacater, new Object[]{serviceURL});
        Class echoClass = echoPort.getClass();

        Method echoStringMethod = echoClass.getMethod("echoString", new Class[]{String.class});
        String val = "Hi";
        assertEquals(val, echoStringMethod.invoke(echoPort, new Object[]{val}));

        Class structClass = Class.forName("org.apache.ws.echosample.EchoStruct", true, jarclassloder);
        Method echostuctMethod = echoClass.getMethod("echoStruct", new Class[]{structClass});
        Object structval = structClass.newInstance();
        
//        //try invoke from this class
//        ContainerIndex index = ContainerIndex.getInstance();
//        int length = index.length();
//        System.out.println("number of continers "+length);
//        for(int i = 0;i<length;i++){
//            EJBContainer contianer = index.getContainer(i);
//            if(contianer!= null){
//                String name = contianer.getEJBName();
//                System.out.println("found the ejb "+name);
//                if("echo".equals(name)){
//                    EJBHome statelessHome = contianer.getEJBHome();
//                    Object stateless = statelessHome.getClass().getMethod("create", null).invoke(statelessHome, null);
//                    Method[] methods = stateless.getClass().getMethods();
//                    
//                    for(int j = 0;j< methods.length;j++){
//                        if(methods[j].getName().equals("echoStruct")){
//                                Class[] classes = methods[j].getParameterTypes();
//                                System.out.println(classes[0]);
//                                methods[j].invoke(stateless, new Object[]{null});
//                                methods[j].invoke(stateless, new Object[]{classes[0].newInstance()});
//                        }
//                    }
//                }
//            }
//        }                                                    
        Thread.currentThread().setContextClassLoader(ocl);

        kernel.stopGBean(axisname);
        kernel.unloadGBean(axisname);

    }

    protected void setUp() throws Exception {
        File file = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        axisname = new ObjectName("test:name=AxisGBean");
        kernel = new Kernel("test.kernel", "test");
        kernel.boot();
//        jettyService = new JettyServiceWrapper(kernel);
//        jettyService.doStart();
        AxisGeronimoUtils.delete(file);
        file.getParentFile().mkdirs();
    }

    protected void tearDown() throws Exception {
//        jettyService.doStop();
        kernel.shutdown();
        File file = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        AxisGeronimoUtils.delete(file);
    }

}
