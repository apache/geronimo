/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.directory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.xml.namespace.QName;

import org.apache.geronimo.xbeans.geronimo.directory.DirDirectoryConfigurationDocument;
import org.apache.geronimo.xbeans.geronimo.directory.DirAttributeDocument.Attribute;
import org.apache.geronimo.xbeans.geronimo.directory.DirBootStrapSchemasDocument.BootStrapSchemas;
import org.apache.geronimo.xbeans.geronimo.directory.DirContextEntryDocument.ContextEntry;
import org.apache.geronimo.xbeans.geronimo.directory.DirDirectoryConfigurationDocument.DirectoryConfiguration;
import org.apache.geronimo.xbeans.geronimo.directory.DirIndexedAttributesDocument.IndexedAttributes;
import org.apache.geronimo.xbeans.geronimo.directory.DirPartitionDocument.Partition;
import org.apache.geronimo.xbeans.geronimo.directory.DirPartitionsDocument.Partitions;
import org.apache.ldap.server.configuration.MutableContextPartitionConfiguration;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

public class DirectoryConfigurator {

    public DirectoryConfigurator() {
    }

    public void configure(ClassLoader cl,
            MutableServerStartupConfiguration startup, File file)
            throws Exception {
        DirDirectoryConfigurationDocument doc = parse(file);
        DirectoryConfiguration dirConfig = doc.getDirectoryConfiguration();

        Partitions partitions = dirConfig.getPartitions();
        if (partitions != null) {
            Partition partitionList[] = partitions.getPartitionArray();
            if (partitionList != null) {
                Set partition = new HashSet();
                for (int i = 0; i < partitionList.length; i++) {
                    partition.add(processPartition(partitionList[i]));
                }
                startup.setContextPartitionConfigurations(partition);
            }
        }

        BootStrapSchemas schemas = dirConfig.getBootStrapSchemas();
        if (schemas != null) {
            String schemaList[] = schemas.getSchemaArray();
            if (schemaList != null) {
                Set bootStrapSchemas = new HashSet();
                for (int i = 0; i < schemaList.length; i++) {
                    Class clazz = cl.loadClass(schemaList[i]);
                    bootStrapSchemas.add(clazz.newInstance());
                }
                startup.setBootstrapSchemas(bootStrapSchemas);
            }
        }
    }

    private MutableContextPartitionConfiguration processPartition(
            Partition partition) {
        MutableContextPartitionConfiguration mcpc = new MutableContextPartitionConfiguration();
        mcpc.setName(partition.getName());
        mcpc.setSuffix(partition.getSuffix());
        IndexedAttributes indexedAttributes = partition.getIndexedAttributes();
        if (indexedAttributes != null) {
            String attributeList[] = indexedAttributes
                    .getIndexedAttributeArray();
            if (attributeList != null) {
                Set set = new HashSet();
                for (int i = 0; i < attributeList.length; i++) {
                    set.add(attributeList[i]);
                }
                mcpc.setIndexedAttributes(set);
            }
        }

        ContextEntry contextEntry = partition.getContextEntry();
        if (contextEntry != null) {
            Attribute[] attributeList = contextEntry.getAttributeArray();
            if (attributeList != null) {
                BasicAttributes attrs = new BasicAttributes(true);
                for (int i = 0; i < attributeList.length; i++) {
                    BasicAttribute attr = new BasicAttribute(attributeList[i]
                            .getId());
                    String values[] = attributeList[i].getValueArray();
                    for (int j = 0; j < values.length; j++) {
                        attr.add(values[j]);
                    }
                    attrs.put(attr);
                }
                mcpc.setContextEntry(attrs);
            }
        }

        return mcpc;
    }

    private DirDirectoryConfigurationDocument parse(File file) throws Exception {
        ArrayList errors = new ArrayList();
        XmlObject config = XmlObject.Factory.parse(file,
                createXmlOptions(errors));

        if (errors.size() != 0) {
            throw new XmlException(errors.toArray().toString());
        }

        if (DirDirectoryConfigurationDocument.type.equals(config.schemaType())) {
            validateDD(config);
            return (DirDirectoryConfigurationDocument) config;
        }

        // If we got here, we will fail due to bad XML. We are doing this get an
        // explicit error message
        XmlObject result = config
                .changeType(DirDirectoryConfigurationDocument.type);
        validateDD(result);
        return (DirDirectoryConfigurationDocument) result;
    }

    private XmlOptions createXmlOptions(Collection errors) {
        Map NAMESPACE_UPDATES = new HashMap();
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/directory",
                "http://geronimo.apache.org/xml/ns/directory-1.0");
        XmlOptions options = new XmlOptions();
        options.setLoadLineNumbers();
        options.setErrorListener(errors);
        options.setLoadSubstituteNamespaces(NAMESPACE_UPDATES);
        options.setUseDefaultNamespace();
        return options;
    }

    private void validateDD(XmlObject dd) throws XmlException {
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setDocumentType(DirDirectoryConfigurationDocument.type);
        xmlOptions.setLoadLineNumbers();
        xmlOptions.setUseDefaultNamespace();
        Collection errors = new ArrayList();
        xmlOptions.setErrorListener(errors);
        if (!dd.validate(xmlOptions)) {
            throw new XmlException("Invalid directory descriptor: " + errors
                    + "\nDescriptor: " + dd.toString(), null, errors);
        }
        // System.out.println("descriptor: " + dd.toString());
    }
}
