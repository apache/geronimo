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

import javax.xml.ws.Service;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;

@WebServiceRefs ({
                     @WebServiceRef(name = "WebServiceRef10",
                                    type = Service.class,
                                    value = Service.class,
                                    wsdlLocation = "WEB-INF/wsdl/WebServiceRef10.wsdl"),
                     @WebServiceRef(name = "WebServiceRef11",
                                    type = Service.class,
                                    value = Service.class,
                                    wsdlLocation = "WEB-INF/wsdl/WebServiceRef11.wsdl",
                                    mappedName = "mappedName11")
                 })
public class WebServiceRefAnnotationExample {

    @WebServiceRef(name = "WebServiceRef12",
                   type = Service.class,
                   value = Service.class,
                   mappedName = "mappedName12")
    Service annotatedField1;

    @WebServiceRef
    Service annotatedField2;

    //------------------------------------------------------------------------------------------
    // Method name (for setter-based injection) must follow JavaBeans conventions:
    // -- Must start with "set"
    // -- Have one parameter
    // -- Return void
    //------------------------------------------------------------------------------------------
    @WebServiceRef(name = "WebServiceRef14",
                   value = MyService.class,
                   wsdlLocation = "WEB-INF/wsdl/WebServiceRef14.wsdl",
                   mappedName = "mappedName14")
    public void setAnnotatedMethod1(MyService bool) {
    }

    @WebServiceRef(name = "WebServiceRef15",
                   value = Service.class,
                   wsdlLocation = "WEB-INF/wsdl/WebServiceRef15.wsdl",
                   mappedName = "mappedName15")
    public void setAnnotatedMethod2(Service string) {
    }

    public static class MyService extends Service {
        public MyService() {
            super(null, null);
        }
    }

}
