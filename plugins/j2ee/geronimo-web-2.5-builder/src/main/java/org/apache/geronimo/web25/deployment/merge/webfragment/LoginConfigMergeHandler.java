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

package org.apache.geronimo.web25.deployment.merge.webfragment;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.geronimo.web25.deployment.utils.WebDeploymentMessageUtils;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @FIXME For login-config, no rules are mentioned in spec, from my understanding, we should only use the one from web.xml
 * @version $Rev$ $Date$
 */
public class LoginConfigMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    @Override
    public void merge(WebFragment srcElement, WebApp targetElement, MergeContext mergeContext) throws DeploymentException {
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        if (webApp.getLoginConfig().size() > 1) {
            throw new DeploymentException(WebDeploymentMessageUtils.createMultipleConfigurationWebAppErrorMessage("login-config"));
        }
    }
}
