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
package org.apache.geronimo.connector.deployment;

import javax.xml.namespace.QName;

import org.apache.geronimo.xbeans.connector.GerConnectorType;
import org.apache.geronimo.xbeans.connector.GerResourceadapterType;
import org.apache.geronimo.xbeans.connector.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.connector.GerConnectiondefinitionInstanceType;
import org.apache.xmlbeans.XmlCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class ConnectorPlanRectifier {

    private static final Logger log = LoggerFactory.getLogger(ConnectorPlanRectifier.class);

    private static final QName VERSION_QNAME = new QName("", "version");
    private static final QName GLOBAL_JNDI_NAME_QNAME = new QName(ConnectorModuleBuilder.GERCONNECTOR_NAMESPACE, "global-jndi-name");
    private static final QName CREDENTIAL_INTERFACE_QNAME = new QName(ConnectorModuleBuilder.GERCONNECTOR_NAMESPACE, "credential-interface");


    static void rectifyPlan(GerConnectorType gerConnector) {
        boolean updated = false;
        XmlCursor cursor = gerConnector.newCursor();
        try {
            updated = cursor.removeAttribute(VERSION_QNAME);
        } finally {
            cursor.dispose();
        }
        GerResourceadapterType[] resourceAdapters = gerConnector.getResourceadapterArray();
        for (int i = 0; i < resourceAdapters.length; i++) {
            GerResourceadapterType resourceAdapter = resourceAdapters[i];
            if (resourceAdapter.isSetOutboundResourceadapter()) {
            GerConnectionDefinitionType[] connectionDefinitions = resourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray();
                for (int j = 0; j < connectionDefinitions.length; j++) {
                    GerConnectionDefinitionType connectionDefinition = connectionDefinitions[j];
                    GerConnectiondefinitionInstanceType[] connectiondefinitionInstances = connectionDefinition.getConnectiondefinitionInstanceArray();
                    for (int k = 0; k < connectiondefinitionInstances.length; k++) {
                        GerConnectiondefinitionInstanceType connectiondefinitionInstance = connectiondefinitionInstances[k];
                        cursor = connectiondefinitionInstance.newCursor();
                        try {
                            if (cursor.toFirstChild()) {
                                if (cursor.toNextSibling(GLOBAL_JNDI_NAME_QNAME)) {
                                    cursor.removeXml();
                                    updated = true;
                                }
                                if (cursor.toNextSibling(CREDENTIAL_INTERFACE_QNAME)) {
                                    cursor.removeXml();
                                    updated = true;
                                }
                            }
                        } finally {
                            cursor.dispose();
                        }
                    }
                }
            }
        }
        if (updated) {
            log.warn("Your connector plan has obsolete elements or attributes in it.  Please remove version attributes, global-jndi-name elements, and credential-interface elements");
        }
    }

}
