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
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class LocalRefDConfigBean extends DConfigBeanSupport {

    protected final RefAdapter ref;

    public LocalRefDConfigBean(DDBean ddBean, RefAdapter ref, String namePath) {
        super(ddBean, ref.getXmlObject());
        this.ref = ref;

        assert ddBean.getChildBean(namePath)[0].getText().equals(getRefName());
    }

    String getRefName() {
        return ref.getRefName();
    }

    public String getExternalUri() {
        return ref.getExternalUri();
    }

    public void setExternalUri(String targetURI) {
        ref.setExternalUri(targetURI);
    }


    public String getKernelName() {
        return ref.getKernelName();
    }

    public void setKernelName(String kernelName) {
        ref.setKernelName(kernelName);
    }

    public String getTargetName() {
        return ref.getTargetName();
    }

    public void setTargetName(String targetName) {
        ref.setTargetName(targetName);
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return ENCHelper.SCHEMA_TYPE_LOADER;
    }
}
