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

package org.apache.geronimo.deployment.dconfigbean;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public abstract class XmlBeanSupport { // should implement Serializable or Externalizable
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private XmlObject xmlObject;

    public XmlBeanSupport(XmlObject xmlObject) {
        this.xmlObject = xmlObject;
    }

    protected void setXmlObject(XmlObject xmlObject) {
        this.xmlObject = xmlObject;
    }

    protected XmlObject getXmlObject() {
        return xmlObject;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

    public void toXML(OutputStream outputStream) throws IOException {
        XmlOptions options = new XmlOptions();
        options.setSavePrettyPrint();
        options.setSavePrettyPrintIndent(4);
        options.setUseDefaultNamespace();
        xmlObject.save(outputStream, options);
    }

    public void fromXML(InputStream inputStream) throws XmlException, IOException {
        xmlObject = getSchemaTypeLoader().parse(inputStream, null, null);
    }

    //override unless the particular object can never be read directly from xml, such as the
    //connector ConnectionDefinitionInstance.
    protected SchemaTypeLoader getSchemaTypeLoader() {
        return null;
    }

    // Must be public but should not be a JavaBean property -- sigh
    public boolean configured() {
        return getXmlObject() != null;
    }

    protected static boolean isEmpty(String s) {
        return s == null || s.trim().equals("");
    }
}
