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
 * @version $Revision: 1.2 $ $Date: 2003/09/04 05:24:21 $
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