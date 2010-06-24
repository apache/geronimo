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
import org.apache.geronimo.web25.deployment.merge.ElementSource;
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.geronimo.web25.deployment.merge.MergeItem;
import org.apache.geronimo.web25.deployment.utils.WebDeploymentMessageUtils;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class ResourceEnvRefMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    public static final String RESOURCE_ENV_REF_NAME_PREFIX = "resource-env-ref.resource-env-ref-name.";

    /**
     * Steps :
     * a. If we already have a resource-env-ref in the current merged web.xml file, then
     *     a.1 If it is from web-fragment.xml, throw an error, as it is not allowed that the same resource-env-ref in different web-fragment.xml while it is not present in web.xml
     *     a.2 Else it is from web.xml, check whether inject-target is configured in the initial web.xml, if not, merge the configurations from web-fragment.xml, else ignore.
     * b. web.xml file should inherit it from the web-fragment.xml file
     */
    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (ResourceEnvRef srcResourceEnvRef : webFragment.getResourceEnvRef()) {
            String resourceEnvRefName = srcResourceEnvRef.getResourceEnvRefName();
            String resourceEnvRefKey = createResourceEnvRefKey(resourceEnvRefName);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(resourceEnvRefKey);
            if (mergeItem != null) {
                if (mergeItem.isFromWebFragment()) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("resource-env-ref", resourceEnvRefName, mergeItem.getBelongedURL(), mergeContext.getCurrentJarUrl()));
                } else if (mergeItem.isFromWebXml() && !isResourceEnvRefInjectTargetsConfiguredInInitialWebXML(resourceEnvRefName, mergeContext)) {
                    //Merge InjectTarget
                    ResourceEnvRef resourceEnvRef = (ResourceEnvRef) mergeItem.getValue();
                    for (InjectionTarget injectTarget : srcResourceEnvRef.getInjectionTarget()) {
                        String resourceEnvRefInjectTargetKey = createResourceEnvRefInjectTargetKey(resourceEnvRefName, injectTarget.getInjectionTargetClass(), injectTarget
                                .getInjectionTargetName());
                        if (!mergeContext.containsAttribute(resourceEnvRefInjectTargetKey)) {
                            resourceEnvRef.getInjectionTarget().add(injectTarget);
                            mergeContext.setAttribute(resourceEnvRefInjectTargetKey, Boolean.TRUE);
                        }
                    }
                }
            } else {
                webApp.getResourceEnvRef().add(srcResourceEnvRef);
                mergeContext.setAttribute(resourceEnvRefKey, new MergeItem(srcResourceEnvRef, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
                for (InjectionTarget injectionTarget : srcResourceEnvRef.getInjectionTarget()) {
                    mergeContext.setAttribute(createResourceEnvRefInjectTargetKey(resourceEnvRefName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
                            ), Boolean.TRUE);
                }
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (ResourceEnvRef resourceEnvRef : webApp.getResourceEnvRef()) {
            String resourceEnvRefName = resourceEnvRef.getResourceEnvRefName();
            mergeContext.setAttribute(createResourceEnvRefKey(resourceEnvRefName), new MergeItem(resourceEnvRef, null, ElementSource.WEB_XML));
            //Create an attribute tag to indicate whether injectTarget is configured in web.xml file
            if (!resourceEnvRef.getInjectionTarget().isEmpty()) {
                mergeContext.setAttribute(createResourceEnvRefInjectTargetConfiguredInWebXMLKey(resourceEnvRefName), Boolean.TRUE);
            }
            for (InjectionTarget injectionTarget : resourceEnvRef.getInjectionTarget()) {
                mergeContext.setAttribute(createResourceEnvRefInjectTargetKey(resourceEnvRefName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
                        ), Boolean.TRUE);
            }
        }
    }

    public static String createResourceEnvRefInjectTargetConfiguredInWebXMLKey(String resourceEnvRefName) {
        return RESOURCE_ENV_REF_NAME_PREFIX + resourceEnvRefName + ".inject_target_configured_in_web_xml";
    }

    public static String createResourceEnvRefInjectTargetKey(String resourceEnvRefName, String injectTargetClassName, String injectTargetName) {
        return RESOURCE_ENV_REF_NAME_PREFIX + resourceEnvRefName + "." + injectTargetClassName + "." + injectTargetName;
    }

    public static String createResourceEnvRefKey(String resourceEnvRefName) {
        return RESOURCE_ENV_REF_NAME_PREFIX + resourceEnvRefName;
    }

    public static boolean isResourceEnvRefInjectTargetConfigured(String resourceEnvRefName, String injectTargetClassName, String injectTargetName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createResourceEnvRefInjectTargetKey(resourceEnvRefName, injectTargetClassName, injectTargetName));
    }

    public static boolean isResourceEnvRefInjectTargetsConfiguredInInitialWebXML(String resourceEnvRefName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createResourceEnvRefInjectTargetConfiguredInWebXMLKey(resourceEnvRefName));
    }
}
