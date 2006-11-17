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

package org.apache.geronimo.deployment.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.XpathListener;

import org.apache.xmlbeans.XmlCursor;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class DDBeanImpl implements DDBean {
    protected final DDBeanRoot root;
    protected final String xpath;
    protected final Map children;
    protected final String content;
    protected final Map attributeMap;
    protected final DDBean parent;

    public DDBeanImpl(DDBeanRoot root, DDBean parent, String xpath, XmlCursor c) {
        this.root = root;
        this.parent = parent;
        this.xpath = xpath;
        this.children = new HashMap();
        this.attributeMap = new HashMap();
        content = c.getTextValue();
        c.push();
        if (c.toFirstAttribute()) {
            do {
                attributeMap.put(c.getName().getLocalPart(), c.getTextValue());
            } while (c.toNextAttribute());
        }
        c.pop();
        c.push();
        if (c.toFirstChild()) {
            do {
                String name = c.getName().getLocalPart();
                List nodes = (List) children.get(name);
                if (nodes == null) {
                    nodes = new ArrayList();
                    children.put(name, nodes);
                }
                nodes.add(new DDBeanImpl(root, this, xpath + "/" + name, c));
            } while (c.toNextSibling());
        }
        c.pop();
    }

    DDBeanImpl(DDBeanImpl source, String xpath) {
        this.xpath = xpath;
        this.root = source.root;
        this.parent = source.parent;
        this.children = source.children;
        this.content = source.content;
        this.attributeMap = source.attributeMap;
    }

    public DDBeanRoot getRoot() {
        return root;
    }

    public String getXpath() {
        return xpath;
    }

    public String getText() {
        return content;
    }

    public String getId() {
        return getAttributeValue("id");
    }

    public String getAttributeValue(String attrName) {
        String value = (String) attributeMap.get(attrName);
        if (value == null || value.length() == 0) {
            return null;
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
        } else if(xpath.equals(".")) {
            return new DDBean[]{this};
        } else if(xpath.startsWith("./")) {
            return getChildBean(xpath.substring(2));
        } else if(xpath.startsWith("..")) {
            if(xpath.length() == 2) {
                return new DDBean[]{parent};
            } else {
                return parent.getChildBean(xpath.substring(3));
            }
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
        return (String[]) attributeMap.keySet().toArray(new String[attributeMap.size()]);
    }

    public void addXpathListener(String xpath, XpathListener xpl) {
    }

    public void removeXpathListener(String xpath, XpathListener xpl) {
    }

    public boolean equals(Object other) {
        if (other.getClass() != DDBeanImpl.class) {
            return false;
        }
        DDBeanImpl otherdd = (DDBeanImpl) other;
        return xpath.equals(otherdd.xpath)
                && children.equals(otherdd.children)
                && attributeMap.equals(otherdd.attributeMap)
                && root.equals(otherdd.root);
    }

    public int hashCode() {
        return xpath.hashCode() ^ attributeMap.hashCode() ^ root.hashCode();
    }
}
