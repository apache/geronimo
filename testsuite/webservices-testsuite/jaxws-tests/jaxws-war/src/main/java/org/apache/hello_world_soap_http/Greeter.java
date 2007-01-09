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

package org.apache.hello_world_soap_http;

import javax.jws.WebParam.Mode;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding;
import java.util.concurrent.Future;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.Response;
import javax.xml.ws.ResponseWrapper;

@WebService(targetNamespace = "http://apache.org/hello_world_soap_http", name = "Greeter")

public interface Greeter {
    @ResponseWrapper(targetNamespace = "http://apache.org/hello_world_soap_http/types", className = "org.apache.hello_world_soap_http.types.GreetMeResponse", localName = "greetMeResponse")
    @RequestWrapper(targetNamespace = "http://apache.org/hello_world_soap_http/types", className = "org.apache.hello_world_soap_http.types.GreetMe", localName = "greetMe")
    @WebResult(targetNamespace = "http://apache.org/hello_world_soap_http/types", name = "responseType")
    @WebMethod(operationName = "greetMe")
    public java.lang.String greetMe(
        @WebParam(targetNamespace = "http://apache.org/hello_world_soap_http/types", name = "requestType")
        java.lang.String requestType
    );
}
