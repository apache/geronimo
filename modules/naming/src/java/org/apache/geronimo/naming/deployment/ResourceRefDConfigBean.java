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
import org.apache.geronimo.xbeans.geronimo.GerResourceRefType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:57 $
 */
public class ResourceRefDConfigBean extends DConfigBeanSupport {

    public ResourceRefDConfigBean(DDBean ddBean, GerResourceRefType resourceRef) {
        super(ddBean, resourceRef);
        assert ddBean.getChildBean("res-ref-name")[0].equals(getResourceRefName());
    }

    GerResourceRefType getResourceRef() {
        return (GerResourceRefType)getXmlObject();
    }

    String getResourceRefName() {
        return getResourceRef().getResRefName().getStringValue();
    }

    public String getTargetURI() {
        return getResourceRef().getUri();
    }

    public void setTargetURI(String targetURI) {
        getResourceRef().setUri(targetURI);
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return ENCHelper.SCHEMA_TYPE_LOADER;
    }
}
