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
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:58:49 $
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
        return docBean.getAttributeNames();
    }

    public String getAttributeValue(String attrName) {
        return docBean.getAttributeValue(attrName);
    }

    public DDBean[] getChildBean(String xpath) {
        if (xpath.startsWith("/")) {
            xpath = xpath.substring(1);
        }
        int index = xpath.indexOf('/');
        String childName = (index == -1) ? xpath : xpath.substring(0, index);
        if (childName.equals(doc.getDocumentElement().getNodeName())) {
            if (index == -1) {
                return new DDBean[] {new DDBeanImpl((DDBeanImpl)docBean, xpath)};
            } else {
                DDBean[] newDDBeans = docBean.getChildBean(xpath.substring(index+1));
                for (int i = 0; i < newDDBeans.length; i++) {
                    newDDBeans[i] = new DDBeanImpl((DDBeanImpl)newDDBeans[i], xpath);
                }
                return newDDBeans;
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
