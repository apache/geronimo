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

import java.net.URI;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.JVM;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.Version;

/**
 * @version $Rev$ $Date$
 */
public final class XStreamUtil {
    private XStreamUtil() {
    }

    public static XStream createXStream() {
        JVM jvm = new JVM();
        ReflectionProvider reflectionProvider = jvm.bestReflectionProvider();
        XStream xstream = new XStream(reflectionProvider);

        // AbstractName
        xstream.alias("abstractName", AbstractName.class);
        xstream.addImmutableType(AbstractName.class);
        xstream.registerConverter(new AbstractNameConverter());

        // AbstractNameQuery
        xstream.alias("abstractNameQuery", AbstractNameQuery.class);
        xstream.addImmutableType(AbstractNameQuery.class);
        xstream.registerConverter(new AbstractNameQueryConverter());

        // Artifact
        xstream.alias("artifact", Artifact.class);
        xstream.addImmutableType(Artifact.class);

        // ConfigurationData
        xstream.alias("configurationData", ConfigurationData.class);
        xstream.registerConverter(new ConfigurationDataConverter(reflectionProvider, xstream.getClassMapper()));

        // ConfigurationModuleTypeConverter
        xstream.alias("moduleType", ConfigurationModuleType.class);
        xstream.addImmutableType(ConfigurationModuleType.class);
        xstream.registerConverter(new ConfigurationModuleTypeConverter());

        // Dependency
        xstream.alias("dependency", Dependency.class);
        xstream.addImmutableType(Dependency.class);

        // GBeanData
        xstream.alias("gbean", GBeanData.class);
        xstream.registerConverter(new GBeanDataConverter(xstream.getClassMapper()));

        // GBeanInfo
        xstream.alias("gbean-info", GBeanInfo.class);

        // w3c Dom
        xstream.registerConverter(new DomConverter());

        // ImportType
        xstream.addImmutableType(ImportType.class);
        xstream.registerConverter(new ImportTypeConverter());

        // QName
        try {
            xstream.registerConverter(new QNameConverter());
        } catch (Exception e) {
            // cl can't see QName class so we don't need to register a converter for it
        }

        // Version
        xstream.alias("version", Version.class);
        xstream.addImmutableType(Version.class);
        xstream.registerConverter(new VersionConverter());

        // URI
        xstream.alias("uri", URI.class);
        xstream.addImmutableType(URI.class);
        xstream.registerConverter(new URIConverter());

        // XStreamGBeanState
        xstream.registerConverter(new XStreamGBeanStateConverter());

        return xstream;
    }
}
