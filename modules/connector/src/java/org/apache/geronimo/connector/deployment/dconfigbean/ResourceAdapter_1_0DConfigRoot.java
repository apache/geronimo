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

package org.apache.geronimo.connector.deployment.dconfigbean;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.plugin.DConfigBeanRootSupport;
import org.apache.geronimo.xbeans.geronimo.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerVersionType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlBeans;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/22 19:11:52 $
 *
 * */
public class ResourceAdapter_1_0DConfigRoot extends DConfigBeanRootSupport {

    private static String[] XPATHS = {
        "connector/resourceadapter"
    };

    private ConnectionDefinitionDConfigBean connectionDefinitionDConfigBean;

    public ResourceAdapter_1_0DConfigRoot(DDBeanRoot ddBean) {
        super(ddBean, GerConnectorDocument.Factory.newInstance());
        GerConnectionDefinitionType connectionDefinition = getConnectorDocument().addNewConnector().addNewResourceadapter().addNewOutboundResourceadapter().addNewConnectionDefinition();
        getConnectorDocument().getConnector().setVersion(GerVersionType.X_1_0);
        replaceConnectionDefinitionDConfigBean(connectionDefinition);
    }

    private void replaceConnectionDefinitionDConfigBean(GerConnectionDefinitionType connectionDefinition) {
        DDBean ddBean = getDDBean();
        DDBean childDDBean = ddBean.getChildBean(getXpaths()[0])[0];
        connectionDefinitionDConfigBean = new ConnectionDefinitionDConfigBean(childDDBean, connectionDefinition);
    }

    GerConnectorDocument getConnectorDocument() {
        return (GerConnectorDocument) getXmlObject();
    }

    public String[] getXpaths() {
        return XPATHS;
    }

    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException {
        if (getXpaths()[0].equals(bean.getXpath())) {
            return connectionDefinitionDConfigBean;
        }
        return null;
    }

    public void fromXML(InputStream inputStream) throws XmlException, IOException {
        super.fromXML(inputStream);
        if (!getConnectorDocument().getConnector().getVersion().equals(GerVersionType.X_1_0)) {
            throw new IllegalStateException("Wrong version, expected 1.0");
        }
        replaceConnectionDefinitionDConfigBean(getConnectorDocument().getConnector().getResourceadapter().getOutboundResourceadapter().getConnectionDefinitionArray(0));
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return ResourceAdapterDConfigRoot.SCHEMA_TYPE_LOADER;
    }
}
