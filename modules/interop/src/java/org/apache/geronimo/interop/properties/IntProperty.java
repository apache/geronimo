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

public class IntProperty extends PropertyType {
    private int _defaultValue = 0;

    private int _minimumValue = 0;

    private int _maximumValue = Integer.MAX_VALUE;

    public IntProperty(Class componentClass, String propertyName) {
        super(componentClass, propertyName);
    }

    public IntProperty displayName(String displayName) {
        setDisplayName(displayName);
        return this;
    }

    public IntProperty displayOnlyIf(PropertyType other, String value) {
        setDisplayOnlyIf(other, value);
        return this;
    }

    public IntProperty description(String description) {
        setDescription(description);
        return this;
    }

    public IntProperty consoleHelp(String consoleHelp) {
        setConsoleHelp(consoleHelp);
        return this;
    }

    public IntProperty sortOrder(int sortOrder) {
        setSortOrder(sortOrder);
        return this;
    }

    public IntProperty defaultValue(int defaultValue) {
        _defaultValue = defaultValue;
        return this;
    }

    public IntProperty minimumValue(int minimumValue) {
        _minimumValue = minimumValue;
        return this;
    }

    public IntProperty maximumValue(int maximumValue) {
        _maximumValue = maximumValue;
        return this;
    }

    public int getDefaultValue() {
        return _defaultValue;
    }

    public String getDefaultValueAsString() {
        return String.valueOf(_defaultValue);
    }

    public int getMinimumValue() {
        return _minimumValue;
    }

    public int getMaximumValue() {
        return _maximumValue;
    }

    public int getInt() {
        return getInt(null, getComponentProperties());
    }

    public int getInt(String instanceName, PropertyMap props) {
        int n;
        boolean ok = true;
        String value = props.getProperty(_propertyName, String.valueOf(_defaultValue));
        try {
            n = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            ok = false;
            n = 0;
        }
        if (n < _minimumValue || n > _maximumValue) {
            ok = false;
        }
        if (!ok) {
            badPropertyValue(instanceName, value, expectedNumberInRange(_minimumValue, _maximumValue));
        }
        logPropertyValue(instanceName, value, n == _defaultValue);
        return n;
    }
}
