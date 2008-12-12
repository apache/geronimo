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

package org.apache.geronimo.jaxws.greeter_provider;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.activation.DataSource;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.Service;

import junit.framework.Assert;

import org.apache.geronimo.jaxws.greeter_provider.common.MessageUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class AssertUtils {

    public static void assertResponseSOAPMessage(SOAPMessage soapMessage, String soapVersion) throws SOAPException {
        Assert.assertNotNull(soapMessage);
        assertResponseNode(soapMessage.getSOAPBody());
        assertSOAPVersion(soapMessage.getSOAPBody(), soapVersion);
    }

    public static void assertResponseSOAPSource(Source source, String soapVersion, Service.Mode mode) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMResult result = new DOMResult();
        transformer.transform(source, result);
        Node node = ((Document) result.getNode()).getDocumentElement();
        assertResponseNode(node);
        if (mode.equals(Service.Mode.MESSAGE))
            assertSOAPVersion(node, soapVersion);
    }

    public static void assertResponseHTTPSource(Source source) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMResult result = new DOMResult();
        transformer.transform(source, result);
        Node node = ((Document) result.getNode()).getDocumentElement();
        assertResponseNode(node);
    }

    public static void assertResponseNode(Node node) {
        Node requestArg = MessageUtils.findNode(node, "responsearg");
        Assert.assertNotNull(requestArg);
        String sResponseText = MessageUtils.getSubTextChildValue(requestArg);
        Assert.assertEquals(MessageUtils.GERONIMO, sResponseText);
    }

    public static void assertSOAPVersion(Node node, String soapVersion) {
        String sNameSpaceURI = node.getNamespaceURI();
        if (soapVersion.equals(SOAPConstants.SOAP_1_1_PROTOCOL))
            Assert.assertEquals(sNameSpaceURI, "http://schemas.xmlsoap.org/soap/envelope/");
        else if (soapVersion.equals(SOAPConstants.SOAP_1_2_PROTOCOL))
            Assert.assertEquals(sNameSpaceURI, "http://www.w3.org/2003/05/soap-envelope");
    }

    public static void assertResponseHTTPDataSource(DataSource dataSource) throws Exception {
        DataSource requestDataSource = MessageUtils.createRequestHTTPDataSource();
        Assert.assertEquals(requestDataSource.getContentType(), dataSource.getContentType());
        byte[] expectedBytes = loadBytesFromDataSource(requestDataSource);
        byte[] actualBytes = loadBytesFromDataSource(dataSource);
        Assert.assertEquals(expectedBytes.length, actualBytes.length);
        for (int i = 0; i < expectedBytes.length; i++)
            Assert.assertEquals("Check byte array [" + i + "]", expectedBytes[i], actualBytes[i]);
    }

    public static byte[] loadBytesFromDataSource(DataSource dataSource) throws Exception {
        ByteArrayOutputStream out = null;
        InputStream in = null;
        try {
            in = dataSource.getInputStream();
            out = new ByteArrayOutputStream();
            byte[] bytesBuffer = new byte[512];
            int iCurrentReadBytes = 0;
            while ((iCurrentReadBytes = in.read(bytesBuffer)) != -1)
                out.write(bytesBuffer, 0, iCurrentReadBytes);
            return out.toByteArray();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (Exception e) {
                }
        }
    }
}
