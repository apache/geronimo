/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.geronimo.calculator;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

@WebService(name = "Calculator", targetNamespace = "http://geronimo.apache.org/calculator")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface Calculator {


    /**
     * 
     * @param arg1
     * @param arg0
     * @return
     *     returns int
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "add", targetNamespace = "http://geronimo.apache.org/calculator", className = "org.apache.geronimo.calculator.Add")
    @ResponseWrapper(localName = "addResponse", targetNamespace = "http://geronimo.apache.org/calculator", className = "org.apache.geronimo.calculator.AddResponse")
    public int add(
        @WebParam(name = "arg0", targetNamespace = "")
        int arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        int arg1);

    /**
     * 
     * @return
     *     returns javax.xml.ws.wsaddressing.W3CEndpointReference
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getEPR", targetNamespace = "http://geronimo.apache.org/calculator", className = "org.apache.geronimo.calculator.GetEPR")
    @ResponseWrapper(localName = "getEPRResponse", targetNamespace = "http://geronimo.apache.org/calculator", className = "org.apache.geronimo.calculator.GetEPRResponse")
    public W3CEndpointReference getEPR();

    /**
     * 
     * @param arg1
     * @param arg0
     * @return
     *     returns int
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "multiply", targetNamespace = "http://geronimo.apache.org/calculator", className = "org.apache.geronimo.calculator.Multiply")
    @ResponseWrapper(localName = "multiplyResponse", targetNamespace = "http://geronimo.apache.org/calculator", className = "org.apache.geronimo.calculator.MultiplyResponse")
    public int multiply(
        @WebParam(name = "arg0", targetNamespace = "")
        int arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        int arg1);

}
