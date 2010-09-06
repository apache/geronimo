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
package org.apache.geronimo.deployment.xmlbeans;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * @version $Rev$ $Date$
 */
public class XmlBeansUtil {
    private static final Logger logger = LoggerFactory.getLogger(XmlBeansUtil.class);
    private static final Map<String, String> NAMESPACE_UPDATES = new HashMap<String, String>();
    //TODO thread safe? conncurrentReaderMap?
    private static final Map<QName, QNameSet> substitutionGroups = new HashMap<QName, QNameSet>();
    private static final XmlObject[] NO_ELEMENTS = new XmlObject[]{};

    private XmlBeansUtil() {
    }

    public static void registerNamespaceUpdates(Map<String, String> updates) {
        NAMESPACE_UPDATES.putAll(updates);
    }

    public static void unregisterNamespaceUpdates(Map<String, String> updates) {
        NAMESPACE_UPDATES.entrySet().removeAll(updates.entrySet());
    }

    public static XmlObject parse(File file) throws IOException, XmlException {
        ArrayList errors = new ArrayList();
        XmlObject parsed = XmlObject.Factory.parse(file, createXmlOptions(errors));
        if (errors.size() != 0) {
            throw new XmlException(errors.toArray().toString());
        }
        return parsed;
    }

    public static XmlObject parse(URL url, ClassLoader cl) throws IOException, XmlException {
        ArrayList errors = new ArrayList();
        Thread currentThread = Thread.currentThread();
        ClassLoader oldcl = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(cl);
        XmlObject parsed;
        try {
            parsed = XmlObject.Factory.parse(url, createXmlOptions(errors));
        } finally {
            currentThread.setContextClassLoader(oldcl);
        }
        if (errors.size() != 0) {
            throw new XmlException(errors.toArray().toString());
        }
        return parsed;
    }

    public static XmlObject parse(InputStream is) throws IOException, XmlException {
        ArrayList errors = new ArrayList();
        XmlObject parsed = XmlObject.Factory.parse(is, createXmlOptions(errors));
        if (errors.size() != 0) {
            throw new XmlException(errors.toArray().toString());
        }
        return parsed;
    }

    public static XmlObject parse(String xml) throws XmlException {
        ArrayList errors = new ArrayList();
        XmlObject parsed = XmlObject.Factory.parse(xml, createXmlOptions(errors));
        if (errors.size() != 0) {
            throw new XmlException(errors.toArray().toString());
        }
        return parsed;
    }

    public static XmlObject parse(Element element) throws XmlException {
        ArrayList errors = new ArrayList();
        XmlObject parsed = XmlObject.Factory.parse(element, createXmlOptions(errors));
        if (errors.size() != 0) {
            throw new XmlException(errors.toArray().toString());
        }
        return parsed;
    }

    public static XmlOptions createXmlOptions(Collection errors) {
        XmlOptions options = new XmlOptions();
        options.setLoadLineNumbers();
        options.setErrorListener(errors);
        options.setLoadSubstituteNamespaces(NAMESPACE_UPDATES);
        return options;
    }

    public static void registerSubstitutionGroupElements(QName substitutionGroup, QNameSet substitutions) {
        QNameSet oldSubstitutions = substitutionGroups.get(substitutionGroup);
        if (oldSubstitutions != null) {
            substitutions = oldSubstitutions.union(substitutions);
        }
        substitutionGroups.put(substitutionGroup, substitutions);
    }

    public static void unregisterSubstitutionGroupElements(QName substitutionGroup, QNameSet substitutions) {
        QNameSet oldSubstitutions = substitutionGroups.get(substitutionGroup);
        if (oldSubstitutions != null && substitutions != null) {
            QNameSet difference = oldSubstitutions.intersect(substitutions.inverse());
            substitutionGroups.put(substitutionGroup, difference);
        }
    }

    public static QNameSet getQNameSetForSubstitutionGroup(QName substitutionGroup) {
        return substitutionGroups.get(substitutionGroup);
    }

    public static XmlObject[] selectSubstitutionGroupElements(QName substitutionGroup, XmlObject container) {
        QNameSet substitutionGroupMembers = getQNameSetForSubstitutionGroup(substitutionGroup);
        if (substitutionGroupMembers == null) {
            return NO_ELEMENTS;
        }
        return container.selectChildren(substitutionGroupMembers);
    }

    public static XmlObject typedCopy(XmlObject in, SchemaType type) throws XmlException {
        XmlObject out = in.copy().changeType(type);
        validateDD(out);
        return out;
    }

    public static void validateDD(XmlObject dd) throws XmlException {
        validateDD(dd, Collections.<String>emptySet());
    }

    public static void validateDD(XmlObject dd, Set<String> ignoreElements) throws XmlException {
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setLoadLineNumbers();
        Collection errors = new ArrayList();
        xmlOptions.setErrorListener(errors);
        xmlOptions.setValidateTreatLaxAsSkip();
        try {
            if (!dd.validate(xmlOptions)) {

                for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
                    Object o = iterator.next();
                    if (o instanceof XmlValidationError) {
                        XmlValidationError validationError = (XmlValidationError) o;
                        List<QName> expected = validationError.getExpectedQNames();
                        QName actual = validationError.getOffendingQName();
                        if (actual != null && ignoreElements.contains(actual.getLocalPart())) {
                            iterator.remove();
                            logger.warn(actual.getLocalPart() + " is not supported yet.");
                        }
                        if (expected != null) {
                            for (QName expectedQName : expected) {
                                QNameSet substitutions = getQNameSetForSubstitutionGroup(expectedQName);
                                if (substitutions != null && substitutions.contains(actual)) {
                                    iterator.remove();
                                    break;
                                }
                            }
                        }
                    }
                }

                if (!errors.isEmpty()) {
                    StringBuilder buf = new StringBuilder("Invalid deployment descriptor: errors:\n\n");
                    for (Object o : errors) {
                        buf.append(o).append("\n\n");
                    }
                    buf.append("Descriptor:\n").append(dd.toString()).append("\n");
                    throw new XmlException(buf.toString(), null, errors);
                }
            }
        } catch (NullPointerException e) {
            //ignore
        } catch (AssertionError e) {
            //ignore.  Would be the NPE above if assertions were turned off
        }
    }
}
