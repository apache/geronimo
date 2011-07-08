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

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.xmlbeans.XmlObject;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public abstract class DConfigBeanSupport extends XmlBeanSupport implements DConfigBean {
    private DDBean ddBean;

    public DConfigBeanSupport(DDBean ddBean, XmlObject xmlObject) {
        super(xmlObject);
        this.ddBean = ddBean;
    }

    protected void setParent(DDBean ddBean, XmlObject xmlObject) {
        this.ddBean = ddBean;
        setXmlObject(xmlObject);
    }

    public DDBean getDDBean() {
        return ddBean;
    }

    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException {
        throw new ConfigurationException("No DConfigBean matching DDBean "+bean);
    }

    public String[] getXpaths() {
        return null;
    }

    public void removeDConfigBean(DConfigBean bean) throws BeanNotFoundException {
        throw new BeanNotFoundException("No children");
    }

    public void notifyDDChange(XpathEvent event) {
    }

    protected String[] getXPathsWithPrefix(String prefix, String[][] xpathSegments) {
        String[] result = new String[xpathSegments.length];
        for (int i = 0; i < xpathSegments.length; i++) {
            String[] xpathSegmentArray = xpathSegments[i];
            StringBuilder xpath = new StringBuilder();
            for (int j = 0; j < xpathSegmentArray.length; j++) {
                String segment = xpathSegmentArray[j];
                if (prefix != null) {
                    xpath.append(prefix).append(":");
                }
                xpath.append(segment);
                if (j < xpathSegmentArray.length -1) {
                    xpath.append("/");
                }
            }
            result[i] = xpath.toString();
        }
        return result;
    }

    protected String[] getXPathsFromNamespace(String uri, String[][] xpathSegments) {
        String[] attributeNames = ddBean.getRoot().getAttributeNames();
        for (int i = 0; i < attributeNames.length; i++) {
            String attributeName = attributeNames[i];
            if (attributeName.startsWith("xmlns")) {
                if (ddBean.getRoot().getAttributeValue(attributeName).equals(uri)) {
                    if (attributeName.equals("xmlns")) {
                        return getXPathsWithPrefix(null , xpathSegments);
                    }
                    return getXPathsWithPrefix(attributeName.substring(6), xpathSegments);
                }
            }
        }
        //we can't determine the namespace from looking at attributes, since the namespace is not an attribute.
        //try assuming that the ddbeans strip namespaces from their xpath handing.
        return getXPathsWithPrefix(null , xpathSegments);
    }

    /**
     * Each entry in the first array is an XPath.
     * Each entry in the enclosed array is a component of that XPath (slashes omitted).
     * so {{"foo","bar"},{"baz","foo"}} would represent "foo/bar" and "baz/foo"
     */ 
    protected String[] getXPathsForJ2ee_1_4(String[][] xpathSegments) {
        return getXPathsFromNamespace("http://java.sun.com/xml/ns/j2ee", xpathSegments);
    }
}
