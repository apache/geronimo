/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.jaxws.greeter_provider.common;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Service;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class MessageUtils {

    public static final String GERONIMO = "geronimo";

    public static final String RequestMessagePayload = "<p:sayHi xmlns:p=\"http://geronimo.apache.org/greeter_provider\">"
            + "<p:requestarg>" + GERONIMO + "</p:requestarg>" + "</p:sayHi>";

    public static final String ResponseMessagePayload = "<p:sayHiResponse xmlns:p=\"http://geronimo.apache.org/greeter_provider\">"
            + "<p:responsearg>" + GERONIMO + "</p:responsearg>" + "</p:sayHiResponse>";

    public static final String SOAP11RequestMessage = "<?xml version=\"1.0\"?><S:Envelope "
            + "xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "<S:Body>" + RequestMessagePayload
            + "</S:Body></S:Envelope>";

    public static final String SOAP11ResponseMessage = "<?xml version=\"1.0\"?><S:Envelope "
            + "xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "<S:Body>" + ResponseMessagePayload
            + "</S:Body></S:Envelope>";

    public static final String SOAP12RequestMessage = "<?xml version=\"1.0\"?><S:Envelope "
            + "xmlns:S=\"http://www.w3.org/2003/05/soap-envelope\">" + "<S:Body>" + RequestMessagePayload
            + "</S:Body></S:Envelope>";

    public static final String SOAP12ResponseMessage = "<?xml version=\"1.0\"?><S:Envelope "
            + "xmlns:S=\"http://www.w3.org/2003/05/soap-envelope\">" + "<S:Body>" + ResponseMessagePayload
            + "</S:Body></S:Envelope>";

    public static SOAPMessage createResponseSOAPMessage(String soapVersion) throws SOAPException {
        SOAPMessage responseSoapMessage = MessageFactory.newInstance(soapVersion).createMessage();
        if (soapVersion.equals(SOAPConstants.SOAP_1_1_PROTOCOL)) {
            responseSoapMessage.getSOAPPart().setContent(new StreamSource(new StringReader(SOAP11ResponseMessage)));
        } else if (soapVersion.equals(SOAPConstants.SOAP_1_2_PROTOCOL)) {
            responseSoapMessage.getSOAPPart().setContent(new StreamSource(new StringReader(SOAP12ResponseMessage)));
        } else
            throw new SOAPException("Unrecognized SOAP version [" + soapVersion + "]");
        responseSoapMessage.saveChanges();
        return responseSoapMessage;
    }

    public static SOAPMessage createRequestSOAPMessage(String soapVersion) throws SOAPException {
        SOAPMessage responseSoapMessage = MessageFactory.newInstance(soapVersion).createMessage();
        if (soapVersion.equals(SOAPConstants.SOAP_1_1_PROTOCOL)) {
            responseSoapMessage.getSOAPPart().setContent(new StreamSource(new StringReader(SOAP11RequestMessage)));
        } else if (soapVersion.equals(SOAPConstants.SOAP_1_2_PROTOCOL)) {
            responseSoapMessage.getSOAPPart().setContent(new StreamSource(new StringReader(SOAP12RequestMessage)));
        } else
            throw new SOAPException("Unrecognized SOAP version [" + soapVersion + "]");
        responseSoapMessage.saveChanges();
        return responseSoapMessage;
    }

    public static Source createResponseSOAPSource(String soapVersion, Service.Mode mode) throws SOAPException {
        if (mode.equals(Service.Mode.MESSAGE)) {
            if (soapVersion.equals(SOAPConstants.SOAP_1_1_PROTOCOL))
                return new StreamSource(new StringReader(SOAP11ResponseMessage));
            else if (soapVersion.equals(SOAPConstants.SOAP_1_2_PROTOCOL))
                return new StreamSource(new StringReader(SOAP12ResponseMessage));
        } else if (mode.equals(Service.Mode.PAYLOAD)) {
            return new StreamSource(new StringReader(ResponseMessagePayload));
        }
        throw new SOAPException("Unrecognized SOAP version [" + soapVersion + "] Service.Mode = [" + mode + "]");
    }

    public static Source createRequestSOAPSource(String soapVersion, Service.Mode mode) throws SOAPException {
        if (mode.equals(Service.Mode.MESSAGE)) {
            if (soapVersion.equals(SOAPConstants.SOAP_1_1_PROTOCOL)) {
                return new StreamSource(new StringReader(SOAP11RequestMessage));
            } else if (soapVersion.equals(SOAPConstants.SOAP_1_2_PROTOCOL)) {
                return new StreamSource(new StringReader(SOAP12RequestMessage));
            }
        } else if (mode.equals(Service.Mode.PAYLOAD)) {
            return new StreamSource(new StringReader(RequestMessagePayload));
        }
        throw new SOAPException("Unrecognized SOAP version [" + soapVersion + "] Service.Mode = [" + mode + "]");
    }

    public static Source createRequestHTTPSource() {     
        return new StreamSource(new StringReader(RequestMessagePayload));
    }

    public static Source createResponseHTTPSource() {
        return new StreamSource(new StringReader(ResponseMessagePayload));
    }

    public static DataSource createRequestHTTPDataSource() throws IOException {
        return new FileDataSource(new File(MessageUtils.class.getResource("/geronimo.txt").getFile()));        
    }

    public static DataSource createResponseHTTPDataSource() throws IOException {
        return new FileDataSource(new File(MessageUtils.class.getResource("/geronimo.txt").getFile()));
    }

    public static String getSubTextChildValue(Node parentNode) {
        NodeList childrenList = parentNode.getChildNodes();
        if (childrenList.getLength() == 0)
            return null;
        for (int i = 0; i < childrenList.getLength(); i++) {
            Node node = childrenList.item(i);
            if (node.getNodeType() == Node.TEXT_NODE)
                return ((Text) node).getNodeValue();
        }
        return null;
    }

    public static Node findNode(Node rootNode, String nodeName) {
        if (rootNode.getLocalName().equalsIgnoreCase(nodeName))
            return rootNode;
        NodeList childrenList = rootNode.getChildNodes();
        if (childrenList.getLength() > 0) {
            for (int i = 0; i < childrenList.getLength(); i++) {
                Node node = childrenList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Node findElement = findNode(node, nodeName);
                    if (findElement != null)
                        return findElement;
                }
            }
        }
        return null;
    }
}
