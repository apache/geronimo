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

package org.apache.calculator;

import java.io.ByteArrayInputStream;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@WebServiceProvider
@BindingType(value=HTTPBinding.HTTP_BINDING)
public class CalculatorImpl implements Provider<Source> {

    @Resource
    protected WebServiceContext wsContext;

    public Source invoke(Source source) {
        try {
            String num1 = null;
            String num2 = null;

            if (source == null) {
                System.out.println("Getting input from query string");
                MessageContext mc = wsContext.getMessageContext();
                String query = (String)mc.get(MessageContext.QUERY_STRING);
                System.out.println("Query String = " + query);
                ServletRequest req = (ServletRequest)mc.get(MessageContext.SERVLET_REQUEST);
                num1 = req.getParameter("num1");
                num2 = req.getParameter("num2");
            } else {
                System.out.println("Getting input from input message");
                Node n = null;
                if (source instanceof DOMSource) {
                    n = ((DOMSource)source).getNode();
                } else if (source instanceof StreamSource) {
                    StreamSource streamSource = (StreamSource)source;
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    InputSource inputSource = null;
                    if (streamSource.getInputStream() != null) {
                        inputSource = new InputSource(streamSource.getInputStream());
                    } else if (streamSource.getReader() != null) {
                        inputSource = new InputSource(streamSource.getReader());
                    }
                    n = db.parse(inputSource);
                } else {
                    throw new RuntimeException("Unsupported source: " + source);
                }
                NodeList children = n.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child.getNodeName().equals("add")) {
                        num1 = child.getAttributes().getNamedItem("num1").getNodeValue();
                        num2 = child.getAttributes().getNamedItem("num2").getNodeValue();
                        break;
                    }
                }
            }

            int n1 = Integer.parseInt(num1);
            int n2 = Integer.parseInt(num2);
            return createResultSource(n1 + n2);
        } catch(Exception e) {
            e.printStackTrace();
            throw new HTTPException(500);
        }
    }
    
    private Source createResultSource(int sum) {
        String body =
            "<ns:addResponse xmlns:ns=\"http://geronimo.apache.org\"><ns:return>"
            + sum
            + "</ns:return></ns:addResponse>";
        Source source = new StreamSource(new ByteArrayInputStream(body.getBytes()));
        return source;
    }
}
