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




public class LongProperty extends PropertyType {
    private long _defaultValue = 0;

    private long _minimumValue = 0;

    private long _maximumValue = Integer.MAX_VALUE;

    public LongProperty(Class componentClass, String propertyName) {
        super(componentClass, propertyName);
    }

    public LongProperty displayName(String displayName) {
        setDisplayName(displayName);
        return this;
    }

    public LongProperty displayOnlyIf(PropertyType other, String value) {
        setDisplayOnlyIf(other, value);
        return this;
    }

    public LongProperty description(String description) {
        setDescription(description);
        return this;
    }

    public LongProperty consoleHelp(String consoleHelp) {
        setConsoleHelp(consoleHelp);
        return this;
    }

    public LongProperty sortOrder(int sortOrder) {
        setSortOrder(sortOrder);
        return this;
    }

    public LongProperty defaultValue(long defaultValue) {
        _defaultValue = defaultValue;
        return this;
    }

    public LongProperty minimumValue(long minimumValue) {
        _minimumValue = minimumValue;
        return this;
    }

    public LongProperty maximumValue(long maximumValue) {
        _maximumValue = maximumValue;
        return this;
    }

    public long getDefaultValue() {
        return _defaultValue;
    }

    public String getDefaultValueAsString() {
        return String.valueOf(_defaultValue);
    }

    public long getMinimumValue() {
        return _minimumValue;
    }

    public long getMaximumValue() {
        return _maximumValue;
    }

    public long getLong() {
        return getLong(null, getComponentProperties());
    }

    /*
    public long getLong(Object instance)
    {
        return getLong(((Component.InstanceName)instance).getInstanceName(),
            ((Component.InstanceProperties)instance).getInstanceProperties());
    }
    */

    public long getLong(String instanceName, PropertyMap props) {
        long n;
        boolean ok = true;
        String value = props.getProperty(_propertyName, String.valueOf(_defaultValue));
        try {
            n = Long.parseLong(value);
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
