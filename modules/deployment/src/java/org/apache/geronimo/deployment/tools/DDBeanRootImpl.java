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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.shared.ModuleType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

/**
 * 
 * 
 * @version $Revision: 1.1 $ $Date: 2004/01/21 20:37:29 $
 */
public class DDBeanRootImpl implements DDBeanRoot {
    private final DeployableObject deployable;
    private final Document doc;
    private final DDBean docBean;

    public DDBeanRootImpl(DeployableObject deployable, URL descriptor) throws DDBeanCreateException {
        this.deployable = deployable;
        DocumentBuilder parser = null;
        try {
            parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw (DDBeanCreateException) new DDBeanCreateException("Unable to load parser").initCause(e);
        }
        InputStream is = null;
        try {
            is = descriptor.openStream();
            doc = parser.parse(is);
        } catch (Exception e) {
            throw (DDBeanCreateException) new DDBeanCreateException("Unable to parse descriptor").initCause(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        docBean = new DDBeanImpl(this, "/"+ doc.getDocumentElement().getNodeName(), doc.getDocumentElement());
    }

    public DDBeanRoot getRoot() {
        return this;
    }

    public String getXpath() {
        return "/";
    }

    public String getDDBeanRootVersion() {
        return doc.getDocumentElement().getAttribute("version");
    }

    public DeployableObject getDeployableObject() {
        return deployable;
    }

    public String getFilename() {
        throw new UnsupportedOperationException();
    }

    public String getModuleDTDVersion() {
        throw new UnsupportedOperationException();
    }

    public ModuleType getType() {
        return deployable.getType();
    }

    public String getId() {
        return null;
    }

    public String getText() {
        return null;
    }

    public String[] getAttributeNames() {
        return null;
    }

    public String getAttributeValue(String attrName) {
        return null;
    }

    public DDBean[] getChildBean(String xpath) {
        if (xpath.startsWith("/")) {
            xpath = xpath.substring(1);
        }
        int index = xpath.indexOf('/');
        String childName = (index == -1) ? xpath : xpath.substring(0, index);
        if (childName.equals(doc.getDocumentElement().getNodeName())) {
            if (index == -1) {
                return new DDBean[] {docBean };
            } else {
                return docBean.getChildBean(xpath.substring(index+1));
            }
        } else {
            return null;
        }
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

    public void addXpathListener(String xpath, XpathListener xpl) {
        throw new UnsupportedOperationException();
    }

    public void removeXpathListener(String xpath, XpathListener xpl) {
        throw new UnsupportedOperationException();
    }
}
