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

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.apache.geronimo.kernel.config.ConfigurationData;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationDataConverter extends ReflectionConverter {
    public ConfigurationDataConverter(ReflectionProvider reflectionProvider, Mapper mapper) {
        super(mapper, reflectionProvider);
    }

    public boolean canConvert(Class clazz) {
        return ConfigurationData.class.isAssignableFrom(clazz);
    }

    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext marshallingContext) {
        ConfigurationData configurationData = (ConfigurationData) object;
        writer.addAttribute("configuration-data-version", "1.1");
        super.marshal(configurationData, writer, marshallingContext);
    }
}
