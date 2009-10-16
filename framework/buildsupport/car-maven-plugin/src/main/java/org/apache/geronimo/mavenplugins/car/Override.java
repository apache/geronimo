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


package org.apache.geronimo.mavenplugins.car;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.geronimo.system.configuration.AttributesXmlUtil;
import org.apache.geronimo.system.plugin.model.AttributesType;
import org.xml.sax.SAXException;

/**
 * @version $Rev$ $Date$
 */
public class Override {

    /**
     * server these overides apply to
     * @parameter
     */
    private String server;

    /**
     * file containing overrides
     * @parameter
     */
    private String overrides;

    public String getServer() {
        return server;
    }

    public AttributesType getOverrides(File base) throws IOException, JAXBException, ParserConfigurationException, SAXException, XMLStreamException {
        File file = new File(base, overrides);
        FileReader input = new FileReader(file);
        try {
            return AttributesXmlUtil.loadAttributes(input);
        } finally {
            input.close();
        }

    }
}
