/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.axis2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.apache.geronimo.webservices.WebServiceContainer.Request;

public class Axis2WebServiceContainerTest extends Axis2AbstractTestCase {
    public Axis2WebServiceContainerTest(String testName) {
        super(testName);
    }

    public void testInvokeWithWSDLDocLit() throws Exception {
        testInvokeWithWSDL("test_service_doc_lit_request.xml", "test_service_doc_lit.wsdl");
    }
    
    //TODO:
    public void testInvokeWithWSDLRPCLit() throws Exception {
    }
    
    private void testInvokeWithWSDL(String requestFile, String wsdlFile) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream in = cl.getResourceAsStream(requestFile);

        PortInfo portInfo = new PortInfo();
        portInfo.setLocation("servlet");
        File file = new File(getTestFile("src/test/resources/"+wsdlFile));
        portInfo.setWsdlDefinition(readWSDL(file.toURL().toString()));
        
        try {
            Axis2Request req = new Axis2Request(504,
                    "text/xml; charset=utf-8",
                    in,
                    Request.POST,
                    new HashMap(),
                    new URI("/axis2/servlet"),
                    new HashMap(),
                    "127.0.0.1");
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Axis2Response res = new Axis2Response("text/xml; charset=utf-8", "127.0.0.1", null, null, 8080, out);
            
            String endpointClassName = "org.apache.geronimo.axis2.testdata.HelloWorld";
            Axis2WebServiceContainer container = new Axis2WebServiceContainer(portInfo, endpointClassName, cl , null, null);
            container.invoke(req, res);
            out.flush();
     
        } catch(Throwable ex){    
        	ex.printStackTrace();
            throw new Exception(ex.toString());
        }finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                    // ignore
                }
            }
        }
    }
    
    private Definition readWSDL(String url) throws Exception{
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.importDocuments", true);
        reader.setFeature("javax.wsdl.verbose", false);
        Definition wsdlDefinition = reader.readWSDL(url);
        return wsdlDefinition;
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
}

