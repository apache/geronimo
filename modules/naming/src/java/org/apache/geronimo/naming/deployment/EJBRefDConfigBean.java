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

package org.apache.geronimo.naming.deployment;

import javax.enterprise.deploy.model.DDBean;

import org.apache.geronimo.deployment.plugin.DConfigBeanSupport;
import org.apache.geronimo.xbeans.geronimo.GerEjbRefType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:57 $
 */
public class EJBRefDConfigBean extends DConfigBeanSupport {

    public EJBRefDConfigBean(DDBean ddBean, GerEjbRefType ejbRef) {
        super(ddBean, ejbRef);
        assert ddBean.getChildBean("ejb-ref-name")[0].equals(getEjbRefName());
    }

    GerEjbRefType getEjbRef() {
        return (GerEjbRefType)getXmlObject();
    }

    String getEjbRefName() {
        return getEjbRef().getEjbRefName().getStringValue();
    }

    public String getTargetURI() {
        return getEjbRef().getUri();
    }

    public void setTargetURI(String targetURI) {
        getEjbRef().setUri(targetURI);
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return ENCHelper.SCHEMA_TYPE_LOADER;
    }
}
