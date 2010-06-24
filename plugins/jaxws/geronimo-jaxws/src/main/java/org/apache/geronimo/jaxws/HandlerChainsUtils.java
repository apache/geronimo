/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jaxws;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;


public class HandlerChainsUtils {

    public static final QName HANDLER_CHAINS_QNAME =
        new QName("http://java.sun.com/xml/ns/javaee", "handler-chains");

    private HandlerChainsUtils() {
    }

    public static <T> T toHandlerChains(String xml, Class<T> type)
            throws JAXBException {
        T handlerChains = null;
        if (xml != null) {
            JAXBContext ctx = JAXBContext.newInstance(type);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            StringReader reader = new StringReader(xml);
            StreamSource in = new StreamSource(reader);
            JAXBElement<T> handlerElement = unmarshaller.unmarshal(in, type);
            handlerChains = handlerElement.getValue();
        }
        return handlerChains;
    }

}
