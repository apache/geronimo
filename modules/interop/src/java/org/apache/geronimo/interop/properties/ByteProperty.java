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

public class ByteProperty extends PropertyType {
    private byte    defaultValue = 0;
    private byte    minimumValue = 0;
    private byte    maximumValue = Byte.MAX_VALUE;

    public ByteProperty(Class componentClass, String propertyName) {
        super(componentClass, propertyName);
    }

    public ByteProperty displayName(String displayName) {
        setDisplayName(displayName);
        return this;
    }

    public ByteProperty displayOnlyIf(PropertyType other, String value) {
        setDisplayOnlyIf(other, value);
        return this;
    }

    public ByteProperty description(String description) {
        setDescription(description);
        return this;
    }

    public ByteProperty consoleHelp(String consoleHelp) {
        setConsoleHelp(consoleHelp);
        return this;
    }

    public ByteProperty sortOrder(int sortOrder) {
        setSortOrder(sortOrder);
        return this;
    }

    public ByteProperty defaultValue(byte defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public ByteProperty minimumValue(byte minimumValue) {
        this.minimumValue = minimumValue;
        return this;
    }

    public ByteProperty maximumValue(byte maximumValue) {
        this.maximumValue = maximumValue;
        return this;
    }

    public byte getDefaultValue() {
        return defaultValue;
    }

    public String getDefaultValueAsString() {
        return String.valueOf(defaultValue);
    }

    public byte getMinimumValue() {
        return minimumValue;
    }

    public byte getMaximumValue() {
        return maximumValue;
    }

    public byte getByte() {
        return getByte(null, getComponentProperties());
    }

    public byte getByte(String instanceName, PropertyMap props) {
        byte n;
        boolean ok = true;
        String value = props.getProperty(getPropertyName(), String.valueOf(defaultValue));
        try {
            n = Byte.parseByte(value);
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
