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


package org.apache.geronimo.web25.deployment.model;

import java.io.InputStream;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @version $Rev:$ $Date:$
 */
public class WebJaxbUtil {
    public static final XMLInputFactory XMLINPUT_FACTORY = XMLInputFactory.newInstance();
    private static  JAXBContext WEB_APP_CONTEXT;
    static {
        try {
            WEB_APP_CONTEXT = JAXBContext.newInstance(WebAppType.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }


    public static WebAppType unmarshalWebApp(InputStream in, boolean validate) throws XMLStreamException, JAXBException {
        XMLStreamReader xmlStream = XMLINPUT_FACTORY.createXMLStreamReader(in);
        Unmarshaller unmarshaller = WEB_APP_CONTEXT.createUnmarshaller();
        JAXBElement<WebAppType> element = unmarshaller.unmarshal(xmlStream, WebAppType.class);
        WebAppType moduleType = element.getValue();
        return moduleType;
    }

    public static void marshalWebApp(WebAppType object, Writer out) throws JAXBException {
        Marshaller marshaller = WEB_APP_CONTEXT.createMarshaller();

        marshaller.setProperty("jaxb.formatted.output", true);

        marshaller.marshal(object, out);
    }
}
