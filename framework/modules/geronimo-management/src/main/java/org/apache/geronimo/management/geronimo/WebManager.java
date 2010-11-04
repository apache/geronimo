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

    ConnectorType getConnectorType(AbstractName connectorName);
    
    void updateConnectorConfig(AbstractName connectorName) throws Exception;

    public class ConnectorType {
        private final String description;


        public ConnectorType(String description) {
            this.description = description;
        }


        public String getDescription() {
            return description;
        }


        @Override
        public int hashCode() {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ((description == null) ? 0 : description.hashCode());
            return result;
        }


        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final ConnectorType other = (ConnectorType) obj;
            if (description == null) {
                if (other.description != null)
                    return false;
            } else if (!description.equals(other.description))
                return false;
            return true;
        }
    }

    public class ConnectorAttribute<T> {
        private final String attributeName;
        private String stringValue;
        private final Class<T> clazz;
        private T value;
        private final String description;
        private boolean required;

        public ConnectorAttribute(String attributeName, T value, String description, Class<T> clazz, boolean required) {
            this.attributeName = attributeName;
            this.value = value;
            this.description = description;
            this.clazz = clazz;
            this.required = required;
        }
        
        public ConnectorAttribute(String attributeName, T value, String description, Class<T> clazz) {
            this(attributeName, value, description, clazz, false);
        }

        public ConnectorAttribute(ConnectorAttribute<T> connectorAttribute) {
            this.attributeName = connectorAttribute.attributeName;
            this.value = connectorAttribute.value;
            this.description = connectorAttribute.description;
            this.clazz = connectorAttribute.clazz;
            this.required = connectorAttribute.required;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public String getStringValue() {
//            Class<T> clazz = getClass().getTypeParameters();
            if (value == null) return null;
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

        public Class<T> getAttributeClass() {
            return clazz;
        }
        
        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        @Override
        public String toString() {
            return "ConnectorAttribute [attributeName=" + attributeName + ", stringValue=" + stringValue
                    + ", required=" + required + "]";
        }

    }
}
