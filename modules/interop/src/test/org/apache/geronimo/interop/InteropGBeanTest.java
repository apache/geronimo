/**
 *
 * Copyright 2004-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop;

import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;


/**
 * @version $Rev: $ $Date: $
 */
public class InteropGBeanTest extends TestCase {

    private Kernel kernel;
    private ObjectName objName;
    private static final String KERNEL_NAME = "testKernel";

    public void testFoo() throws Exception {
        log("testFoo():");
    }

    public void testProperties() throws Exception {
        try {
            log("testProperties():");

            GBeanInfo gbi = InteropGBean.getGBeanInfo();
            log("testProperties(): gbi = " + gbi);

            GBeanMBean cmf = new GBeanMBean(gbi);
            log("testProperties(): cmf = " + cmf);

            /*
            cmf.setAttribute("strprop", "strvalue");
            log( "testProperties(): strprop set" );

            objName = ObjectName.getInstance("interop: name=default");
            log( "testProperties(): objName = " + objName );

            kernel.loadGBean(objName, cmf);
            kernel.startGBean(objName);

            Object proxy = kernel.invoke(objName, "$getResource");

            log( "proxy = " + proxy );

            kernel.stopGBean(objName);
            */
        } catch (Error er) {
            // JUnit did not print out the stack trace for this messsage.
            log("er = " + er);
            er.printStackTrace(System.out);
            throw new Exception(er.toString());
        } catch (Exception ex) {
            // JUnit did not print out the stack trace for this messsage.
            log("ex = " + ex);
            ex.printStackTrace(System.out);
            throw new Exception(ex.toString());
        }
    }

    /*
     * Hum, testError and testException will result in a test failure, but
     * no information about which test or why it failed is sent to the console
     * even with maven -X.
     */
    public void testError() {
        log("testError()");
        //throw new java.lang.ExceptionInInitializerError( "Error: Unable to find constructor" );
    }

    public void testException()
            throws Exception {
        log("testException()");
        //throw new java.lang.Exception( "Exception: Something went wrong." );
    }

    protected void setUp() throws Exception {
        log("setUp():");
        kernel = new Kernel(KERNEL_NAME);

        log("setUp(): kernel = " + kernel);

        kernel.boot();
    }

    protected void tearDown() throws Exception {
        log("tearDown():");
        kernel.shutdown();
    }

    protected void log(String msg) {
        System.out.println(msg);
    }
}
