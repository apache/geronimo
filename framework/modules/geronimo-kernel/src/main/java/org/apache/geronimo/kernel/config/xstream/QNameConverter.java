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

import java.lang.reflect.Method;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @version $Rev$ $Date$
 */
public class QNameConverter implements Converter {
    private static final String QNAME_CLASS = "javax.xml.namespace.QName";

    public boolean canConvert(Class clazz) {
        return QNAME_CLASS.equals(clazz.getName());
    }

    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext marshallingContext) {
        writer.setValue(object.toString());
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext unmarshallingContext) {
        Class qnameClass = unmarshallingContext.getRequiredType();
        if (!canConvert(qnameClass)) {
            throw new ConversionException("Unexpected type in unmarshal: " + qnameClass.getName());
        }

        String qnameString = reader.getValue();
        try {
            Method method = qnameClass.getMethod("valueOf", new Class[]{String.class});
            Object qname = method.invoke(null, new Object[] { qnameString });
            return qname;
        } catch (Exception e) {
            throw new ConversionException("Unable to convert value to a qname: " + qnameString, e);
        }
    }
}
