/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.properties;

public class ShortProperty extends PropertyType {
    private short   defaultValue = 0;
    private short   minimumValue = 0;
    private short   maximumValue = Short.MAX_VALUE;

    public ShortProperty(Class componentClass, String propertyName) {
        super(componentClass, propertyName);
    }

    public ShortProperty displayName(String displayName) {
        setDisplayName(displayName);
        return this;
    }

    public ShortProperty displayOnlyIf(PropertyType other, String value) {
        setDisplayOnlyIf(other, value);
        return this;
    }

    public ShortProperty description(String description) {
        setDescription(description);
        return this;
    }

    public ShortProperty consoleHelp(String consoleHelp) {
        setConsoleHelp(consoleHelp);
        return this;
    }

    public ShortProperty sortOrder(int sortOrder) {
        setSortOrder(sortOrder);
        return this;
    }

    public ShortProperty defaultValue(short defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public ShortProperty minimumValue(short minimumValue) {
        this.minimumValue = minimumValue;
        return this;
    }

    public ShortProperty maximumValue(short maximumValue) {
        this.maximumValue = maximumValue;
        return this;
    }

    public short getDefaultValue() {
        return defaultValue;
    }

    public String getDefaultValueAsString() {
        return String.valueOf(defaultValue);
    }

    public short getMinimumValue() {
        return minimumValue;
    }

    public short getMaximumValue() {
        return maximumValue;
    }

    public short getShort() {
        return getShort(null, getComponentProperties());
    }

    public short getShort(String instanceName, PropertyMap props) {
        short n;
        boolean ok = true;
        String value = props.getProperty(getPropertyName(), String.valueOf(defaultValue));
        try {
            n = Short.parseShort(value);
        } catch (NumberFormatException ex) {
            ok = false;
            n = 0;
        }
        if (n < minimumValue || n > maximumValue) {
            ok = false;
        }
        if (!ok) {
            badPropertyValue(instanceName, value, expectedNumberInRange(minimumValue, maximumValue));
        }
        logPropertyValue(instanceName, value, n == defaultValue);
        return n;
    }
}
