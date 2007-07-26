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
package org.apache.geronimo.management.geronimo;

import java.util.List;
import java.util.ArrayList;
import java.beans.PropertyEditor;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.common.propertyeditor.PropertyEditors;

/**
 * Specialization of NetworkManager for web containers.
 *
 * @version $Rev$ $Date$
 */
public interface WebManager extends NetworkManager {
    public final static String PROTOCOL_HTTP = "HTTP";
    public final static String PROTOCOL_HTTPS = "HTTPS";
    public final static String PROTOCOL_AJP = "AJP";

    /**
     * Creates and returns a new web connector.  Note that the connector may
     * well require further customization before being fully functional (e.g.
     * SSL settings for a secure connector).  This may need to be done before
     * starting the resulting connector.
     *
     * @param container    The container to add the connector to
     * @param uniqueName   A name fragment that's unique to this connector
     * @param protocol     The protocol that the connector should use
     * @param host         The host name or IP that the connector should listen on
     * @param port         The port that the connector should listen on
     *
     * @return The ObjectName of the new connector.
     * @deprecated
     */
    public WebConnector addConnector(WebContainer container, String uniqueName, String protocol, String host, int port);

    /**
     * Gets the WebAccessLog implementation for a web container.
     * May be null if the access log cannot be managed.
     *
     * @param container The container whose access log is interesting
     *
     */
    public WebAccessLog getAccessLog(WebContainer container);

    List<ConnectorType> getConnectorTypes();

    List<ConnectorAttribute> getConnectorAttributes(ConnectorType connectorType);

    AbstractName getConnectorConfiguration(ConnectorType connectorType, List<ConnectorAttribute> connectorAttributes, WebContainer container, String uniqueName);

    public class ConnectorType {
        private final String description;


        public ConnectorType(String description) {
            this.description = description;
        }


        public String getDescription() {
            return description;
        }
    }

    public class ConnectorAttribute<T> {
        private final String attributeName;
        private String stringValue;
        private final Class<T> clazz;
        private T value;
        private final String description;

        public ConnectorAttribute(String attributeName, T value, String description, Class<T> clazz) {
            this.attributeName = attributeName;
            this.value = value;
            this.description = description;
            this.clazz = clazz;
        }

        public ConnectorAttribute(ConnectorAttribute<T> connectorAttribute) {
            this.attributeName = connectorAttribute.attributeName;
            this.stringValue = connectorAttribute.stringValue;
            this.description = connectorAttribute.description;
            this.clazz = connectorAttribute.clazz;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public String getStringValue() {
//            Class<T> clazz = getClass().getTypeParameters();
            PropertyEditor propertyEditor = PropertyEditors.getEditor(clazz);
            propertyEditor.setValue(value);
            return propertyEditor.getAsText();
        }

        public void setStringValue(String stringValue) {
            PropertyEditor propertyEditor = PropertyEditors.getEditor(clazz);
            propertyEditor.setAsText(stringValue);
            this.value = (T) propertyEditor.getValue();
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public static List<ConnectorAttribute> copy(List<ConnectorAttribute> source) {
            List<ConnectorAttribute> copy = new ArrayList<ConnectorAttribute>(source.size());
            for (ConnectorAttribute connectorAttribute: source) {
                copy.add(new ConnectorAttribute(connectorAttribute));
            }
            return copy;
        }
    }
}
