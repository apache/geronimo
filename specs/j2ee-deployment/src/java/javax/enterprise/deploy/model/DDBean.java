/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * This source code implements specifications defined by the Java
 * Community Process. In order to remain compliant with the specification
 * DO NOT add / change / or delete method signatures!
 *
 * ====================================================================
 */
package javax.enterprise.deploy.model;

/**
 * An interface for beans that represent a fragment of a standard deployment
 * descriptor.  A link is provided to the J2EE application that includes this bean.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/30 02:16:58 $
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