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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.apache.geronimo.echo package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _EchoBytes_QNAME = new QName("http://geronimo.apache.org/echo", "echoBytes");
    private final static QName _EchoImage_QNAME = new QName("http://geronimo.apache.org/echo", "echoImage");
    private final static QName _EchoBytesResponse_QNAME = new QName("http://geronimo.apache.org/echo", "echoBytesResponse");
    private final static QName _EchoImageResponse_QNAME = new QName("http://geronimo.apache.org/echo", "echoImageResponse");
    private final static QName _Hello_QNAME = new QName("http://geronimo.apache.org/echo", "hello");
    private final static QName _HelloResponse_QNAME = new QName("http://geronimo.apache.org/echo", "helloResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.apache.geronimo.echo
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EchoImageResponse }
     * 
     */
    public EchoImageResponse createEchoImageResponse() {
        return new EchoImageResponse();
    }

    /**
     * Create an instance of {@link EchoImage }
     * 
     */
    public EchoImage createEchoImage() {
        return new EchoImage();
    }

    /**
     * Create an instance of {@link EchoBytes }
     * 
     */
    public EchoBytes createEchoBytes() {
        return new EchoBytes();
    }

    /**
     * Create an instance of {@link EchoBytesResponse }
     * 
     */
    public EchoBytesResponse createEchoBytesResponse() {
        return new EchoBytesResponse();
    }

    /**
     * Create an instance of {@link HelloResponse }
     * 
     */
    public HelloResponse createHelloResponse() {
        return new HelloResponse();
    }

    /**
     * Create an instance of {@link Hello }
     * 
     */
    public Hello createHello() {
        return new Hello();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EchoBytes }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/echo", name = "echoBytes")
    public JAXBElement<EchoBytes> createEchoBytes(EchoBytes value) {
        return new JAXBElement<EchoBytes>(_EchoBytes_QNAME, EchoBytes.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EchoImage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/echo", name = "echoImage")
    public JAXBElement<EchoImage> createEchoImage(EchoImage value) {
        return new JAXBElement<EchoImage>(_EchoImage_QNAME, EchoImage.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EchoBytesResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/echo", name = "echoBytesResponse")
    public JAXBElement<EchoBytesResponse> createEchoBytesResponse(EchoBytesResponse value) {
        return new JAXBElement<EchoBytesResponse>(_EchoBytesResponse_QNAME, EchoBytesResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EchoImageResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/echo", name = "echoImageResponse")
    public JAXBElement<EchoImageResponse> createEchoImageResponse(EchoImageResponse value) {
        return new JAXBElement<EchoImageResponse>(_EchoImageResponse_QNAME, EchoImageResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Hello }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/echo", name = "hello")
    public JAXBElement<Hello> createHello(Hello value) {
        return new JAXBElement<Hello>(_Hello_QNAME, Hello.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HelloResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://geronimo.apache.org/echo", name = "helloResponse")
    public JAXBElement<HelloResponse> createHelloResponse(HelloResponse value) {
        return new JAXBElement<HelloResponse>(_HelloResponse_QNAME, HelloResponse.class, null, value);
    }

}
