/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.j2ee.deployment.annotation;

import javax.jws.HandlerChain;
import javax.xml.ws.WebServiceRef;

@HandlerChain(file = "annotation/handlers1.xml")            // Ignored by Geronimo at the class-level
public class HandlerChainAnnotationExample {

    @WebServiceRef(name = "WebServiceRef1",
                   value = javax.xml.ws.Service.class,
                   wsdlLocation = "WEB-INF/wsdl/WebServiceRef1.wsdl",
                   mappedName = "mappedName")
    @HandlerChain(file = "annotation/handlers2.xml")
    int annotatedField1;

    @WebServiceRef(name = "WebServiceRef2",
                   value = javax.xml.ws.Service.class,
                   wsdlLocation = "WEB-INF/wsdl/WebServiceRef2.wsdl",
                   mappedName = "mappedName")
    @HandlerChain(file = "annotation/handlers3.xml")
    boolean annotatedField2;

    //------------------------------------------------------------------------------------------
    // Method name (for setter-based injection) must follow JavaBeans conventions:
    // -- Must start with "set"
    // -- Have one parameter
    // -- Return void
    //------------------------------------------------------------------------------------------
    @WebServiceRef(name = "WebServiceRef3",
                   value = javax.xml.ws.Service.class,
                   wsdlLocation = "WEB-INF/wsdl/WebServiceRef3.wsdl",
                   mappedName = "mappedName")
    @HandlerChain(file = "annotation/handlers4.xml")
    public void setAnnotatedMethod1(String string) {
    }

    @WebServiceRef(name = "WebServiceRef4",
                   value = javax.xml.ws.Service.class,
                   wsdlLocation = "WEB-INF/wsdl/WebServiceRef4.wsdl",
                   mappedName = "mappedName")
    @HandlerChain(file = "annotation/handlers5.xml")
    public void setAnnotatedMethod2(int ii) {
    }
    
    @WebServiceRef(name = "WebServiceRef100",
            value = javax.xml.ws.Service.class,
            wsdlLocation = "WEB-INF/wsdl/WebServiceRef4.wsdl",
            mappedName = "mappedName")
    @HandlerChain(file = "annotation/handlers5.xml")
    public void setAnnotatedMethod3(int ii) {
    }
    
}
