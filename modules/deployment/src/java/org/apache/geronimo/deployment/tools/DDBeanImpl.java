/**
 *
 * Copyright 2004 The Apache Software Foundation
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
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:57:38 $
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

    DDBeanImpl(DDBeanImpl source, String xpath) {
        this.xpath = xpath;
        this.root = source.root;
        this.children = source.children;
        this.element = source.element;
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
            DDBean[] newDDBeans = (DDBean[]) beans.toArray(new DDBean[beans.size()]);
            for (int i = 0; i < newDDBeans.length; i++) {
                newDDBeans[i] = new DDBeanImpl((DDBeanImpl) newDDBeans[i], xpath);
            }
            return newDDBeans;
        } else {
            List childBeans = (List) children.get(xpath.substring(0, index));
            if (childBeans == null) {
                return null;
            }
            String path = xpath.substring(index + 1);
            List beans = new ArrayList();
            for (Iterator i = childBeans.iterator(); i.hasNext();) {
                DDBean bean = (DDBean) i.next();
                DDBean[] childs = bean.getChildBean(path);
                if (childs != null) {
                    for (int j = 0; j < childs.length; j++) {
                        beans.add(new DDBeanImpl((DDBeanImpl) childs[j], xpath));
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

    public boolean equals(Object other) {
        if (other.getClass() != DDBeanImpl.class) {
            return false;
        }
        DDBeanImpl otherdd = (DDBeanImpl)other;
        return xpath.equals(otherdd.xpath)
        && element.equals(otherdd.element)
        && root.equals(otherdd.root);
    }

    public int hashCode() {
        return xpath.hashCode() ^ element.hashCode() ^ root.hashCode();
    }
}
