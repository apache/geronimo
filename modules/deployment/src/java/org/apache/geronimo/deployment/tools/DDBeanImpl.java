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
 * ====================================================================
 */
package org.apache.geronimo.deployment.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.XpathListener;

import org.apache.geronimo.deployment.util.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/21 20:37:29 $
 */
public class DDBeanImpl implements DDBean {
    protected final DDBeanRoot root;
    protected final Element element;
    protected final String xpath;
    protected final Map children;

    public DDBeanImpl(DDBeanRoot root, String xpath, Element element) {
        this.root = root;
        this.xpath = xpath;
        this.element = element;
        this.children = new HashMap();
        for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element) {
                Element child = (Element) node;
                List childs = (List) children.get(child.getNodeName());
                if (childs == null) {
                    childs = new ArrayList();
                    children.put(child.getNodeName(), childs);
                }
                childs.add(new DDBeanImpl(root, xpath + "/" + child.getNodeName(), child));
            }
        }
    }

    public DDBeanRoot getRoot() {
        return root;
    }

    public String getXpath() {
        return xpath;
    }

    public String getText() {
        return (String) XMLUtil.getContent(element);
    }

    public String getId() {
        return getAttributeValue("ID");
    }

    public String getAttributeValue(String attrName) {
        String value = element.getAttribute(attrName).trim();
        if (value.length() == 0) {
            value = null;
        }
        return value;
    }

    public String[] getText(String xpath) {
        DDBean[] beans = getChildBean(xpath);
        if (beans == null) {
            return null;
        }

        String[] text = new String[beans.length];
        for (int i = 0; i < beans.length; i++) {
            text[i] = beans[i].getText();
        }
        return text;
    }

    public DDBean[] getChildBean(String xpath) {
        if (xpath.startsWith("/")) {
            return getRoot().getChildBean(xpath.substring(1));
        }
        int index = xpath.indexOf('/');
        if (index == -1) {
            List beans = (List) children.get(xpath);
            if (beans == null) {
                return null;
            }
            return (DDBean[]) beans.toArray(new DDBean[beans.size()]);
        } else {
            List childBeans = (List) children.get(xpath.substring(0, index));
            String path = xpath.substring(index + 1);
            List beans = new ArrayList();
            for (Iterator i = childBeans.iterator(); i.hasNext();) {
                DDBean bean = (DDBean) i.next();
                DDBean[] childs = bean.getChildBean(path);
                if (childs != null) {
                    for (int j = 0; j < childs.length; j++) {
                        beans.add(childs[j]);
                    }
                }
            }
            return beans.size() > 0 ? (DDBean[]) beans.toArray(new DDBean[beans.size()]) : null;
        }
    }

    public String[] getAttributeNames() {
        NamedNodeMap attrs = element.getAttributes();
        String[] attrNames = new String[attrs.getLength()];
        for (int i = 0; i < attrNames.length; i++) {
            Attr node = (Attr) attrs.item(i);
            attrNames[i] = node.getName();
        }
        return attrNames;
    }

    public void addXpathListener(String xpath, XpathListener xpl) {
    }

    public void removeXpathListener(String xpath, XpathListener xpl) {
    }
}
