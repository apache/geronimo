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
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class ResourceRefMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    public static final String RESOURCE_REF_NAME_PREFIX = "resource-ref.res-ref-name.";

    /**
     * Steps :
     * a. If we already have a resource-ref in the current merged web.xml file, then
     *     a.1 If it is from web-fragment.xml, throw an error, as it is not allowed that the same resource-ref in different web-fragment.xml while it is not present in web.xml
     *     a.2 Else it is from web.xml, check whether inject-target is configured in the initial web.xml, if not, merge the configurations from web-fragment.xml, else ignore.
     * b. web.xml file should inherit it from the web-fragment.xml file
     */
    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (ResourceRef srcResourceRef : webFragment.getResourceRef()) {
            String resourceRefName = srcResourceRef.getResRefName();
            String resourceRefKey = createResourceRefKey(resourceRefName);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(resourceRefKey);
            if (mergeItem != null) {
                if (mergeItem.isFromWebFragment()) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("resource-ref", resourceRefName, mergeItem.getBelongedURL(), mergeContext.getCurrentJarUrl()));
                } else if (mergeItem.isFromWebXml() && !isResourceRefInjectTargetsConfiguredInInitialWebXML(resourceRefName, mergeContext)) {
                    //Merge InjectTarget
                    ResourceRef resourceRef = (ResourceRef) mergeItem.getValue();
                    for (InjectionTarget injectTarget : srcResourceRef.getInjectionTarget()) {
                        String resourceRefInjectTargetKey = createResourceRefInjectTargetKey(resourceRefName, injectTarget.getInjectionTargetClass(), injectTarget
                                .getInjectionTargetName());
                        if (!mergeContext.containsAttribute(resourceRefInjectTargetKey)) {
                            resourceRef.getInjectionTarget().add(injectTarget);
                            mergeContext.setAttribute(resourceRefInjectTargetKey, Boolean.TRUE);
                        }
                    }
                }
            } else {
                webApp.getResourceRef().add(srcResourceRef);
                mergeContext.setAttribute(resourceRefKey, new MergeItem(srcResourceRef, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
                for (InjectionTarget injectionTarget : srcResourceRef.getInjectionTarget()) {
                    mergeContext.setAttribute(createResourceRefInjectTargetKey(resourceRefName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
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
        for (ResourceRef resourceRef : webApp.getResourceRef()) {
            String resourceRefName = resourceRef.getResRefName();
            mergeContext.setAttribute(createResourceRefKey(resourceRefName), new MergeItem(resourceRef, null, ElementSource.WEB_XML));
            //Create an attribute tag to indicate whether injectTarget is configured in web.xml file
            if (!resourceRef.getInjectionTarget().isEmpty()) {
                mergeContext.setAttribute(createResourceRefInjectTargetConfiguredInWebXMLKey(resourceRefName), Boolean.TRUE);
            }
            for (InjectionTarget injectionTarget : resourceRef.getInjectionTarget()) {
                mergeContext.setAttribute(createResourceRefInjectTargetKey(resourceRefName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
                        ), Boolean.TRUE);
            }
        }
    }

    public static String createResourceRefInjectTargetConfiguredInWebXMLKey(String resourceRefName) {
        return RESOURCE_REF_NAME_PREFIX + resourceRefName + ".inject_target_configured_in_web_xml";
    }

    public static String createResourceRefInjectTargetKey(String resourceRefName, String injectTargetClassName, String injectTargetName) {
        return RESOURCE_REF_NAME_PREFIX + resourceRefName + "." + injectTargetClassName + "." + injectTargetName;
    }

    public static String createResourceRefKey(String resourceRefName) {
        return RESOURCE_REF_NAME_PREFIX + resourceRefName;
    }

    public static boolean isResourceRefInjectTargetConfigured(String resourceRefName, String injectTargetClassName, String injectTargetName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createResourceRefInjectTargetKey(resourceRefName, injectTargetClassName, injectTargetName));
    }

    public static boolean isResourceRefInjectTargetsConfiguredInInitialWebXML(String resourceRefName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createResourceRefInjectTargetConfiguredInWebXMLKey(resourceRefName));
    }
}
