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
import org.apache.openejb.jee.LifecycleCallback;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class PostConstructMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        if (mergeContext.containsAttribute(createPostConstructorConfiguredInWebXMLKey())) {
            return;
        }
        for (LifecycleCallback lifecycleCallback : webFragment.getPostConstruct()) {
            webApp.getPostConstruct().add(lifecycleCallback);
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        context.setAttribute(createPostConstructorConfiguredInWebXMLKey(), !webApp.getPostConstruct().isEmpty());
    }

    public static String createPostConstructorConfiguredInWebXMLKey() {
        return "post-construct.configured_in_web_xml";
    }
}
