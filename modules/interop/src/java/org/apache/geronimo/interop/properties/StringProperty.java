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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.interop.util.FileUtil;
import org.apache.geronimo.interop.util.ListUtil;
import org.apache.geronimo.interop.util.StringUtil;

public class StringProperty extends PropertyType {
    private String      defaultValue = "";
    private List        valueIsInList = null;
    private Map         displayValues = null;
    private Class       valueIsNameOf = null;
    private boolean     isDirName = false;
    private boolean     isFileName = false;
    private boolean     isList = false;
    private boolean     isReadOnly = false;
    private boolean     isRequired = false;

    public StringProperty(Class componentClass, String propertyName) {
        super(componentClass, propertyName);
    }

    public StringProperty displayName(String displayName) {
        setDisplayName(displayName);
        return this;
    }

    public StringProperty displayOnlyIf(PropertyType other, String value) {
        setDisplayOnlyIf(other, value);
        return this;
    }

    public StringProperty description(String description) {
        setDescription(description);
        return this;
    }

    public StringProperty consoleHelp(String consoleHelp) {
        setConsoleHelp(consoleHelp);
        return this;
    }

    public StringProperty sortOrder(int sortOrder) {
        setSortOrder(sortOrder);
        return this;
    }

    public StringProperty defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public StringProperty legalValues(Class valueIsNameOf) {
        this.valueIsNameOf = valueIsNameOf;
        return this;
    }

    public StringProperty legalValues(List valueIsInList) {
        valueIsInList = Collections.unmodifiableList(valueIsInList);
        return this;
    }

    public StringProperty legalValues(String valueIsInList) {
        List list = ListUtil.getCommaSeparatedList(valueIsInList);
        this.valueIsInList = new ArrayList(list.size());
        for (Iterator i = list.iterator(); i.hasNext();) {
            String value = (String) i.next();
            if (value.indexOf('=') != -1) {
                String displayValue = StringUtil.afterFirst("=", value).trim();
                value = StringUtil.beforeFirst("=", value).trim();
                if (displayValues == null) {
                    displayValues = new HashMap();
                }
                displayValues.put(value, displayValue);
            }
            this.valueIsInList.add(value);
        }
        return this;
    }

    public String getDisplayValue(String value) {
        if (displayValues != null) {
            String displayValue = (String) displayValues.get(value);
            if (displayValue != null) {
                return displayValue;
            }
        }
        return value;
    }

    public StringProperty isDirName() {
        isDirName = true;
        return this;
    }

    public StringProperty isFileName() {
        isFileName = true;
        return this;
    }

    public StringProperty list() {
        isList = true;
        return this;
    }

    public StringProperty readOnly() {
        isReadOnly = true;
        return this;
    }

    public StringProperty required() {
        isRequired = true;
        return this;
    }

    public boolean isList() {
        return isList;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDefaultValueAsString() {
        return defaultValue;
    }

    public List getLegalValues() {
        if (valueIsInList != null) {
            return valueIsInList;
        } else if (valueIsNameOf != null) {
            //return Repository.getInstance().getInstanceNames(_valueIsNameOf);
            return null;
        } else {
            return null;
        }
    }

    public String getString() {
        return getString(null, getComponentProperties());
    }

    public String getString(String instanceName, PropertyMap props) {
        boolean ok = true, usingDefaultValue = false;
        String s = props.getProperty(getPropertyName(), defaultValue);
        if (s != null && s.startsWith("${")) {
            // Value is contained in system property.
            s = StringUtil.removePrefix(s, "${");
            s = StringUtil.removeSuffix(s, "}");
            StringProperty sp = new StringProperty(SystemProperties.class, s);
            s = sp.getString();
            if (s == null || s.length() == 0) {
                if (isRequired()) {
                    String message = getLog(instanceName).errorMissingValueForRequiredSystemProperty(sp.getPropertyName(), getPropertyName(), getContext(instanceName));
                    throw new MissingRequiredPropertyException(message);
                }
            }
        }
        if (s == null && !isRequired()) {
            s = "";
        }
        List legalValues = getLegalValues();
        if (legalValues != null) {
            ok = false;
            for (Iterator i = legalValues.iterator(); i.hasNext();) {
                String legalValue = (String) i.next();
                if (s != null && s.equals(legalValue)) {
                    ok = true;
                    break;
                }
            }
            if (!isRequired() && s.equals("")) {
                ok = true;
            }
        }
        if (!ok) {
            badPropertyValue(instanceName, s, expectedValueInList(legalValues));
        }
        if (isDirName || isFileName) {
            s = FileUtil.expandHomeRelativePath(s);
            s = FileUtil.pretty(s);
        }
        if (s == null || s.length() == 0) {
            if (isRequired()) {
                String message = getLog(instanceName).errorMissingValueForRequiredProperty(getPropertyName(), getContext(instanceName));
                throw new MissingRequiredPropertyException(message);
            }
        }
        logPropertyValue(instanceName, s, s != null && s.equals(defaultValue));
        return s;
    }
}
