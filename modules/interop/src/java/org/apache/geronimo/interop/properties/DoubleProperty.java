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

public class DoubleProperty extends PropertyType {
    private double  defaultValue = 0;
    private double  minimumValue = 0;
    private double  maximumValue = Double.MAX_VALUE;

    public DoubleProperty(Class componentClass, String propertyName) {
        super(componentClass, propertyName);
    }

    public DoubleProperty displayName(String displayName) {
        setDisplayName(displayName);
        return this;
    }

    public DoubleProperty displayOnlyIf(PropertyType other, String value) {
        setDisplayOnlyIf(other, value);
        return this;
    }

    public DoubleProperty description(String description) {
        setDescription(description);
        return this;
    }

    public DoubleProperty consoleHelp(String consoleHelp) {
        setConsoleHelp(consoleHelp);
        return this;
    }

    public DoubleProperty sortOrder(int sortOrder) {
        setSortOrder(sortOrder);
        return this;
    }

    public DoubleProperty defaultValue(double defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public DoubleProperty minimumValue(double minimumValue) {
        this.minimumValue = minimumValue;
        return this;
    }

    public DoubleProperty maximumValue(double maximumValue) {
        this.maximumValue = maximumValue;
        return this;
    }

    public double getDefaultValue() {
        return defaultValue;
    }

    public String getDefaultValueAsString() {
        return String.valueOf(defaultValue);
    }

    public double getMinimumValue() {
        return minimumValue;
    }

    public double getMaximumValue() {
        return maximumValue;
    }

    public double getDouble() {
        return getDouble(null, getComponentProperties());
    }

    public double getDouble(String instanceName, PropertyMap props) {
        double n;
        boolean ok = true;
        String value = props.getProperty(getPropertyName(), String.valueOf(defaultValue));
        try {
            n = Double.parseDouble(value);
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
