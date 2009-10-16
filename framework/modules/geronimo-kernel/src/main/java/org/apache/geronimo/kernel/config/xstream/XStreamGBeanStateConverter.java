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
package org.apache.geronimo.kernel.config.xstream;

import java.io.IOException;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * @version $Rev$ $Date$
 */
public class XStreamGBeanStateConverter implements Converter {
    public boolean canConvert(Class clazz) {
        return XStreamGBeanState.class.isAssignableFrom(clazz);
    }

    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext marshallingContext) {
        XStreamGBeanState gbeanState = (XStreamGBeanState) object;
        Element element = null;
        try {
            element = gbeanState.getGBeanState();
        } catch (IOException e) {
            throw new ConversionException("Cannot get xml version of gbeans", e);
        }
        marshallingContext.convertAnother(element);
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext unmarshallingContext) {
        Element element = (Element) unmarshallingContext.convertAnother(reader, Element.class);
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i ++) {
            Node node = childNodes.item(i);
            if (node instanceof Element) {
                element = (Element) node;
                return new XStreamGBeanState(element);
            }
        }
        throw new ConversionException("No nested nodes found");
    }
}
