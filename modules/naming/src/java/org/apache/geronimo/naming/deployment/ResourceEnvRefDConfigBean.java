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
import org.apache.geronimo.xbeans.geronimo.GerResourceEnvRefType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:57 $
 */
public class ResourceEnvRefDConfigBean extends DConfigBeanSupport {

    public ResourceEnvRefDConfigBean(DDBean ddBean, GerResourceEnvRefType resourceEnvRef) {
        super(ddBean, resourceEnvRef);
        assert ddBean.getChildBean("resource-env-ref-name")[0].equals(getResourceEnvRefName());
    }

    GerResourceEnvRefType getResourceEnvRef() {
        return (GerResourceEnvRefType)getXmlObject();
    }

    String getResourceEnvRefName() {
        return getResourceEnvRef().getResourceEnvRefName().getStringValue();
    }

    public String getTargetURI() {
        return getResourceEnvRef().getUri();
    }

    public void setTargetURI(String targetURI) {
        getResourceEnvRef().setUri(targetURI);
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return ENCHelper.SCHEMA_TYPE_LOADER;
    }
}
