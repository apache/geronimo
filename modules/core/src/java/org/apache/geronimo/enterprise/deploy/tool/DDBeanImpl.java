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

package org.apache.geronimo.enterprise.deploy.tool;

import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.XpathListener;

import org.dom4j.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The DDBeanImpl provides an implementation for javax.enterprise.deploy.model.DDBean.
 *
 * @version $Rev$ $Date$
 */
public class DDBeanImpl implements DDBean {
    private final static Log log = LogFactory.getLog(DDBeanImpl.class);

    protected Node node;
    protected String id;
    protected DDBeanRoot root;
    protected String text;
    protected String xPath;
    protected HashMap listeners;

    /** Creates a new instance of DDBeanImpl */
    DDBeanImpl() {
    }
    
    /**
     * Creates a new instance of DDBeanImpl
     * @todo Grab DDBeanRoot
     */
    DDBeanImpl(Node node, String xpath) {
        this.node = node;
        this.id = node.getName();
        this.text = node.getText();
        this.xPath = xpath;
    }
    
    public void addXpathListener(String str, XpathListener xpathListener) {
        if ( listeners == null ) listeners = new HashMap();
        listeners.put(str, xpathListener);
    }
    
    public DDBean[] getChildBean(String str) {
        String tempXPath = createXPath(str);

        List nodes = node.selectNodes(tempXPath);
        int count = nodes.size();
        DDBeanImpl[] ddBeans = new DDBeanImpl[count];
        for ( int i = 0; i < count; i++ ) {
            Node node = (Node)nodes.get(i);
            ddBeans[i] = new DDBeanImpl(node, str); // @todo doesn't this need a factory?
        }
        return ddBeans;
    }
    
    public String[] getText(String str) {
        String tempXPath = createXPath(str);

        List nodes = node.selectNodes(tempXPath);
        int count = nodes.size();
        String[] text = new String[count];
        for ( int i = 0; i < count; i++ ) {
            Node node = (Node)nodes.get(i);
            text[i] = node.getText();
        }
        return text;
    }

    private String createXPath(String path) {
        StringTokenizer tokens = new StringTokenizer(path, "/");
        StringBuffer buffer = new StringBuffer(100);
        while ( tokens.hasMoreTokens() ) {
            if(buffer.length() > 0) {
                buffer.append("/");
            }
            buffer.append("*[name()='").append(tokens.nextToken()).append("']");
        }
        return buffer.toString();
    }
    
    public String getId() {
        return id;
    }
    
    public DDBeanRoot getRoot() {
        return root;
    }
    
    public String getText() {
        return text;
    }
    
    public String getXpath() {
        return xPath;
    }
    
    public void removeXpathListener(String str, XpathListener xpathListener) {
        if ( listeners != null ) listeners.remove(str);
    }

    public String[] getAttributeNames() {
        // @todo implement
        return new String[0];
    }

    public String getAttributeValue(String attrName) {
        // @todo implement
        return null;
    }

    /**
     * DDBeans are the same if nodes are the same
     */
    public int hashCode() {
        return node.hashCode();
    }

    /**
     * DDBeans are the same if nodes are the same
     */
    public boolean equals(Object obj) {
        try {
            return node.equals(((DDBeanImpl)obj).node);
        } catch(ClassCastException e) {
            return false;
        }
    }
}