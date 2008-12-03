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


package org.apache.geronimo.echo;

import java.awt.Image;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(name = "Echo", targetNamespace = "http://geronimo.apache.org/echo")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface Echo {


    /**
     * 
     * @param arg0
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "hello", targetNamespace = "http://geronimo.apache.org/echo", className = "org.apache.geronimo.echo.Hello")
    @ResponseWrapper(localName = "helloResponse", targetNamespace = "http://geronimo.apache.org/echo", className = "org.apache.geronimo.echo.HelloResponse")
    public String hello(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0);

    /**
     * 
     * @param bytes
     * @param useMTOM
     * @return
     *     returns byte[]
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "echoBytes", targetNamespace = "http://geronimo.apache.org/echo", className = "org.apache.geronimo.echo.EchoBytes")
    @ResponseWrapper(localName = "echoBytesResponse", targetNamespace = "http://geronimo.apache.org/echo", className = "org.apache.geronimo.echo.EchoBytesResponse")
    public byte[] echoBytes(
        @WebParam(name = "useMTOM", targetNamespace = "")
        boolean useMTOM,
        @WebParam(name = "bytes", targetNamespace = "")
        byte[] bytes);

    /**
     * 
     * @param imageBytes
     * @param useMTOM
     * @return
     *     returns java.awt.Image
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "echoImage", targetNamespace = "http://geronimo.apache.org/echo", className = "org.apache.geronimo.echo.EchoImage")
    @ResponseWrapper(localName = "echoImageResponse", targetNamespace = "http://geronimo.apache.org/echo", className = "org.apache.geronimo.echo.EchoImageResponse")
    public Image echoImage(
        @WebParam(name = "useMTOM", targetNamespace = "")
        boolean useMTOM,
        @WebParam(name = "imageBytes", targetNamespace = "")
        Image imageBytes);

}
