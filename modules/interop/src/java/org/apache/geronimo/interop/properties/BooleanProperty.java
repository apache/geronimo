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

public class BooleanProperty extends PropertyType {
    private boolean _defaultValue = false;

    public BooleanProperty(Class componentClass, String propertyName) {
        super(componentClass, propertyName);
    }

    public BooleanProperty displayName(String displayName) {
        setDisplayName(displayName);
        return this;
    }

    public BooleanProperty displayOnlyIf(PropertyType other, String value) {
        setDisplayOnlyIf(other, value);
        return this;
    }

    public BooleanProperty description(String description) {
        setDescription(description);
        return this;
    }

    public BooleanProperty consoleHelp(String consoleHelp) {
        setConsoleHelp(consoleHelp);
        return this;
    }

    public BooleanProperty sortOrder(int sortOrder) {
        setSortOrder(sortOrder);
        return this;
    }

    public BooleanProperty defaultValue(boolean defaultValue) {
        _defaultValue = defaultValue;
        return this;
    }

    public boolean getDefaultValue() {
        return _defaultValue;
    }

    public String getDefaultValueAsString() {
        return String.valueOf(_defaultValue);
    }

    public boolean getBoolean() {
        return getBoolean(null, getComponentProperties());
    }

    public boolean getBoolean(String instanceName, PropertyMap props) {
        boolean b;
        boolean ok = true;
        String value = props.getProperty(_propertyName, String.valueOf(_defaultValue));
        value = value.toLowerCase();
        if (value.equals("true")) {
            b = true;
        } else if (value.equals("false")) {
            b = false;
        } else {
            ok = false;
            b = false;
        }
        if (!ok) {
            badPropertyValue(instanceName, value, expectedTrueOrFalse());
        }
        logPropertyValue(instanceName, value, b == _defaultValue);
        return b;
    }
}
