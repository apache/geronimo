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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javax.management.ObjectName;

import org.apache.axis.utils.ClassUtils;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

public class ComplexTypeWebServiceTest extends AbstractTestCase {
    private ObjectName axisname;
    private ObjectName deployGbeanName;
    private Kernel kernel;
    private JettyServiceWrapper jettyService;

    /**
     * @param testName
     */
    public ComplexTypeWebServiceTest(String testName) {
        super(testName);
    }

    public void testLoad() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
		ClassLoader myCl =   new URLClassLoader(new URL[]{}, cl);
  
        //axis gbean        
        GBeanMBean axisgbean = new GBeanMBean(AxisGbean.getGBeanInfo(), myCl);
        kernel.loadGBean(axisname, axisgbean);
        kernel.startGBean(axisname);

        GBeanMBean deploygbean =
          new GBeanMBean(WebServiceDeployerGbean.getGBeanInfo(), cl);
        kernel.loadGBean(deployGbeanName, deploygbean);
        kernel.startGBean(deployGbeanName);
        System.out.println(
            kernel.getMBeanServer().getAttribute(deployGbeanName, "state"));
        File jarfile = new File(getTestFile("target/generated/samples/echo-ewsimpl.jar"));    
        kernel.getMBeanServer().invoke(
            deployGbeanName,
            "deployEWSModule",
            new Object[] {
                jarfile.getAbsolutePath(),
                null,
                "ws/apache/axis/echo" },
            new String[] {
                String.class.getName(),
                String.class.getName(),
                String.class.getName()});
        kernel.stopGBean(deployGbeanName);
        kernel.unloadGBean(deployGbeanName);

//      //try invoke from this java
//          ContainerIndex index = ContainerIndex.getInstance();
//          int length = index.length();
//          System.out.println("number of continers "+length);
//          for(int i = 0;i<length;i++){
//              EJBContainer contianer = index.getContainer(i);
//              if(contianer!= null){
//                  String name = contianer.getEJBName();
//                  System.out.println("found the ejb "+name);
//                  if("echo".equals(name)){
//                      EJBHome statelessHome = contianer.getEJBHome();
//                      Object stateless = statelessHome.getClass().getMethod("create", null).invoke(statelessHome, null);
//                      Method[] methods = stateless.getClass().getMethods();
//                        
//                      for(int j = 0;j< methods.length;j++){
//                          if(methods[j].getName().equals("echoStruct")){
//                                  Class[] classes = methods[j].getParameterTypes();
//                                  System.out.println(classes[0]);
//                                  methods[j].invoke(stateless, new Object[]{null});
//                                  methods[j].invoke(stateless, new Object[]{classes[0].newInstance()});
//                          }
//                      }
//                  }
//              }
//          }                                                    

        //check the real web service invocations 
//        ClassLoader ocl = Thread.currentThread().getContextClassLoader();
//        URLClassLoader jarclassloder = new URLClassLoader(new URL[]{jarfile.toURL()});
//        Thread.currentThread().setContextClassLoader(jarclassloder);
        
//        Class echoLoacaterClass =  Class.forName("org.apache.ws.echosample.EchoServiceLocator",true,jarclassloder);
//        Class structClass = Class.forName("org.apache.ws.echosample.EchoStruct",true,jarclassloder);
          Class echoLoacaterClass =  ClassUtils.forName("org.apache.ws.echosample.EchoServiceLocator");
          Class structClass = ClassUtils.forName("org.apache.ws.echosample.EchoStruct");


        Object echoLoacater = echoLoacaterClass.newInstance();
        Method getportMethod = echoLoacaterClass.getMethod("getechoPort",new Class[]{URL.class});
        
        URL serviceURL = new URL("http://localhost:"
                +AxisGeronimoConstants.AXIS_SERVICE_PORT
               // + 5679
                +"/axis/services/echoPort");
        Object echoPort = getportMethod.invoke(echoLoacater,new Object[]{serviceURL});        
        Class echoClass = echoPort.getClass();
        
        Method echostuctMethod = echoClass.getMethod("echoStruct",new Class[]{structClass});
        Object structval = structClass.newInstance();
        
        Object structret = echostuctMethod.invoke(echoPort,new Object[]{null});
        structret = echostuctMethod.invoke(echoPort,new Object[]{structval});
        assertEquals(structval,structret);
        //Thread.currentThread().setContextClassLoader(ocl); 
         
        kernel.stopGBean(axisname);
        kernel.unloadGBean(axisname);
    }

    protected void setUp() throws Exception {
		File file = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        axisname = new ObjectName("test:name=AxisGBean");
        deployGbeanName = new ObjectName("test:name=WebServiceDeployerGbean");
        kernel = new Kernel("test.kernel", "test");
        kernel.boot();
        jettyService = new JettyServiceWrapper(kernel);
        jettyService.doStart();
        AxisGeronimoUtils.delete(file);
        file.getParentFile().mkdirs();
    }

    protected void tearDown() throws Exception {
        jettyService.doStop();
        kernel.shutdown();
        File file = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        AxisGeronimoUtils.delete(file);
    }

}
