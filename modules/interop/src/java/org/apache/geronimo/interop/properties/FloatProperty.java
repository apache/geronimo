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

public class FloatProperty extends PropertyType {
    private float _defaultValue = 0;

    private float _minimumValue = 0;

    private float _maximumValue = Float.MAX_VALUE;

    public FloatProperty(Class componentClass, String propertyName) {
        super(componentClass, propertyName);
    }

    public FloatProperty displayName(String displayName) {
        setDisplayName(displayName);
        return this;
    }

    public FloatProperty displayOnlyIf(PropertyType other, String value) {
        setDisplayOnlyIf(other, value);
        return this;
    }

    public FloatProperty description(String description) {
        setDescription(description);
        return this;
    }

    public FloatProperty consoleHelp(String consoleHelp) {
        setConsoleHelp(consoleHelp);
        return this;
    }

    public FloatProperty sortOrder(int sortOrder) {
        setSortOrder(sortOrder);
        return this;
    }

    public FloatProperty defaultValue(float defaultValue) {
        _defaultValue = defaultValue;
        return this;
    }

    public FloatProperty minimumValue(float minimumValue) {
        _minimumValue = minimumValue;
        return this;
    }

    public FloatProperty maximumValue(float maximumValue) {
        _maximumValue = maximumValue;
        return this;
    }

    public float getDefaultValue() {
        return _defaultValue;
    }

    public String getDefaultValueAsString() {
        return String.valueOf(_defaultValue);
    }

    public float getMinimumValue() {
        return _minimumValue;
    }

    public float getMaximumValue() {
        return _maximumValue;
    }

    public float getFloat() {
        return getFloat(null, getComponentProperties());
    }

    public float getFloat(String instanceName, PropertyMap props) {
        float n;
        boolean ok = true;
        String value = props.getProperty(_propertyName, String.valueOf(_defaultValue));
        try {
            n = Float.parseFloat(value);
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
