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
package org.apache.geronimo.test;

import java.io.StringWriter;
import java.io.StringReader;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPConstants;

/**
 * Performs basic tests using the SAAJ 1.3 API. 
 */
public class SAAJTest {

    MessageFactory messageFactory = null;
    SOAPConnectionFactory soapConnectionFactory = null;
    SOAPFactory soapFactory = null;

    public SAAJTest() throws Exception {
        this.messageFactory = MessageFactory.newInstance();
        this.soapConnectionFactory = SOAPConnectionFactory.newInstance();
        this.soapFactory = SOAPFactory.newInstance();
    }

    public String getMessageFactoryImplementation() {
        return this.messageFactory.getClass().getName();
    }

    public String getSOAPFactoryImplementation() {
        return this.soapFactory.getClass().getName();
    }

    public String getSOAPConnectionFactoryImplementation() {
        return this.soapConnectionFactory.getClass().getName();
    }

    public void test() throws Exception {
        // this will test SAAJMetaFactory (and 1.3 SAAJ API)
        System.out.println(SOAPFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL));
        System.out.println(SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL));
        System.out.println(SOAPFactory.newInstance(SOAPConstants.DYNAMIC_SOAP_PROTOCOL));
        
        System.out.println(MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL));
        System.out.println(MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL));
        System.out.println(MessageFactory.newInstance(SOAPConstants.DYNAMIC_SOAP_PROTOCOL));

        System.out.println(this.soapConnectionFactory.createConnection().getClass().getName());
        System.out.println(this.messageFactory.createMessage().getClass().getName());
        System.out.println(this.soapFactory.createName("foo").getClass().getName());
    }

}
