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

import java.util.List;

import org.apache.geronimo.interop.util.ExceptionUtil;


public abstract class PropertyType {
    protected static boolean _debug;

    static {
        try {
            _debug = Boolean.getBoolean("org.apache.geronimo.interop.debug:properties");
        } catch (Exception ignore) // e.g. SecurityException for Applet
        {
            _debug = false;
        }
    }

    protected Class _componentClass;

    protected String _propertyName;

    protected String _displayName;

    protected String _displayOnlyIfOther;

    protected String _displayOnlyIfValue;

    protected String _description;

    protected String _consoleHelp;

    protected int _sortOrder;

    public PropertyType(Class componentClass, String propertyName) {
        _componentClass = componentClass;
        _propertyName = propertyName;
    }

    public Class getComponentClass() {
        return _componentClass;
    }

    public PropertyMap getComponentProperties() {
        if (_componentClass == SystemProperties.class) {
            return SystemProperties.getInstance();
        } else {
            return null; // Component.forClass(_componentClass).getProperties();
        }
    }

    public String getPropertyName() {
        return _propertyName;
    }

    public String getDisplayName() {
        return _displayName;
    }

    public String getDisplayOnlyIfOther() {
        return _displayOnlyIfOther;
    }

    public String getDisplayOnlyIfValue() {
        return _displayOnlyIfValue;
    }

    public String getDescription() {
        return _description;
    }

    public String getConsoleHelp() {
        return _consoleHelp;
    }

    public int getSortOrder() {
        return _sortOrder;
    }

    public String getDefaultValueAsString() {
        return "";
    }

    public boolean isList() {
        return false;
    }

    public boolean isReadOnly() {
        return false;
    }

    public void setDisplayName(String displayName) {
        _displayName = displayName;
    }

    public void setDisplayOnlyIf(PropertyType other, String value) {
        _displayOnlyIfOther = other.getPropertyName();
        _displayOnlyIfValue = value;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public void setConsoleHelp(String consoleHelp) {
        _consoleHelp = consoleHelp;
    }

    public void setSortOrder(int sortOrder) {
        _sortOrder = sortOrder;
    }

    public void badPropertyValue(String instanceName, String value) {
        badPropertyValue(instanceName, value, (String) null);
    }

    public void badPropertyValue(String instanceName, String value, Exception ex) {
        badPropertyValue(instanceName, value, "exception: " + ExceptionUtil.getStackTrace(ex));
    }

    public void badPropertyValue(String instanceName, String value, String reason) {
        // TODO: I18N
        /* 
        throw new SystemException("Bad value '" + value
            + "' for property '" + _propertyName
            + "' of component " + _componentClass.getName()
            + (instanceName == null ? "" : (", instance " + instanceName))
            + (reason != null ? (", " + reason) : ""));
            */
        Thread.dumpStack();
    }

    public String expectedNumberInRange(long minimumValue, long maximumValue) {
        // TODO: I18N
        return "expected number in range [" + minimumValue + " .. " + maximumValue + "]";
    }

    public String expectedNumberInRange(double minimumValue, double maximumValue) {
        // TODO: I18N
        return "expected number in range [" + minimumValue + " .. " + maximumValue + "]";
    }

    public String expectedTrueOrFalse() {
        // TODO: I18N
        return "expected true or false";
    }

    public String expectedValueInList(List legalValues) {
        // TODO: I18N
        return "expected value in list " + legalValues;
    }

    public void logPropertyValue(String instanceName, String value, boolean usingDefaultValue) {
        if (_propertyName.toLowerCase().endsWith("password")) {
            value = "******";
        }
        if (_debug) // TODO: allow for bootstrap
        {
            if (usingDefaultValue) {
                if (_componentClass == SystemProperties.class) {
                    SystemPropertyLog.getInstance(_propertyName).debugUsingDefaultValue(value);
                } else {
                    getLog(instanceName).debugUsingDefaultValue(value);
                }
            } else {
                if (_componentClass == SystemProperties.class) {
                    SystemPropertyLog.getInstance(_propertyName).debugUsingValue(value);
                } else {
                    getLog(instanceName).debugUsingValue(value);
                }
            }
        }
    }

    public String getContext(String instanceName) {
        /*
        String showName = JavaClass.getNameSuffix(_componentClass.getName());
        // TODO: optional full component name
        return showName + (instanceName != null ? (":" + instanceName) : "");
        */
        return "TODO: PropertyType.getContext()";
    }

    public PropertyLog getLog(String instanceName) {
        return PropertyLog.getInstance(_propertyName + ", " + getContext(instanceName));
    }
}
