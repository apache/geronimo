/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.enterprise.deploy.model;

/**
 * An interface for beans that represent a fragment of a standard deployment
 * descriptor.  A link is provided to the J2EE application that includes this bean.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:50 $
 */
public interface DDBean {
    /**
     * Returns the location in the deployment descriptor from which this bean is derived.
     *
     * @return The XPath of this Bean.
     */
    public String getXpath();

    /**
     * Returns the XML text for by this bean.
     *
     * @return The XML text for this Bean.
     */
    public String getText();

    /**
     * Returns the ATTLIST ID value for the XML tag defined by the Xpath for this bean.
     *
     * @return The XML text for this Bean or 'null' if no attribute was specifed with the tag.
     */
    public String getId();

    /**
     * Return the root element for this DDBean.
     *
     * @return The DDBeanRoot at the root of this DDBean tree.
     */
    public DDBeanRoot getRoot();

    /**
     * Return a list of DDBeans based upon the XPath.
     *
     * @param xpath An XPath string referring to a location in the same deployment descriptor as this standard bean.
     *
     * @return a list of DDBeans or 'null' if no matching XML data is found.
     */
    public DDBean[] getChildBean(String xpath);

    /**
     * Return a list of text values for a given XPath in the deployment descriptor.
     *
     * @param xpath An XPath.
     *
     * @return The list text values for this XPath or 'null' if no matching XML data is found.
     */
    public String[] getText(String xpath);

    /**
     * Register a listener for a specific XPath.
     *
     * @param xpath The XPath this listener is to be registered for.
     * @param xpl The listener object.
     */
    public void addXpathListener(String xpath, XpathListener xpl);

    /**
     * Unregister a listener for a specific XPath.
     *
     * @param xpath The XPath this listener is to be registered for.
     * @param xpl The listener object.
     */
    public void removeXpathListener(String xpath, XpathListener xpl);

    /**
     * Returns the list of attribute names associated with XML element.
     *
     * @since 1.1
     *
     * @return a list of attribute names on this element.  Null
     * is returned if there are no attributes.
     */
    public String[] getAttributeNames();

    /**
     * Returns the string value of the named attribute.
     *
     * @since 1.1
     *
     * @return the value of the attribute.  Null is returned
     *   if there is no such attribute.
     */
    public String getAttributeValue(String attrName);
}