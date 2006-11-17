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

import java.io.InputStream;
import java.net.URL;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.shared.ModuleType;

import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class DDBeanRootImpl implements DDBeanRoot {
    private final DeployableObject deployable;
    private final DDBean docBean;

    public DDBeanRootImpl(DeployableObject deployable, URL descriptor) throws DDBeanCreateException {
        this.deployable = deployable;
        InputStream is = null;
        try {
            is = descriptor.openStream();
            try {
                XmlObject xmlObject = XmlBeansUtil.parse(is);
                XmlCursor c = xmlObject.newCursor();
                try {
                    c.toStartDoc();
                    c.toFirstChild();
                    docBean = new DDBeanImpl(this, this, "/" + c.getName().getLocalPart(), c);
                } finally {
                    c.dispose();
                }
            } finally {
                is.close();
            }
        } catch (Exception e) {
            throw (DDBeanCreateException) new DDBeanCreateException("problem").initCause(e);
        }
    }

    public DDBeanRoot getRoot() {
        return this;
    }

    public String getXpath() {
        return "/";
    }

    public String getDDBeanRootVersion() {
        return docBean.getAttributeValue("version");
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
        if (("/" + childName).equals(docBean.getXpath())) {
            if (index == -1) {
                return new DDBean[]{new DDBeanImpl((DDBeanImpl) docBean, xpath)};
            } else {
                DDBean[] newDDBeans = docBean.getChildBean(xpath.substring(index + 1));
                if (newDDBeans != null) {
                    for (int i = 0; i < newDDBeans.length; i++) {
                        newDDBeans[i] = new DDBeanImpl((DDBeanImpl) newDDBeans[i], xpath);
                    }
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
